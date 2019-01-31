package org.hildan.github.changelog.generator

import org.kohsuke.github.GHFileNotFoundException
import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHTag
import org.kohsuke.github.GHUser
import org.kohsuke.github.GitHub
import org.kohsuke.github.HttpException
import java.io.File

class GitHubChangelogGenerator(
    private val config: ChangelogConfig,
    private val formatter: MarkdownFormatter = MarkdownFormatter()
) {
    private val changeLogBuilder = ChangeLogBuilder(config)

    fun generate(outputFile: File? = null) {
        val repo = fetchRepositoryInfo()
        val changeLog = changeLogBuilder.createChangeLog(repo.closedIssues, repo.tags)
        val markdown = formatter.formatChangeLog(changeLog)
        if (outputFile != null) {
            outputFile.writeText(markdown)
        } else {
            print(markdown)
        }
    }

    private fun fetchRepositoryInfo(): Repository {
        val ghRepository = config.github.fetchGHRepository()
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
}

private data class Repository(val tags: List<Tag>, val closedIssues: List<Issue>)

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
