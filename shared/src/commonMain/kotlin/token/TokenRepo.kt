package token

import cast
import createAddressXdr
import createPrepareAndSubmitTx
import createSorobanTransaction
import me.rahimklaber.stellar.SorobanClient
import me.rahimklaber.stellar.base.Account
import me.rahimklaber.stellar.base.Transaction
import me.rahimklaber.stellar.base.xdr.decodeFromString
import me.rahimklaber.stellar.base.xdr.soroban.Int128Parts
import me.rahimklaber.stellar.base.xdr.soroban.SCVal
import me.rahimklaber.stellar.base.xdr.toXdrString
import simulateTransaction
import toULong
import zeroAccount

class TokenRepo(val rpc: SorobanClient, val config: RepoConfig) {
    suspend fun decimals(contractAddress: String): UInt {
        val tx = createSorobanTransaction(
            zeroAccount,
            contractAddress,
            "decimals",
            network = config.network
        )

        val result = rpc.simulateTransaction(tx.toEnvelopeXdr().toXdrString())

        return (SCVal.decodeFromString(result.results?.first()?.xdr!!) as SCVal.U32).value
    }

    suspend fun name(contractAddress: String): String {
        val tx = createSorobanTransaction(
            zeroAccount,
            contractAddress,
            "name",
            network = config.network
        )

        val result = rpc.simulateTransaction(tx.toEnvelopeXdr().toXdrString())

        return (SCVal.decodeFromString(result.results?.first()?.xdr!!) as SCVal.String).str.string
    }

    suspend fun balance(contractAddress: String, address: String): ULong /*i128*/ {
        return simulateTransaction(
            rpc,
            contractAddress,
            "balance",
            listOf(createAddressXdr(address)),
            network = config.network
        ).getOrNull().cast<SCVal.I128>().toULong()
    }

    suspend fun symbol(contractAddress: String): String {
        val tx = createSorobanTransaction(
            zeroAccount,
            contractAddress,
            "symbol",
            network = config.network
        )

        val result = rpc.simulateTransaction(tx.toEnvelopeXdr().toXdrString())

        return (SCVal.decodeFromString(result.results?.first()?.xdr!!) as SCVal.String).str.string
    }

    suspend fun transfer(
        source: Account,
        token: String,
        from: String,
        to: String,
        amount: ULong, // when will we ever need the full i128
        sign: (Transaction) -> TransactionBlob,
    ) = createPrepareAndSubmitTx(
        rpc,
        source,
        token,
        "transfer",
        listOf(createAddressXdr(from), createAddressXdr(to), SCVal.I128(Int128Parts(0L, amount))),
        config.network,
        sign
    )

//    suspend fun assetInfo(contractAddress: String): SorobanAssetInfo = run {
//        parZip(
//            {symbol(contractAddress)},
//            {name(contractAddress)},
//            {decimals(contractAddress)}
//        ){symbol, name, decimals ->
//            SorobanAssetInfo(symbol, name, decimals, contractAddress)
//        }
//    }

}