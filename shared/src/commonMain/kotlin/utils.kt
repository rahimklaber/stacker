import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import me.rahimklaber.stellar.GetTransactionResponse
import me.rahimklaber.stellar.SimulateTransactionResponse
import me.rahimklaber.stellar.SorobanClient
import me.rahimklaber.stellar.base.Account
import me.rahimklaber.stellar.base.Network
import me.rahimklaber.stellar.base.StrKey
import me.rahimklaber.stellar.base.Transaction
import me.rahimklaber.stellar.base.VersionByte
import me.rahimklaber.stellar.base.decodeContractAddress
import me.rahimklaber.stellar.base.encodeAccountId
import me.rahimklaber.stellar.base.encodeCheck
import me.rahimklaber.stellar.base.encodeToAccountIDXDR
import me.rahimklaber.stellar.base.encodeToScAddress
import me.rahimklaber.stellar.base.operations.InvokeHostFunction
import me.rahimklaber.stellar.base.transactionBuilder
import me.rahimklaber.stellar.base.xdr.Hash
import me.rahimklaber.stellar.base.xdr.HostFunction
import me.rahimklaber.stellar.base.xdr.InvokeContractArgs
import me.rahimklaber.stellar.base.xdr.InvokeHostFunctionOp
import me.rahimklaber.stellar.base.xdr.PublicKey
import me.rahimklaber.stellar.base.xdr.SorobanAuthorizationEntry
import me.rahimklaber.stellar.base.xdr.SorobanTransactionData
import me.rahimklaber.stellar.base.xdr.TransactionMeta
import me.rahimklaber.stellar.base.xdr.decodeFromString
import me.rahimklaber.stellar.base.xdr.soroban.Int128Parts
import me.rahimklaber.stellar.base.xdr.soroban.SCBytes
import me.rahimklaber.stellar.base.xdr.soroban.SCMap
import me.rahimklaber.stellar.base.xdr.soroban.SCSymbol
import me.rahimklaber.stellar.base.xdr.soroban.SCVal
import me.rahimklaber.stellar.base.xdr.soroban.SCVec
import me.rahimklaber.stellar.base.xdr.soroban.ScAddress
import me.rahimklaber.stellar.base.xdr.soroban.UInt128Parts
import me.rahimklaber.stellar.base.xdr.toXdrString

val zeroAccount = Account(StrKey.encodeAccountId(ByteArray(32) { 0b0 }), 0L)

fun <T: SCVal> SCVal?.cast() : T = this as T

fun getReturnValue(transactionMeta: TransactionMeta) = (transactionMeta as TransactionMeta.V3).v3.sorobanMeta!!.returnValue

fun createSymbol(value: String) = SCVal.Symbol(SCSymbol(value))

fun createAddressXdr(value: String) = catch({
    SCVal.Address(ScAddress.Account(StrKey.encodeToAccountIDXDR(value)))
}) {
    SCVal.Address(ScAddress.Contract(Hash(StrKey.decodeContractAddress(value))))
}

fun addressFromScVal(value: SCVal.Address) = when(value.address){
    is ScAddress.Account -> StrKey.encodeAccountId(((value.address as ScAddress.Account).accountId.publicKey as PublicKey.PublicKeyEd25519).ed25519.byteArray)
    is ScAddress.Contract -> StrKey.encodeCheck(VersionByte.CONTRACT, (value.address as ScAddress.Contract).contractId.byteArray)
}


fun createI128(value: ULong) = SCVal.I128(Int128Parts(0, value))
fun createU128(value: ULong) = SCVal.U128(UInt128Parts(0u, value))

fun SCVal.I128.stringValue() = i128.low.toString()
fun SCVal.I128.toULong() = i128.low
fun SCVal.U128.toULong() = u128.low

fun createU64(value: ULong) = SCVal.U64(value)
fun createU32(value: UInt) = SCVal.U32(value)

inline operator fun SCMap.get(key: String) = mapEntries.find { it.key == createSymbol(key) }?.value

fun createSorobanTransaction(source: Account, contractId: String, function: String, args: List<SCVal> = listOf(), network: Network, fee: UInt = 2000u): Transaction{
    return transactionBuilder(source, network){
        addOperation(
            InvokeHostFunction(
                InvokeHostFunctionOp(
                    HostFunction.InvokeContract(
                        InvokeContractArgs(
                            StrKey.encodeToScAddress(contractId),
                            createSymbol(function).symbol,
                            args
                        ),
                    ),
                    listOf(),
                ),
            )
        )
        setFee(fee)
    }
}

