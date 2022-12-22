package dev.triumphteam.docsly.meilisearch

import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.AuthProvider
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.auth.AuthScheme
import io.ktor.http.auth.HttpAuthHeader

public fun Auth.api(
    token: String,
    sendWithoutRequestCallback: (HttpRequestBuilder) -> Boolean = { true },
    realm: String? = null,
) {
    providers.add(ApiAuthProvider(token, sendWithoutRequestCallback, realm))
}

public class ApiAuthProvider(
    private val token: String,
    private val sendWithoutRequestCallback: (HttpRequestBuilder) -> Boolean = { true },
    private val realm: String?,
) : AuthProvider {

    @Suppress("OverridingDeprecatedMember")
    @Deprecated("Please use sendWithoutRequest function instead", ReplaceWith("sendWithoutRequest"))
    override val sendWithoutRequest: Boolean
        get() = error("Deprecated")

    override fun sendWithoutRequest(request: HttpRequestBuilder): Boolean = sendWithoutRequestCallback(request)

    /** Checks if current provider is applicable to the request. */
    override fun isApplicable(auth: HttpAuthHeader): Boolean {
        if (auth.authScheme != AuthScheme.Bearer) return false
        if (realm == null) return true
        if (auth !is HttpAuthHeader.Parameterized) return false

        return auth.parameter("realm") == realm
    }

    /** Adds an authentication method headers and credentials. */
    override suspend fun addRequestHeaders(request: HttpRequestBuilder, authHeader: HttpAuthHeader?) {
        request.headers {
            if (contains(HttpHeaders.Authorization)) remove(HttpHeaders.Authorization)
            append(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}
