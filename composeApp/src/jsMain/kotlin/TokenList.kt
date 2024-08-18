import io.ktor.client.fetch.fetch
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import me.rahimklaber.stellar.base.xdr.Asset
import me.rahimklaber.stellar.base.xdr.contractId
import kotlin.collections.firstOrNull

@Serializable
data class TokenListAsset(
    val code: String,
    val issuer: String? = null,
    val contract: String,
    val name: String? = null,
    val org: String? = null,
    val domain: String? = null,
    val icon: String,
    val decimals: Int,
)

private var _tokenList: List<TokenListAsset>? = null
val tokenList: List<TokenListAsset>
    get() = _tokenList!!

suspend fun loadTokenList() {

    val map = Json.decodeFromString<JsonObject>(
        fetch(Config.tokenlistUrl)
            .await()
            .text()
            .await()
    )

    val tokens =
        Json.Default.decodeFromJsonElement<List<TokenListAsset>>(map["assets"]!!) as MutableList<TokenListAsset>

    val native = Asset.Native.contractId(Config.network)

    if (tokens.firstOrNull { it.contract == native } == null) {
        tokens.add(
            TokenListAsset(
                "XLM",
                null,
                native,
                "Stellar Lumens",
                "Stellar Development Foundation",
                "stellar.org",
                "https://assets.coingecko.com/coins/images/100/standard/Stellar_symbol_black_RGB.png",
                7
            )
        )
    }

    _tokenList = tokens
}