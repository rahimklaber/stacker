package ui.components

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.autoLength
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.toAttrs
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.dom.Div
import kotlin.let
import kotlin.to

typealias ModalComposableFun = (@Composable (ShowFun) -> Unit)


typealias ShowFun = (Boolean) -> Unit
@Composable
fun Modal(modifier: Modifier = Modifier, modalContent: ModalComposableFun? = null): Pair<ShowFun, (ModalComposableFun?) -> Unit> {
    var show by remember { mutableStateOf(false) }
    var content: ModalComposableFun? by remember {
        mutableStateOf(
            modalContent
        )
    }

    val showFun = { shouldShow: Boolean ->
        show = shouldShow
    }

    Div({
        classes("custom_modal")
        if (show)
            classes("d-flex")
        else
            classes("d-none")

    }) {
        Div(
            modifier
                .fillMaxWidth()
                .margin(topBottom = 2.cssRem, leftRight = autoLength)
                .toAttrs {}
        ) {
            content?.let { it(showFun) }
        }
    }

    return showFun to { f: ModalComposableFun? ->
        content = f
    }
}