package walletkit

import me.rahimklaber.stellar.base.Network

fun createModalParams(onWalletSelected:  (ISupportedWallet) -> Unit): ModalParams {
    val modalParams: ModalParams = js("{}")

    modalParams.modalTitle = "Test"

    modalParams.onWalletSelected = onWalletSelected
    modalParams.onClosed = null
    modalParams.notAvailableText = null

    return modalParams
}

private fun createWalletConnectModule(): WalletConnectModule {
    val params: IWalletConnectConstructorParams = js("{}")

    params.projectId = "b396959c630c373d96c741dc8828b7c6"
    params.url = "https://stacker.rahimklaber.me"
    params.description = "Compounding vaults on Soroban"
    params.name = "Stacker"
    params.method ="stellar_signXDR"
    params.network = Config.network.networkPassphrase
    params.icons = arrayOf("https://placehold.co/50")

    return WalletConnectModule(params)
}

fun createWalletKit(): StellarWalletsKit {
    val params = js("{}")


    params.modules = allowAllModules().toMutableList().apply { add(createWalletConnectModule()) }.toTypedArray()
    params.selectedWalletId = "freighter"
    params.network = Config.network.networkPassphrase

    return StellarWalletsKit(params)
}