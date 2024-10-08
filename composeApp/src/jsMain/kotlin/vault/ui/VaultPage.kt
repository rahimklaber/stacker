package vault.ui

import RepoConfig
import _10to7
import addressPreview
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import aqua.pair.AquaPairRepo
import calculateBalanceInUsd
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.varabyte.kobweb.compose.css.TextDecorationLine
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.ColumnScope
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxHeight
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textDecorationLine
import com.varabyte.kobweb.compose.ui.modifiers.whiteSpace
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.disclosure.Tabs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorSchemes
import connectedAddress
import getStellarExpertUrlForContract
import isConnected
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.rahimklaber.stellar.base.xdr.toXdrString
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import parseAmountAsULong
import parseULongtoString
import priceFetcher
import rpcClient
import stackerRepoFor
import tokenList
import tokenRepo
import ui.components.ComponentSurfaceStyle
import ui.components.InputField
import ui.components.ShowFun
import ui.components.TxSubmitModal
import ui.components.UpdateState
import ui.components.VaultGridStyle
import updateStateOnFailure
import vault.DEFAULT_VAULTS
import vault.VaultData
import vault.aqua_lp_stacker.AquaLpStackerRepo
import walletKit


@Composable
fun VaultPage(vaultContract: String) {
    val details = remember { DEFAULT_VAULTS.firstOrNull { it.vaultContract == vaultContract } }
    val scope = rememberCoroutineScope()

    //todo make this generic

    if (details == null) {
        Text("No vault found for $vaultContract")
        return
    }

    val pairRepo = remember { AquaPairRepo(rpcClient, RepoConfig(Config.network, details.pairContract)) }

    var reloadMarker by remember { mutableStateOf(0) }

    val stackerRepo = remember { stackerRepoFor(vaultContract) }

    var amountDeposited by remember { mutableStateOf("???") }
    var amountOfYourShares by remember { mutableStateOf("???") }
    var rewardsToBeClaimed: ULong? by remember { mutableStateOf(null) }

    var priceOfRewardToken: Float? by remember {  mutableStateOf(null) }

    val fee = remember {
        details.feeBps.toString().padStart(2, '0').toMutableList().apply {
            add(size - 2, '.')
            add('%')
        }.joinToString(separator = "")
    }

    val rewardTokenInfo = remember { tokenList.firstOrNull { it.contract == details.rewardToken }!! }

    LaunchedEffect(reloadMarker) {
        amountDeposited = parseULongtoString(tokenRepo.balance(details.token, details.vaultContract))
    }

    LaunchedEffect(Unit) {
        priceOfRewardToken = priceFetcher.priceOf(details.rewardToken)
        while (true) {
            rewardsToBeClaimed =
                pairRepo.rewardBalance(details.vaultContract).getOrNull()
            delay(10000)
        }
    }

    LaunchedEffect(connectedAddress, reloadMarker) {
        if (connectedAddress != null) {
            stackerRepo.balance(connectedAddress!!).getOrNull()?.let {
                amountOfYourShares = parseULongtoString(it)
            }
        }
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        H2 {
            Text(details.name)
        }
    }

    SimpleGrid(numColumns(1, lg = 2), VaultGridStyle.toModifier().fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {

            Column(
                Modifier
                    .fillMaxWidth(95.percent)
                    .gap(15.px),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Column(
                    ComponentSurfaceStyle.toModifier()
                        .padding(10.px)
                        .margin(15.px)
                        .fillMaxWidth()
                        .whiteSpace(WhiteSpace.PreWrap)
                ) {
                    H2 {
                        Text("Vault info")
                    }

                    Row {
                        Box {
                            Text("Vault contract: ")
                        }

                        A(getStellarExpertUrlForContract(details.vaultContract)) {
                            Text(addressPreview(details.vaultContract))
                        }
                    }

                    Row {
                        Box {
                            Text("Input token: ")
                        }

                        A(getStellarExpertUrlForContract(details.token)) {
                            Text(addressPreview(details.token))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Reward claimable by vault: ")
                        }

                        Row(Modifier.gap(3.px), verticalAlignment = Alignment.CenterVertically) {
                            Text(rewardsToBeClaimed?.let { reward ->
                                parseULongtoString(reward) + (priceOfRewardToken?.let { price ->
                                    " (${calculateBalanceInUsd(reward, price, true)})"
                                } ?: "")
                            } ?: "???")
                            Img(rewardTokenInfo.icon, attrs = Modifier.width(1.5.cssRem).toAttrs { })
                        }
                    }

                    Row {
                        Box {
                            Text("Tokens deposited: ")
                        }

                        Box {
                            Text(amountDeposited)
                        }
                    }

                    Row {
                        Box {
                            Text("Your share balance: ")
                        }

                        Box {
                            Text(amountOfYourShares)
                        }
                    }

                    Row {
                        Box {
                            Text("fee: ")
                        }

                        Box {
                            Text(fee)
                        }
                    }

                    A(details.linkToProtocol) { Text("Link to protocol") }
                }

                Column(
                    ComponentSurfaceStyle.toModifier()
                        .padding(10.px)
                        .margin(15.px)
                        .fillMaxWidth()
                        .whiteSpace(WhiteSpace.PreWrap)
                ) {
                    H2 {
                        Text("Strategy")
                    }

                    Box {
                        Text(details.description)
                    }
                }
            }


        }

        Box(
            Modifier
                .padding(10.px)
                .margin(15.px)
                .fillMaxWidth(), contentAlignment = Alignment.TopCenter
        ) {
            Column(
                Modifier
                    .fillMaxWidth(95.percent),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Forms(scope, stackerRepo, details) { reloadMarker++ }
            }
        }
    }
}

