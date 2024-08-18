package vault.aqua_lp_stacker

import ResultWithHash
import arrow.core.Either
import bytesFromHex
import cast
import createAddressXdr
import createI128
import createPrepareAndSubmitTx
import createSymbol
import createU128
import createU32
import me.rahimklaber.stellar.SorobanClient
import me.rahimklaber.stellar.base.Account
import me.rahimklaber.stellar.base.xdr.soroban.SCMap
import me.rahimklaber.stellar.base.xdr.soroban.SCMapEntry
import me.rahimklaber.stellar.base.xdr.soroban.SCVal
import toULong
import vault.RpcVaultRepo
import vault.VaultRepo

data class StackerConfig(
    val feeRecipient: String,
    val feeBps: UInt,
    val rewardToken: String,
    val pairSellReward: String?,
    val pair: String,
){
    fun toScVal(): SCVal.Map {
        val pairSellRewardVal = pairSellReward?.let { createAddressXdr(it) } ?: SCVal.Void

        return SCVal.Map(
            SCMap(
                listOf(
                    SCMapEntry(createSymbol("fee_bps"), createU32(feeBps)),
                    SCMapEntry(createSymbol("fee_recipient"), createAddressXdr(feeRecipient)),
                    SCMapEntry(createSymbol("pair"), createAddressXdr(pair)),
                    SCMapEntry(createSymbol("pair_sell_reward"), pairSellRewardVal),
                    SCMapEntry(createSymbol("reward_token"), createAddressXdr(rewardToken)),

                )
            )
        )
    }
}

data class KeeperArgs(
    val inIdx0: UInt,
    val outAmount0: ULong, /*u128*/

    val inIdx1: UInt,
    val outAmount1: ULong, /*u128*/

    val inToken1: String? = null,
){
    fun toScVal(): SCVal.Map {

        val tokenEncoded = if(inToken1 == null){
            SCVal.Void
        }else{
            createAddressXdr(inToken1)
        }

        return SCVal.Map(
            SCMap(
                listOf(
                    SCMapEntry(createSymbol("in_idx_0"), createU32(inIdx0)),
                    SCMapEntry(createSymbol("in_idx_1"), createU32(inIdx1)),
                    SCMapEntry(createSymbol("in_token_1"),tokenEncoded ),
                    SCMapEntry(createSymbol("out_amount_0"), createU128(outAmount0)),
                    SCMapEntry(createSymbol("out_amount_1"), createU128(outAmount1)),
                )
            )
        )
    }
}

class AquaLpStackerRepo(
    val rpc: SorobanClient,
    val repoConfig: RepoConfig
): VaultRepo by RpcVaultRepo(rpc, repoConfig) {

    suspend fun init(
        source: Account,
        stackerConfig: StackerConfig,
        depositToken: String,
        leeWay: ULong, // i128
        sign: SignFun
    ): Either<Throwable, ResultWithHash<Unit>> {
        return createPrepareAndSubmitTx(
            rpc,
            source,
            repoConfig.contract,
            "init",
            listOf(stackerConfig.toScVal(), createAddressXdr(depositToken), createI128(leeWay)),
            repoConfig.network,
            sign
        ).map {ResultWithHash(Unit, it.second)}
    }

    suspend fun keeper(
        source: Account,
        args: KeeperArgs,
        sign: SignFun
    ): Either<Throwable, ResultWithHash<ULong>/*i128*/> {
        return createPrepareAndSubmitTx(
            rpc,
            source,
            repoConfig.contract,
            "keeper",
            listOf(args.toScVal()),
            repoConfig.network,
            sign
        ).map { ResultWithHash(it.first.cast<SCVal.I128>().toULong(), it.second) }
    }

    suspend fun claimFee(
        source: Account,
        amount: ULong,
        sign: SignFun
    ): Either<Throwable, ResultWithHash<Unit>/*i128*/> {
        return createPrepareAndSubmitTx(
            rpc,
            source,
            repoConfig.contract,
            "claim_fee",
            listOf(createI128(amount)),
            repoConfig.network,
            sign
        ).map { ResultWithHash(Unit, it.second) }
    }

    suspend fun upgrade(
        source: Account,
        newWasmHash: String,
        sign: SignFun
    ): Either<Throwable, ResultWithHash<Unit>> {
        return createPrepareAndSubmitTx(
            rpc,
            source,
            repoConfig.contract,
            "upgrade",
            listOf(bytesFromHex(newWasmHash)),
            repoConfig.network,
            sign
        ).map { ResultWithHash(Unit, it.second) }
    }
}