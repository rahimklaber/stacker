import com.ionspin.kotlin.bignum.decimal.BigDecimal
import me.rahimklaber.stellar.base.Network
import me.rahimklaber.stellar.sorobanClient
import token.TokenRepo
import vault.aqua_lp_stacker.AquaLpStackerRepo
import walletkit.createWalletKit

val rpcAddress = if(Config.network == Network.TESTNET){
    "https://soroban-testnet.stellar.org"
}else{
    "https://soroban-rpc.creit.tech/"
}

val rpcClient = sorobanClient(rpcAddress)
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