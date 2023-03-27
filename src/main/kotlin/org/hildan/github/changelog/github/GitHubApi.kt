package org.hildan.github.changelog.github

import com.expediagroup.graphql.client.ktor.*
import kotlinx.coroutines.flow.*
import org.hildan.github.changelog.builder.Issue
import org.hildan.github.changelog.builder.Milestone
import org.hildan.github.changelog.builder.Tag
import org.hildan.github.changelog.builder.User
import org.hildan.github.graphql.*
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

suspend fun fetchRepositoryInfo(gitHubConfig: GitHubConfig): Repository {
    val gitHubClient = GitHubGraphQLClient(gitHubConfig.token ?: error("GitHub token is required"))

    logger.info("Fetching tags...")
    val tags = ghRepository.listTags().map { it.toTag() }
    logger.info("${tags.size} tags found")

    logger.info("Fetching closed issues...")
    val closedIssues = gitHubClient.closedIssuesFlow(repo = gitHubConfig.repo, owner = gitHubConfig.user)
        .map { it.toIssue() }
        .toList()
    logger.info("${closedIssues.size} closed issues found")

    logger.info("Fetching merged pull-requests...")
    val mergedPullRequests = gitHubClient.mergedPRsFlow(repo = gitHubConfig.repo, owner = gitHubConfig.user)
        .map { it.toIssue() }
        .toList()
    logger.info("${mergedPullRequests.size} merged pull-requests found")

    val firstCommit = ghRepository.listCommits().withPageSize(1000).last()
    return Repository(tags, closedIssues + mergedPullRequests, firstCommit.shA1)
}

private fun GraphQLKtorClient.closedIssuesFlow(repo: String, owner: String) =
    paginatedFlow { lastEndCursor ->
        val data = executeOrThrow(
            GetClosedIssues(
                variables = GetClosedIssues.Variables(
                    repo = repo,
                    owner = owner,
                    first = 100,
                    after = lastEndCursor,
                )
            )
        )
        PaginatedData(data.repository?.issues?.pageInfo, data.repository?.issues?.nodes?.filterNotNull() ?: emptyList())
    }

private fun GraphQLKtorClient.mergedPRsFlow(repo: String, owner: String) =
    paginatedFlow { lastEndCursor ->
        val data = executeOrThrow(
            GetMergedPRs(
                variables = GetMergedPRs.Variables(
                    repo = repo,
                    owner = owner,
                    first = 100,
                    after = lastEndCursor,
                )
            )
        )
        PaginatedData(data.repository?.pullRequests?.pageInfo, data.repository?.pullRequests?.nodes?.filterNotNull() ?: emptyList())
    }

private fun GHTag.toTag(): Tag = Tag(name, commit.commitDate.toInstant())

private fun org.hildan.github.graphql.getclosedissues.Issue.toIssue(): Issue = Issue(
    number = number,
    title = title,
    body = body,
    closedAt = closedAt ?: error("Issue $number is expected to be closed"),
    labels = labels?.nodes?.map { it!!.name } ?: emptyList(),
    url = url,
    author = author?.toUser() ?: error("Author is missing in issue $number"),
    isPullRequest = false,
    milestone = milestone?.toMilestone(),
)

private fun org.hildan.github.graphql.getmergedprs.PullRequest.toIssue(): Issue = Issue(
    number = number,
    title = title,
    body = body,
    closedAt = closedAt ?: error("Issue $number is expected to be closed"),
    labels = labels?.nodes?.map { it!!.name } ?: emptyList(),
    url = url,
    author = author?.toUser() ?: error("Author is missing in issue $number"),
    isPullRequest = false,
    milestone = milestone?.toMilestone(),
)

private fun org.hildan.github.graphql.getclosedissues.Milestone.toMilestone() = Milestone(title, description)

private fun org.hildan.github.graphql.getmergedprs.Milestone.toMilestone() = Milestone(title, description)

private fun org.hildan.github.graphql.getclosedissues.Actor.toUser(): User = User(login, url)

private fun org.hildan.github.graphql.getmergedprs.Actor.toUser(): User = User(login, url)

class GitHubConfigException(message: String) : RuntimeException(message)
