package aqua.pair

import arrow.core.Either
import cast
import createAddressXdr
import me.rahimklaber.stellar.SorobanClient
import me.rahimklaber.stellar.base.xdr.soroban.SCVal
import simulateTransaction
import toULong

class AquaPairRepo(
    val rpc: SorobanClient,
    val config: RepoConfig
) {
    suspend fun rewardBalance(
        address: String,
    ): Either<Throwable, ULong>{
        return simulateTransaction(
            rpc,
            config.contract,
            "get_user_reward",
            listOf(createAddressXdr(address)),
            config.network
        ).map { it.cast<SCVal.U128>().toULong() }
    }
}