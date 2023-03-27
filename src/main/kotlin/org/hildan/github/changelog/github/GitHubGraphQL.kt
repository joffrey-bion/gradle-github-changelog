package org.hildan.github.changelog.github

import com.expediagroup.graphql.client.*
import com.expediagroup.graphql.client.ktor.*
import com.expediagroup.graphql.client.types.*
import io.ktor.client.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import kotlinx.coroutines.flow.*
import org.hildan.github.graphql.getclosedissues.*
import java.net.*

@Suppress("FunctionName")
fun GitHubGraphQLClient(githubToken: String): GraphQLKtorClient {
    val httpClient = HttpClient {
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(githubToken, "")
                }
            }
        }
    }
    return GraphQLKtorClient(url = URL("https://api.github.com/graphql"), httpClient)
}

fun <R> GraphQLClient<*>.paginatedFlow(
    requestPage: suspend GraphQLClient<*>.(lastEndCursor: String?) -> PaginatedData<R>,
) = flow {
    var lastEndCursor: String? = null
    var pageInfo: PageInfo
    do {
        val connection = requestPage(lastEndCursor)
        pageInfo = connection.pageInfo ?: break
        lastEndCursor = pageInfo.endCursor
        emitAll(connection.data.asFlow())
    } while (pageInfo.hasNextPage)
}

data class PaginatedData<T>(
    val pageInfo: PageInfo?,
    val data: List<T>
)

suspend fun <R : Any> GraphQLClient<*>.executeOrThrow(request: GraphQLClientRequest<R>): R = execute(request).orThrow()

fun <R> GraphQLClientResponse<R>.orThrow(): R {
    val gqlErrors = errors
    if (gqlErrors != null) {
        throw GraphQLClientException(gqlErrors)
    }
    return data ?: error("response data is null, yet no GraphQL errors were received")
}

class GraphQLClientException(errors: List<GraphQLClientError>) : Exception()
