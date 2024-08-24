package vault.ui

import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import backendClient
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.style.vars.size.FontSizeVars
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Table
import org.jetbrains.compose.web.dom.Tbody
import org.jetbrains.compose.web.dom.Td
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Th
import org.jetbrains.compose.web.dom.Tr
import tokenList
import tokenRepo
import vault.DEFAULT_VAULTS
import vault.VaultData
import kotlin.text.iterator


@Composable
fun VaultList(vaults: List<VaultData> = DEFAULT_VAULTS) {



    Column(
        Modifier.fillMaxSize().padding(1.cssRem),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Table(
            Modifier
                .textAlign(TextAlign.Left)
                .width(700.px)
                .maxWidth(95.percent)
                .toAttrs()
        ) {
            Tr {
                Th {
                    Text("Vault")
                }
                Th {
                    Text("Apy")
                }
                Th {
                    Text("Tvl vault")
                }
            }

            Tbody {
                for (vault in vaults) {
                    var tvlInUsd by remember { mutableStateOf("???")}

                    LaunchedEffect(Unit) {
                        tvlInUsd = backendClient.getPriceOfToken(vault.token).getOrNull()?.let {

                            val amount = it * tokenRepo.balance(vault.token, vault.vaultContract).toFloat().div(10_000_000)

                            "$${amount.toString().split(".").first()}"
                        } ?: "???"
                    }
                    Tr {

                        Td {
                            A("#vault/${vault.vaultContract}", {classes("a-hidden")}) {
                                Row(
                                    Modifier
                                        .gap(5.px),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        Modifier.gap(1.px)
                                    ) {
                                        vault.tokens.forEach { token ->
                                            val url = tokenList.firstOrNull { it.contract == token }?.icon
                                                ?: ""
                                            Img(url, attrs = Modifier.width(1.5.cssRem).toAttrs { })
                                        }
                                    }
                                    Column(
                                        Modifier
                                            .gap(5.px)
                                    ) {
                                        Box { Text(vault.name) }
                                        Box(
                                            Modifier
                                                .fontSize(FontSizeVars.SM.value())
                                                .padding(2.px)
                                                .backgroundColor(Colors.Tan)
                                        ) {
                                            Text(vault.exchange)
                                        }
                                    }
                                }

                            }
                        }


                        Td {
                            Text("???")
                        }

                        Td {
                            Text(tvlInUsd)
                        }

                    }

                }
            }
        }
    }
}