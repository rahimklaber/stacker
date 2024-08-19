@file:JsModule("@creit.tech/stellar-wallets-kit/modules/walletconnect.module")
@file:JsNonModule
package walletkit

external interface IWalletConnectConstructorParams{
    var projectId: String
    var name: String
    var description: String
    var url: String
    var icons: Array<String>
    var method: dynamic
    var network: String
    var sessionId: String?
    var client: dynamic
    var modal: dynamic
}

external class WalletConnectModule (
    wcParams: IWalletConnectConstructorParams,
)