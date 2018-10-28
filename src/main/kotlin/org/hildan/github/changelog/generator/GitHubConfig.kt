package org.hildan.github.changelog.generator

import org.kohsuke.github.GHFileNotFoundException
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.HttpException

data class GitHubConfig(
    private val user: String,
    private val token: String?,
    private val repo: String,
    private val baseUrl: String = "https://github.com/$user/$repo",
    private val releaseUrl: String = "$baseUrl/tree/%s"
) {

    fun releaseUrl(tag: String): String = String.format(releaseUrl, tag)

    fun changelogUrl(fromTag: String, toTag: String): String = "$baseUrl/compare/$fromTag...$toTag"

    fun fetchRepositoryInfo(): GHRepository {
        try {
            val github = connect()
            val repoSlug = "$user/$repo"
            return github.getRepository(repoSlug) ?: throw IllegalArgumentException("repo not found")
        } catch (e: HttpException) {
            throw GitHubConfigException("Could not connect to GitHub: ${e.cause?.message}")
        } catch (e: GHFileNotFoundException) {
            throw GitHubConfigException("Could not find repository: ${e.cause?.message}")
        }
    }

    private fun connect() = if (token == null) {
        GitHub.connectAnonymously()
    } else {
        GitHub.connectUsingPassword(user, token)
    }
}

class GitHubConfigException(message: String) : RuntimeException(message)
