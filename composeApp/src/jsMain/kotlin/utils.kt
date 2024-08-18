import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.browser.window
import kotlinx.coroutines.await
import me.rahimklaber.stellar.base.Network
import ui.components.TxSubmitModalState
import ui.components.UpdateState

suspend fun signWtihWallet(tx: TransactionBlob, updateState: UpdateState) = run{
    val promise = Freighter.signTransaction(tx, Config.network.name())
    promise.catch { updateState(TxSubmitModalState.Failed(it.message ?: "unknown failure reason")) }.await()

    val blob = promise.await()

    blob
}

fun addressPreview(address: String) = address.take(5) + "..." + address.takeLast(4)

fun goToPage(path: String){
    window.location.hash = path
    console.log("page: $path")
}

fun reload(){
    val t = window.location.hash
    goToPage("#")
    goToPage(t)
}

fun getStellarExpertUrlForAccount(address: String, network: Network = Config.network) = run {
    val networkString = when (network) {
        Network.TESTNET -> "testnet"
        Network.PUBLIC -> "public"
        else -> error("Network not supported $network")
    }

    "https://stellar.expert/explorer/$networkString/account/$address"
}

fun getStellarExpertUrlForTransaction(txHash: String, network: Network = Config.network) = run {
    val networkString = when (network) {
        Network.TESTNET -> "testnet"
        Network.PUBLIC -> "public"
        else -> error("Network not supported $network")
    }

    "https://stellar.expert/explorer/$networkString/tx/$txHash"
}

fun getStellarExpertUrlForContract(contractId: String, network: Network = Config.network) = run {
    val networkString = when (network) {
        Network.TESTNET -> "testnet"
        Network.PUBLIC -> "public"
        else -> error("Network not supported $network")
    }

    "https://stellar.expert/explorer/$networkString/contract/$contractId"
}

suspend fun updateStateOnFailure(updateState: UpdateState, block: suspend () -> String) = run{
    updateState(TxSubmitModalState.Pending)
    try {
        updateState(TxSubmitModalState.Success(block.invoke()))
    }catch (e: Throwable){
        updateState(TxSubmitModalState.Failed(e.message ?: "unknown failure reason"))
    }
}

fun parseAmountAsULong(amount: String, decimals: Int = 7) : ULong{
    return (amount.toBigDecimal() * 10.toBigDecimal().pow(decimals)).toBigInteger().ulongValue()
}

fun parseULongtoString(amount: ULong, decimals: Int = 7): String {
    return  BigDecimal.fromULong(amount).div(BigDecimal.TEN.pow(decimals)).toPlainString()
}