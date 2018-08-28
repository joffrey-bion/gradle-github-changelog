package org.hildan.github.changelog.generator

import org.kohsuke.github.GHFileNotFoundException
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.HttpException

interface GitHubConfig {
    val user: String
    val token: String
    val repo: String

    fun fetchRepositoryInfo(): GHRepository {
        try {
            val github = GitHub.connectUsingPassword(user, token)
            val repoSlug = "$user/$repo"
            return github.getRepository(repoSlug) ?: throw IllegalArgumentException("repo not found")
        } catch (e: HttpException) {
            throw GitHubConfigException("Could not connect to GitHub: ${e.cause?.message}")
        } catch (e: GHFileNotFoundException) {
            throw GitHubConfigException("Could not find repository: ${e.cause?.message}")
        }
    }
}

class GitHubConfigException(message: String): RuntimeException(message)
