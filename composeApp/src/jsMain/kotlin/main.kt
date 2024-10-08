import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.background
import com.varabyte.kobweb.compose.ui.modifiers.classNames
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.setVariable
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.InputVars
import com.varabyte.kobweb.silk.defer.renderWithDeferred
import com.varabyte.kobweb.silk.init.initSilkWidgets
import com.varabyte.kobweb.silk.prepareSilkFoundation
import com.varabyte.kobweb.silk.style.breakpoint.BreakpointUnitValue
import com.varabyte.kobweb.silk.theme.colors.ColorScheme
import com.varabyte.kobweb.silk.theme.colors.ColorSchemes
import com.varabyte.kobweb.silk.theme.colors.palette.button
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.rahimklaber.stellar.base.KeyPair
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import ui.components.ComponentSurfaceStyle
import ui.components.VaultGridStyle
import ui.components.surfaceColor
import vault.ui.VaultList
import vault.ui.VaultPage
import walletkit.createModalParams


suspend fun main() {
    loadTokenList()
    while (!KeyPair.isInit) {
        delay(5)
    }
    var hash by mutableStateOf(window.location.hash)

    window.addEventListener("popstate", {
        hash = window.location.hash
//        console.log(hash)
    })

    renderComposable(rootElementId = "root") {
        prepareSilkFoundation(initSilk = { ctx ->
            initSilkWidgets(ctx) // REQUIRED

            ctx.theme.breakpoints = ctx.theme.breakpoints.copy(lg = BreakpointUnitValue.Px(1200.px))

            ctx.theme.palettes.light.surfaceColor = Color.rgb(254, 250, 224)
            ctx.theme.palettes.dark.surfaceColor = Color.rgb(254, 250, 224)

            ctx.theme.palettes.light.button.default = Colors.OrangeRed

            ctx.theme.registerStyle("surface-style", ComponentSurfaceStyle)
            ctx.theme.registerStyle("vault-grid", VaultGridStyle)

        }) {
            val scope = rememberCoroutineScope()

            renderWithDeferred {

                Column(
                    Modifier
                        .overflow(overflowY = Overflow.Auto , overflowX = Overflow.Hidden)
                        .fillMaxSize()
                        .setVariable(InputVars.BorderColor, Color.rgb(224, 224, 224))
                        .setVariable(InputVars.BorderFocusColor, Colors.Black),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(Modifier
                        .padding(leftRight = 10.px)
                        .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                    ) {
                        A("#", {classes("a-hidden")}){
                            H1(){
                                Text("Stacker")
                            }
                        }
                        Button(
                            {
                                scope.launch {
                                    if (isConnected) disconnectWallet()
                                    else connectWallet()
                                }

                            },
                            modifier = Modifier.margin(5.px),
                            colorScheme = ColorSchemes.Lime
                        ) {
                            if (isConnected) Text(addressPreview(connectedAddress!!))
                            else Text("Connect Wallet")
                        }
                    }

                    when {
                        hash.startsWith("#vault") -> VaultPage(hash.split("/")[1])
                        else -> VaultList()
                    }
                }
            }
        }
    }
}