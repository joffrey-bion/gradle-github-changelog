package org.hildan.github.changelog.github

import org.hildan.github.changelog.builder.Issue
import org.hildan.github.changelog.builder.Milestone
import org.hildan.github.changelog.builder.Tag
import org.hildan.github.changelog.builder.User
import org.kohsuke.github.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("GithubApi")

data class GitHubConfig(
    val user: String,
    val repo: String,
    val token: String? = null,
) {
    val releaseUrlTemplate: String = "https://github.com/$user/$repo/tree/%s"
    val diffUrlTemplate: String = "https://github.com/$user/$repo/compare/%s...%s"
}

data class Repository(
    val tags: List<Tag>,
    val closedIssues: List<Issue>,
    val initialCommitSha: String,
)

fun fetchRepositoryInfo(gitHubConfig: GitHubConfig): Repository {
    val ghRepository = gitHubConfig.fetchGHRepository()

    logger.info("Fetching tags...")
    val tags = ghRepository.listTags().map { it.toTag() }
    logger.info("${tags.size} tags found")

    logger.info("Fetching closed issues...")
    val closedIssues = ghRepository.getIssues(GHIssueState.CLOSED)
        .filter { !it.isPullRequest }
        .map { it.toIssue() }
    logger.info("${closedIssues.size} closed issues found")

    logger.info("Fetching merged pull-requests...")
    val mergedPullRequests = ghRepository.getPullRequests(GHIssueState.CLOSED)
        .filter { it.isMerged }
        .map { it.toIssue() }
    logger.info("${mergedPullRequests.size} merged pull-requests issues found")

    val firstCommit = ghRepository.listCommits().withPageSize(1000).last()
    return Repository(tags, closedIssues + mergedPullRequests, firstCommit.shA1)
}

private fun GitHubConfig.fetchGHRepository(): GHRepository {
    try {
        val connect = connect()
        logger.info("Fetching repository info for $user/$repo...")
        return connect.getRepository("$user/$repo")
    } catch (e: HttpException) {
        throw GitHubConfigException("Could not connect to GitHub: ${e.cause ?: e}")
    } catch (e: GHFileNotFoundException) {
        throw GitHubConfigException("Could not find repository: ${e.cause ?: e }")
    }
}

private fun GitHubConfig.connect(): GitHub = when (token) {
    null -> {
        logger.warn("Connecting to GitHub anonymously (you may be subject to rate limiting)...")
        GitHub.connectAnonymously()
    }
    else -> {
        logger.info("Connecting to GitHub using token...")
        GitHub.connectUsingOAuth(token)
    }
}

private fun GHTag.toTag(): Tag = Tag(name, commit.commitDate.toInstant())

private fun GHIssue.toIssue(): Issue = Issue(
    number = number,
    title = title,
    body = body,
    closedAt = closedAt.toInstant(),
    labels = labels.map { it.name },
    url = htmlUrl.toString(),
    author = user.toUser(),
    isPullRequest = isPullRequest || this is GHPullRequest,
    milestone = milestone?.toMilestone(),
)

private fun GHMilestone.toMilestone() = Milestone(title, description)

private fun GHUser.toUser(): User = User(login, htmlUrl.toString())

class GitHubConfigException(message: String) : RuntimeException(message)