/**
 * 1. Simulates the transactions
 * 2. Adds the sorobandata and authentry to transaction
 */
suspend fun prepareTransaction(sorobanClient: SorobanClient, transaction: Transaction): Either<Throwable, Transaction> = either{
    val simulateRes = sorobanClient.simulateTransaction(transaction.toEnvelopeXdr().toXdrString())

    ensure(simulateRes.error == null){
        IllegalStateException("Rpc call returned error: ${simulateRes.error}")
    }

    ensureNotNull(simulateRes.transactionData){
        IllegalStateException("Simulate rpc call does not have any sorobanData")
    }

    var newTx = transaction
        .withSorobanData(SorobanTransactionData.decodeFromString(simulateRes.transactionData!!))
        .copy(fee = simulateRes.minResourceFee!!.toUInt() + 200u)

    newTx = catch({
        newTx.withAuthEntry(SorobanAuthorizationEntry.decodeFromString(simulateRes.results!!.first().auth.first()))
    }){
        newTx
    }

    newTx
}

typealias TransactionBlob = String
typealias SignFun = suspend (Transaction) -> TransactionBlob
typealias TransactionHash = String

/**
 * 1. Creates the tx
 * 2. Simulates the tx
 * 3. adds the sorobandata and authentry to the tx
 * 4. submit the tx
 */
@OptIn(ExperimentalStdlibApi::class)
suspend fun createPrepareAndSubmitTx(sorobanClient: SorobanClient, source: Account, contractId: String, function: String, args: List<SCVal> = listOf(), network: Network, sign: SignFun) = either<Throwable, Pair<SCVal, TransactionHash>>{
    var tx =
        createSorobanTransaction(source, contractId, function, args, network)

    tx = prepareTransaction(sorobanClient, tx).bind()


    val signedBlob = sign(tx)

    val submitResponse = submitWithRpc(sorobanClient, signedBlob).bind()

    val transactionMeta = TransactionMeta.decodeFromString(submitResponse.resultMetaXdr!!) as TransactionMeta.V3

    getReturnValue(transactionMeta) to tx.hash().toHexString()
}

fun getSimulationReturnValue(result: SimulateTransactionResponse): SCVal? {
    return result.results?.first()?.xdr?.let(SCVal::decodeFromString)
}

@OptIn(ExperimentalStdlibApi::class)
fun bytesFromHex(hex: String) = SCVal.Bytes(SCBytes(hex.hexToByteArray().toList()))


suspend fun simulateTransaction(sorobanClient: SorobanClient, contractId: String, function: String, args: List<SCVal> = listOf(), network: Network) = either<Throwable, SCVal>{
    var tx =
        createSorobanTransaction(zeroAccount, contractId, function, args, network)

    val result = sorobanClient.simulateTransaction(tx.toEnvelopeXdr().toXdrString())

    getSimulationReturnValue(result).cast()
}

suspend fun submitWithRpc(rpc: SorobanClient, txBlob: String) = either<Throwable, GetTransactionResponse> {
    val result = rpc.sendTransaction(txBlob)

    ensure(result.status == "PENDING"){
        IllegalStateException("Submission failed when sending transaction $result")
    }

    val ok = withTimeoutOrNull(10_000){
        while (true){
            delay(1000)

            val getTransactionResponse = rpc.getTransaction(result.hash)

            when(getTransactionResponse.status){
                "FAILED" -> raise(IllegalStateException("Submission failed $getTransactionResponse"))
                "SUCCESS" -> return@withTimeoutOrNull getTransactionResponse
            }
        }
    }

    if(ok == null){
        raise(IllegalStateException("Submission failed"))
    }

    ok as GetTransactionResponse
}


fun List<SCVal>.toScVec() = SCVal.Vec(SCVec(this))
fun SCVal.Vec.toList() = vec!!.vals

fun Network.name() = when(this){
    Network.TESTNET -> "TESTNET"
    Network.PUBLIC -> "PUBLIC"
    else -> error("unknown network $this")
}