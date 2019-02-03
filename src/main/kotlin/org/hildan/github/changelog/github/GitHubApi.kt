package org.hildan.github.changelog.github

import org.hildan.github.changelog.builder.Issue
import org.hildan.github.changelog.builder.Tag
import org.hildan.github.changelog.builder.User
import org.kohsuke.github.GHFileNotFoundException
import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHTag
import org.kohsuke.github.GHUser
import org.kohsuke.github.GitHub
import org.kohsuke.github.HttpException

data class GitHubConfig(
    val user: String,
    val repo: String,
    val token: String? = null
) {
    val releaseUrlTemplate: String = "https://github.com/$user/$repo/tree/%s"
    val diffUrlTemplate: String = "https://github.com/$user/$repo/compare/%s...%s"
}

data class Repository(val tags: List<Tag>, val closedIssues: List<Issue>)

fun fetchRepositoryInfo(gitHubConfig: GitHubConfig): Repository {
    val ghRepository = gitHubConfig.fetchGHRepository()
    val tags = ghRepository.listTags().map { it.toTag() }
    val closedIssues = ghRepository.getIssues(GHIssueState.CLOSED).map { it.toIssue() }
    return Repository(tags, closedIssues)
}

private fun GitHubConfig.fetchGHRepository(): GHRepository {
    try {
        return connect().getRepository("$user/$repo")
    } catch (e: HttpException) {
        throw GitHubConfigException("Could not connect to GitHub: ${e.cause?.message}")
    } catch (e: GHFileNotFoundException) {
        throw GitHubConfigException("Could not find repository: ${e.cause?.message}")
    }
}

private fun GitHubConfig.connect(): GitHub = if (token == null) {
    GitHub.connectAnonymously()
} else {
    GitHub.connectUsingPassword(user, token)
}

private fun GHTag.toTag(): Tag = Tag(name, commit.commitDate.toInstant())

private fun GHIssue.toIssue(): Issue = Issue(
    number = number,
    title = title,
    closedAt = closedAt.toInstant(),
    labels = labels.map { it.name },
    url = htmlUrl.toString(),
    author = user.toUser(),
    isPullRequest = isPullRequest
)

private fun GHUser.toUser(): User = User(login, htmlUrl.toString())

class GitHubConfigException(message: String) : RuntimeException(message)
