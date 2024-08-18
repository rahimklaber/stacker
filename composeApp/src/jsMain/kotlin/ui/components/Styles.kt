package ui.components

import com.varabyte.kobweb.compose.css.StyleVariable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.Modifier.Companion
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.display
import com.varabyte.kobweb.compose.ui.modifiers.gridTemplateColumns
import com.varabyte.kobweb.silk.components.layout.SimpleGridKind
import com.varabyte.kobweb.silk.style.ComponentKind
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.colors.palette.MutablePalette
import com.varabyte.kobweb.silk.theme.colors.palette.Palette
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.fr
import org.jetbrains.compose.web.css.px

var MutablePalette.surfaceColor: Color
    get() = this.getValue("surface-color")
    set(value) { this["surface-color"] = value }

val Palette.surfaceColor: Color
    get() = this.getValue("surface-color")

val ComponentSurfaceStyle = CssStyle.base{
    Modifier
        .backgroundColor(colorMode.toPalette().surfaceColor)
        .borderRadius(10.px)
}


private val columnVariables = Breakpoint.entries.associateWith { breakpoint ->
    StyleVariable.NumberValue<Int>("simple-grid-col-count-${breakpoint.name.lowercase()}", prefix = "silk")
}


val VaultGridStyle = CssStyle<SimpleGridKind> {
    base {
        Modifier.display(DisplayStyle.Grid)
    }
        Breakpoint.LG {
            Modifier.gridTemplateColumns {
                    size(1.fr)
                    minmax(300.px, 400.px)
            }
        }
}