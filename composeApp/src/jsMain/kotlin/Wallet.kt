import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import walletkit.createModalParams
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.js.Promise



var connectedAddress: String? by mutableStateOf(null)
    private set

val isConnected by derivedStateOf {
    connectedAddress != null
}

fun connectWallet(){
   walletKit.openModal(createModalParams {
       GlobalScope.launch {
           walletKit.setWallet(it.id)
           connectedAddress = walletKit.getAddress().await().address
       }
   })
}

fun disconnectWallet(){
    connectedAddress = null
}