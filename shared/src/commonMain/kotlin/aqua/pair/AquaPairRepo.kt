package aqua.pair

import arrow.core.Either
import cast
import createAddressXdr
import createU128
import me.rahimklaber.stellar.SorobanClient
import me.rahimklaber.stellar.base.xdr.soroban.SCVal
import simulateTransaction
import toList
import toScVec
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

    /**
     * helper method to know how much of the underlying tokens you are entitled to
     */
    suspend fun depositBalance(address: String, amountOfShares: ULong): Either<Throwable, Pair<ULong, ULong>>{
        return simulateTransaction(
            rpc,
            config.contract,
            "withdraw",
            listOf(createAddressXdr(address), createU128(amountOfShares), listOf<SCVal>(createU128(0u), createU128(0u)).toScVec()),
            config.network
        ).map {
            val (r1, r2) = it.cast<SCVal.Vec>().toList().map{ it.cast<SCVal.U128>().toULong() }
            r1 to r2
        }
    }
}