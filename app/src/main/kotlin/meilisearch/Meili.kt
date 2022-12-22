package dev.triumphteam.docsly.meilisearch

import dev.triumphteam.docsly.meilisearch.annotation.PrimaryKey
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.resources.Resource
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

/** A client to handle connection between the application and https://www.meilisearch.com/. */
public class Meili(
    host: String,
    port: Int,
    protocol: URLProtocol,
    private val apiKey: String = "",
) {

    public val client: HttpClient = HttpClient(CIO) {
        install(Resources)
        install(Auth) { api(apiKey) } // Auto setup authentication
        install(ContentNegotiation) { json() } // Using Kotlin serialization for content negotiation

        defaultRequest {
            this.host = host
            this.port = port
            url {
                this.protocol = protocol
            }
        }
    }

    /** Gets an index, doesn't always mean the index exists within Meili. */
    public fun index(
        uid: String,
        primaryKey: String? = null,
        searchableAttributes: List<String>? = null,
    ): Index = Index(uid, primaryKey, searchableAttributes)

    /** Representation of an index. Contains the needed operations with the index. */
    public inner class Index(
        public val uid: String,
        public val primaryKey: String?,
        private val searchableAttributes: List<String>? = null,
    ) {

        /** Create the index. Success even if it already exists. */
        public suspend fun create(): HttpResponse = client.post(Indexes()) {
            setBody(Create(uid, primaryKey, searchableAttributes))
        }

        /** Deletes the current index. Success even if it doesn't exist. */
        public suspend fun delete(): HttpResponse = client.delete(Indexes.Uid(uid = uid))

        /** Search for specific content in the index. */
        public suspend inline fun <reified T> search(query: String, filter: Map<String, String>): List<T> {
            return searchFull<T>(query, filter).hits
        }

        /** [search] but returns all the data ([SearchResult]) provided by the search. */
        public suspend inline fun <reified T> searchFull(query: String, filter: Map<String, String>): SearchResult<T> {
            // TODO: Handle errors
            return client.post(Indexes.Uid.Search(Indexes.Uid(uid = uid))) {
                setBody(SearchRequest(query, filter))
            }.body()
        }

        /** Add documents to the index. Creates a new index if none exists. */
        public suspend inline fun <reified T> addDocuments(
            documents: List<T>,
            primaryKey: String? = null,
        ): HttpResponse {
            val pk = primaryKey ?: this.primaryKey ?: run {
                T::class.java.constructors.firstOrNull()?.parameters?.find { it.isAnnotationPresent(PrimaryKey::class.java) }?.name
            }

            return client.post(Indexes.Uid.Documents(Indexes.Uid(uid = uid))) {
                contentType(ContentType.Application.Json) // Json body
                parameter(PRIMARY_KEY_PARAM, pk)
                setBody(documents)
            }
        }

        /** Transfers all the documents from this index into the passed [index], and deletes this. */
        public suspend fun transferTo(index: Index): HttpResponse {
            val createResponse = index.create()

            // If there was an error creating the new index we return the response
            if (!createResponse.status.isSuccess()) return createResponse

            val swapResponse = client.post(Indexes.Swap()) {
                setBody(listOf(Swap(listOf(uid, this@Index.uid))))
            }

            // If there was an error swapping the new indexes we return the response
            if (!swapResponse.status.isSuccess()) return swapResponse

            // Then we delete the current index
            return delete()
        }
    }

    /** Resource location for "$url/indexes". */
    @Serializable
    @Resource("/indexes")
    public class Indexes {

        /** Resource location for "$url/indexes/[uid]". */
        @Serializable
        @Resource("{uid}")
        public class Uid(public val parent: Indexes = Indexes(), public val uid: String) {

            /** Resource location for "$url/indexes/[uid]/search". */
            @Serializable
            @Resource("/search")
            public class Search(public val parent: Uid)

            /** Resource location for "$url/indexes/[uid]/documents". */
            @Serializable
            @Resource("/documents")
            public class Documents(public val parent: Uid)
        }

        /** Resource location for "$url/indexes/swap-indexes". */
        @Serializable
        @Resource("/swap-indexes")
        public class Swap(public val parent: Indexes = Indexes())
    }

    /** Serializable class for the create end point. */
    @Serializable
    public data class Create(
        val uid: String,
        val primaryKey: String?,
        val searchableAttributes: List<String>?,
    )

    /** Serializable class for the search result from end point. */
    @Serializable
    public data class SearchResult<T>(
        val hits: List<T>,
        val query: String,
        val processingTimeMs: Long,
        val limit: Int,
        val offset: Int,
        val estimatedTotalHits: Int,
    )

    /** Serializable class for the search end point. */
    @Serializable
    public data class SearchRequest(
        public val q: String,
        public val filter: Map<String, String>,
    )

    /** Serializable class for the swap end point. */
    @Serializable
    public data class Swap(public val indexes: List<String>)

    public companion object {

        public const val QUERY_PARAM: String = "q"
        public const val PRIMARY_KEY_PARAM: String = "primaryKey"
    }
}
