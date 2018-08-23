package org.hildan.github.changelog.generator

import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GitHub
import java.time.Instant
import java.util.Date

fun main(args: Array<String>) {
    val user = if (args.size > 0) args[0] else "joffrey-bion"
    val repoName = if (args.size > 1) args[1] else "livedoc"

    val github = GitHub.connect()
    val repo = github.getRepository("$user/$repoName") ?: throw IllegalArgumentException("repo not found")

    val tags = repo.listTags() ?: throw RuntimeException("could not get tags")
    val datedTags = tags.map { DatedTag(it.name, it.commit.commitDate) }

    val issues = repo.getIssues(GHIssueState.CLOSED) ?: throw RuntimeException("could not get issues")

    val releases = createReleases(datedTags, issues)

    println(releases.joinToString("\n\n"))
}

fun createReleases(tags: List<DatedTag>, issues: List<GHIssue>): List<Release> {
    val releases = mutableListOf<Release>()
    var remainingIssues = issues
    for (tag in tags.sortedBy { it.date }) {
        val (closedBeforeTag, closedAfterTag) = remainingIssues.partition { it.closedAt <= tag.date }
        remainingIssues = closedAfterTag
        releases.add(Release(tag.name, tag.date.toInstant(), closedBeforeTag))
    }
    return releases.sortedByDescending { it.date }
}

data class DatedTag(
    val name: String,
    val date: Date
)

data class Release(
    val tag: String,
    val date: Instant,
    val issues: List<GHIssue>
) {
    private fun GHIssue.prettyPrint(): String = "$title ${labels.map { it.name }}"

    private fun List<GHIssue>.prettyPrint(): String = joinToString("\n") { " - ${it.prettyPrint()}" }

    override fun toString(): String = "$tag ($date)\n${issues.prettyPrint()}"
}