@Composable
private fun Forms(scope: CoroutineScope, stackerRepo: AquaLpStackerRepo, details: VaultData, reloadData: () -> Unit) {

    val modalStuff = TxSubmitModal(scope) {
        reloadData()
    }

    var amountOfShares by remember { mutableStateOf(0uL) }
    var amountOfTokens by remember { mutableStateOf(0uL) }


    LaunchedEffect(connectedAddress) {
        amountOfShares = connectedAddress?.let { stackerRepo.balance(it) }?.getOrNull() ?: 0uL
        amountOfTokens = connectedAddress?.let { tokenRepo.balance(details.token, it) } ?: 0uL

    }

    Tabs(ComponentSurfaceStyle.toModifier()) {
        TabPanel {
            Tab { Text("Deposit") }
            Panel {
                FormsLayout {
                    DepositForm(scope, stackerRepo, amountOfTokens, details, modalStuff)
                }
            }
        }

        TabPanel {
            Tab { Text("Withdraw") }
            Panel {
                FormsLayout {
                    WithdrawForm(scope, stackerRepo, amountOfShares, { amountOfShares = it }, details, modalStuff)
                }
            }
        }
    }


}

@Composable
private fun DepositForm(
    scope: CoroutineScope,
    stackerRepo: AquaLpStackerRepo,
    amountOfToken: ULong,
    details: VaultData,
    modalStuff: Pair<ShowFun, UpdateState>,
) {
    var amount by remember { mutableStateOf("") }

    val (toggleModal, updateState) = modalStuff

    Column {
        InputField("amount", amount, { input -> input.toFloatOrNull()?.let { amount = input } })
        Box(
            Modifier
                .textDecorationLine(TextDecorationLine.Underline)
                .onClick {
                    amount = BigDecimal.fromULong(amountOfToken).div(_10to7).toPlainString()
                }
        ) {
            Text(
                "max: ${
                    BigDecimal.fromULong(amountOfToken).div(_10to7).toPlainString()
                }"
            ) //todo read decimals from token contract
        }
    }

    Button(
        {
            scope.launch {
                toggleModal(true)
                updateStateOnFailure(updateState) {

                    val source = stackerRepo.rpc.getAccount(connectedAddress!!)
                    val amountParsed = parseAmountAsULong(amount)

                    val result = stackerRepo.deposit(source, source.accountId, amountParsed) {
                        walletKit.signTransaction(it.toEnvelopeXdr().toXdrString()).await().signedTxXdr
                    }

                    result.fold({ throw it }) { it.hash }

                    result.fold({ throw it }) { it.hash }
                }
            }
        },
        colorScheme = ColorSchemes.BlueGray,
        enabled = isConnected,
    ) {
        Text("Deposit")
    }
}

@Composable
private fun WithdrawForm(
    scope: CoroutineScope,
    stackerRepo: AquaLpStackerRepo,
    amountOfShares: ULong,
    updateAmountOfShares: (ULong) -> Unit,
    details: VaultData,
    modalStuff: Pair<ShowFun, UpdateState>,
) {
    var amount by remember { mutableStateOf("") }

    val (toggleModal, updateState) = modalStuff

    Column {
        InputField("amount", amount, { input -> input.toFloatOrNull()?.let { amount = input } })
        Box(
            Modifier
                .textDecorationLine(TextDecorationLine.Underline)
                .onClick {
                    amount = BigDecimal.fromULong(amountOfShares).div(_10to7).toPlainString()
                }
        ) {
            Text(
                "max: ${
                    BigDecimal.fromULong(amountOfShares).div(_10to7).toPlainString()
                }"
            ) //todo read decimals from token contract
        }
    }

    Button(
        {
            scope.launch {
                toggleModal(true)
                updateStateOnFailure(updateState) {

                    val source = stackerRepo.rpc.getAccount(connectedAddress!!)
                    val amountParsed = parseAmountAsULong(amount)

                    val result = stackerRepo.withdraw(source, source.accountId, amountParsed) {
                        walletKit.signTransaction(it.toEnvelopeXdr().toXdrString()).await().signedTxXdr
                    }

                    stackerRepo.balance(connectedAddress!!).getOrNull()?.let {
                        updateAmountOfShares(it)
                    }

                    result.fold({ throw it }) { it.hash }
                }
            }
        },
        colorScheme = ColorSchemes.BlueGray,
        enabled = isConnected,
    ) {
        Text("Withdraw")
    }
}

@Composable
private fun FormsLayout(content: @Composable (ColumnScope.() -> Unit)) = Column(
    Modifier
        .width(300.px)
        .maxWidth(95.percent)
        .height(250.px)
        .maxHeight(95.percent)
        .padding(5.px)
        .gap(35.px),
    content = content
)
