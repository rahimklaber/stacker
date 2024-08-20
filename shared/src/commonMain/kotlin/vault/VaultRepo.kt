package vault

import ResultWithHash
import arrow.core.Either
import cast
import createAddressXdr
import createI128
import createPrepareAndSubmitTx
import me.rahimklaber.stellar.SorobanClient
import me.rahimklaber.stellar.base.Account
import me.rahimklaber.stellar.base.xdr.soroban.SCVal
import simulateTransaction
import toULong

interface VaultRepo {
    suspend fun deposit(
        source: Account,
        depositor: String,
        amount: ULong, // i128
        sign: SignFun
    ): Either<Throwable, ResultWithHash<ULong>> /*i128*/

    suspend fun balance(
        depositor: String,
    ): Either<Throwable, ULong>

    suspend fun withdraw(
        source: Account,
        depositor: String,
        amount: ULong, // i128
        sign: SignFun
    ): Either<Throwable, ResultWithHash<ULong>> /*i128*/
}

class RpcVaultRepo(
    val rpc: SorobanClient,
    val config: RepoConfig
): VaultRepo {
    override suspend fun deposit(
        source: Account,
        depositor: String,
        amount: ULong, // i128
        sign: SignFun
    ): Either<Throwable, ResultWithHash<ULong>> /*i128*/{
        return createPrepareAndSubmitTx(
           rpc,
            source,
            config.contract,
            "deposit",
            listOf(createAddressXdr(depositor), createI128(amount)),
            config.network,
            sign
        ).map {
            ResultWithHash(it.first.cast<SCVal.I128>().toULong(), it.second)
        }
    }

    override suspend fun balance(
        depositor: String,
    ): Either<Throwable, ULong> {
        return simulateTransaction(
            rpc,
            config.contract,
            "balance",
            listOf(createAddressXdr(depositor)),
            config.network
        ).map {
            it.cast<SCVal.I128>().toULong()
        }
    }

    override suspend fun withdraw(
        source: Account,
        depositor: String,
        amount: ULong, // i128
        sign: SignFun
    ): Either<Throwable, ResultWithHash<ULong>> /*i128*/{
        return createPrepareAndSubmitTx(
            rpc,
            source,
            config.contract,
            "withdraw",
            listOf(createAddressXdr(depositor), createI128(amount)),
            config.network,
            sign
        ).map {
            ResultWithHash(it.first.cast<SCVal.I128>().toULong(), it.second)
        }
    }
}