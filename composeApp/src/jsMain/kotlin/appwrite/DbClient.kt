package appwrite

import kotlinx.browser.window
import org.w3c.fetch.Response
import kotlin.apply
import kotlin.js.Promise


class QueryBuilder(
    private val dbClient: DbClient,
    private val collectionId: String,
    private val projectId: String,
){
    private val filters = mutableListOf<String>()

    fun equalTo(column:String, value: String) = apply {
        val query = """
            {"method":"equal","attribute":"$column","values":["$value"]}
        """.trimIndent()
        filters.add(query)
    }

    fun call(): Promise<Response> {
        val requestInit = js("{}")

        requestInit.headers = js("{}")
        requestInit.headers["X-Appwrite-Project"] = projectId

        val queries = StringBuilder()

        filters.forEach {
            queries.append("queries[]=")
            queries.append(it)
            queries.append("&")
        }

        return window.fetch("${dbClient.host}/v1/databases/${dbClient.databaseId}/collections/${collectionId}/documents?$queries", requestInit)

    }
}

class DbClient(
    val projectId: String,
    val databaseId: String,
    val host: String, // http://localhost
) {

    fun query(
        collectionId: String
    ): QueryBuilder = QueryBuilder(this, collectionId, projectId)

}