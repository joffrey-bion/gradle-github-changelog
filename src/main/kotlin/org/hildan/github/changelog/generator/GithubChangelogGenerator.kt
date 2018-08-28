package org.hildan.github.changelog.generator

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GHRepository
import java.time.Instant
import java.util.Date

class ArgsConfig(parser: ArgParser) : GitHubConfig {
    override val user by parser.storing("-u", "--user", help = "username of the owner of the target GitHub repo")
    override val token by parser.storing("-t", "--token", help = "the GitHub API key")
    override val repo by parser.storing("-r", "--repo", help = "name of the target GitHub repo")
}

fun main(args: Array<String>) = mainBody {
    try {
        val githubConfig = ArgParser(args).parseInto(::ArgsConfig)
        val repo = githubConfig.fetchRepositoryInfo()
        val releases = createReleases(repo)
        println(releases.joinToString("\n\n"))
    } catch (e: GitHubConfigException) {
        System.err.println(e.message)
        System.exit(1)
    }
}

fun createReleases(repo: GHRepository): List<Release> {
    val tags = repo.listTags()
    val datedTags = tags.map { DatedTag(it.name, it.commit.commitDate) }
    val issues = repo.getIssues(GHIssueState.CLOSED)

    return splitIssues(datedTags, issues)
}

fun splitIssues(tags: List<DatedTag>, issues: List<GHIssue>): List<Release> {
    val releases = mutableListOf<Release>()
    var remainingIssues = issues
    for (tag in tags.sortedBy { it.date }) {
        val (closedBeforeTag, closedAfterTag) = remainingIssues.partition { it.closedAt <= tag.date }
        remainingIssues = closedAfterTag
        releases.add(Release(tag.name, tag.date.toInstant(), closedBeforeTag))
    }
    return releases.sortedByDescending { it.date }
}

data class DatedTag(val name: String, val date: Date)

data class Release(
    val tag: String,
    val date: Instant,
    val issues: List<GHIssue>
) {
    private fun GHIssue.prettyPrint(): String = "$title ${labels.map { it.name }}"

    private fun List<GHIssue>.prettyPrint(): String = joinToString("\n") { " - ${it.prettyPrint()}" }

    override fun toString(): String = "$tag ($date)\n${issues.prettyPrint()}"
}
