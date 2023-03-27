package org.hildan.github.changelog

import com.expediagroup.graphql.client.ktor.*
import kotlinx.coroutines.*
import org.hildan.github.changelog.github.*
import org.hildan.github.graphql.*

fun main() {
    runBlocking {
        val githubToken = System.getenv("GITHUB_TOKEN")
        val client = GitHubGraphQLClient(githubToken)
        val issuesFlow = client.issuesFlow("krossbow", "joffrey-bion")
        issuesFlow.collect(::println)
    }
}
