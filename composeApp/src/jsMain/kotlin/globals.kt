import appwrite.DbClient
import backend.PriceFetcher
import backend.StackerBackend
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import me.rahimklaber.stellar.base.Network
import me.rahimklaber.stellar.horizon.Server
import me.rahimklaber.stellar.sorobanClient
import token.TokenRepo
import vault.aqua_lp_stacker.AquaLpStackerRepo
import walletkit.createWalletKit

//TODO: Add ability to change the configs. Localstorage?
val rpcAddress = if(Config.network == Network.TESTNET){
    "https://soroban-testnet.stellar.org"
}else{
    "https://soroban-rpc.creit.tech/"
}

val rpcClient = sorobanClient(rpcAddress)
val horizon = Server("https://horizon.stellar.org")

val tokenRepo = TokenRepo(rpcClient, RepoConfig(Config.network, ""))

val walletKit = createWalletKit()



val _10to7 = BigDecimal.fromInt(1_000_000_0)

private val mutableMap = mutableMapOf<String, AquaLpStackerRepo>()

fun stackerRepoFor(contract: String): AquaLpStackerRepo {
    return mutableMap[contract] ?: run {
        AquaLpStackerRepo(rpcClient, RepoConfig(Config.network, contract))
            .also {
                mutableMap[contract] = it
            }
    }
}



// app write

val backendClient = StackerBackend(
    DbClient(Config.appwriteProjectId, "66c7693b00297a8d03fe", Config.appwriteUrl),
    mapOf(
        "price_in_usd" to "66c769440038bad0e133"
    )
)

val priceFetcher = PriceFetcher(backendClient)