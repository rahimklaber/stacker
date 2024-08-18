package ui.components

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.silk.components.forms.Button
import getStellarExpertUrlForTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

sealed interface TxSubmitModalState{
    data object Pending : TxSubmitModalState
    data class Success(val hash: String? = null) : TxSubmitModalState
    data class Failed(val reason: String) : TxSubmitModalState
}

typealias TxSubmitModalComposableFun = (@Composable (ShowFun, TxSubmitModalState) -> Unit)
// prev -> new state
typealias UpdateState = (TxSubmitModalState) -> Unit
@Composable
fun TxSubmitModal(scope: CoroutineScope, onclose: suspend () -> Unit): Pair<ShowFun, UpdateState> {
    var state: TxSubmitModalState by remember{ mutableStateOf(TxSubmitModalState.Pending) }

    val (show, modalUpdate) = Modal(Modifier.color(Colors.NavajoWhite)){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when(state){
                is TxSubmitModalState.Failed -> Text("Failed: ${(state as TxSubmitModalState.Failed).reason}")
                TxSubmitModalState.Pending -> {
                    Div({classes("spinner-border")}) {  }

                    Text("Submitting transaction...")
                }
                is TxSubmitModalState.Success -> {
                    Text("success")
                    if((state as TxSubmitModalState.Success).hash != null){
                        A(getStellarExpertUrlForTransaction((state as TxSubmitModalState.Success).hash!!)){
                            Text("View on stellar expert")
                        }
                    }
                }
            }

            Button({
                scope.launch{
                    it(false)
                    onclose()
                }
            }){
                Text("close")
            }
        }
    }

    return Pair(show, {state = it})
}

