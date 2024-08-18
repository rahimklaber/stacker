import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.await
import kotlin.js.Promise

@JsModule("@stellar/freighter-api")
@JsNonModule
external object Freighter{
    fun isConnected(): Promise<Boolean>
    fun isAllowed(): Promise<Boolean>
    fun setAllowed(): Promise<Boolean>
    fun getPublicKey(): Promise<String>
    //    fun getUserInfo(): Promise<UserInfo>
    fun getNetwork(): Promise<String>
    fun signTransaction(transactionXdr: String, network: String = definedExternally, publicKey: String? = definedExternally): Promise<String>
    fun signAuthEntry(entryXdr: String): Promise<String>
}

var connectedAddress: String? by mutableStateOf(null)
    private set

val isConnected by derivedStateOf {
    connectedAddress != null
}

suspend fun connectWallet(){
    Freighter
        .getPublicKey()
        .then {
            connectedAddress = it
        }.await()
}

fun disconnectWallet(){
    connectedAddress = null
}