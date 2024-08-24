package backend

import appwrite.DbClient
import arrow.core.raise.either
import kotlinx.coroutines.await

class StackerBackend(
    private val dbClient: DbClient,
    private val collectionToCollectionId: Map<String,String>
) {

    suspend fun getPriceOfToken(token: String) = either<Throwable, Float> {
        try {
            val result = dbClient.query(collectionToCollectionId["price_in_usd"]!!)
                .equalTo("token", token)
                .call()
                .await()
                .json()
                .await()
                .asDynamic()

            result.documents[0].price as Float
        }catch (e:Exception){
            raise(e)
        }
    }

}