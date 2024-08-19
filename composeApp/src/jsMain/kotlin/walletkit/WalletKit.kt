@file:JsModule("@creit.tech/stellar-wallets-kit")
@file:JsNonModule

package walletkit

import kotlin.js.Promise

external fun allowAllModules(): Array<dynamic>



external interface ISupportedWallet {
    val id: String
    val name: String
    val type: String
    val isAvailable: Boolean
    val icon: String
    val url: String
}

external interface ModalParams{
    var onWalletSelected:  (ISupportedWallet) -> Unit
    var onClosed: dynamic
    var modalTitle: String?
    var notAvailableText: String?
}

external interface SignTransactionOptions {
    val networkPassphrase: String?
    val address: String?
    val path: String?
    val submit: Boolean?
    val submitUrl: String?
}

external interface SignTransactionResult {
    val signedTxXdr: String
    val signerAddress: String?
}

external interface GetAddressResult {
    val address: String
}



external class StellarWalletsKit (
    params: dynamic = definedExternally,
){
    fun openModal(params: ModalParams): Promise<Unit>
    fun setWallet(id: String)

    fun getAddress(params: dynamic = definedExternally): Promise<GetAddressResult>
    fun signTransaction(xdr: String, opts: SignTransactionOptions? = definedExternally): Promise<SignTransactionResult>
}

