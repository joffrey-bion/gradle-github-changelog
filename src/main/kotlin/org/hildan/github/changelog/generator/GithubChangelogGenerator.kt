package org.hildan.github.changelog.generator

import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRepository
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

val DEFAULT_SECTIONS = listOf(
    SectionDefinition("Breaking change:", listOf("backwards-incompatible", "breaking")),
    SectionDefinition("Implemented enhancements:", listOf("enhancement", "Enhancement")),
    SectionDefinition("Fixed bugs:", listOf("bug", "Bug")),
    SectionDefinition("Deprecated:", listOf("deprecated", "Deprecated")),
    SectionDefinition("Removed:", listOf("removed", "Removed")),
    SectionDefinition("Security:", listOf("security", "Security"))
)

data class Configuration(
    val github: GitHubConfig,
    val globalHeader: String = "Changelog",
    val showUnreleased: Boolean = true,
    val unreleasedTitle: String = "Unreleased:",
    val futureVersion: String? = null,
    val sections: List<SectionDefinition> = DEFAULT_SECTIONS,
    val defaultIssueSectionTitle: String = "Closed issue:",
    val defaultPrSectionTitle: String = "Merged pull requests:",
    val includeLabels: List<String> = emptyList(),
    val excludeLabels: List<String> = listOf("duplicate", "invalid", "question", "wontfix")
)

data class SectionDefinition(val title: String, val labels: List<String>)

data class DatedTag(val name: String, val date: Date)

class GithubChangelogGenerator(
    private val config: Configuration,
    private val outputFile: File? = null,
    private val formatter: MarkdownFormatter = MarkdownFormatter(config.unreleasedTitle)
) {
    private val sectionByLabel: Map<String, String> =
        config.sections.flatMap { s -> s.labels.map { it to s.title } }.toMap()

    fun generate() {
        val repo = config.github.fetchRepositoryInfo()
        val releases = createReleases(repo)
        val changeLog = ChangeLog(config.globalHeader, releases)
        val markdown = formatter.format(changeLog)
        if (outputFile != null) {
            outputFile.writeText(markdown)
        } else {
            print(markdown)
        }
    }

    private fun createReleases(repo: GHRepository): List<Release> {
        val tags = repo.listTags()
        val datedTags = tags.map { DatedTag(it.name, it.commit.commitDate) }
        val issues = repo.getIssues(GHIssueState.CLOSED).filter(this::shouldInclude)

        return splitIssues(datedTags, issues)
    }

    private fun shouldInclude(issue: GHIssue) = !isExcluded(issue) && isIncluded(issue)

    private fun isIncluded(issue: GHIssue) =
        config.includeLabels.isEmpty() || issue.labels.any { config.includeLabels.contains(it.name) }

    private fun isExcluded(issue: GHIssue) = issue.labels.any { config.excludeLabels.contains(it.name) }

    private fun splitIssues(tags: List<DatedTag>, issues: List<GHIssue>): List<Release> {
        val releases = mutableListOf<Release>()
        var remainingIssues = issues
        var previousTag: String? = null
        for (tag in tags.sortedBy { it.date }) {
            val (closedBeforeTag, closedAfterTag) = remainingIssues.partition { it.closedAt <= tag.date }
            val sections = splitSections(closedBeforeTag)
            remainingIssues = closedAfterTag
            val release = createRelease(previousTag, tag, sections)
            releases.add(release)
            previousTag = tag.name
        }
        if (remainingIssues.isNotEmpty() && config.showUnreleased) {
            val sections = splitSections(remainingIssues)
            releases.add(Release(config.futureVersion, previousTag, LocalDate.now(), sections, null, null))
        }
        return releases.sortedByDescending { it.date }
    }

    private fun createRelease(previousTag: String?, tag: DatedTag, sections: List<Section>): Release {
        val releaseUrl = config.github.releaseUrl(tag.name)
        val changeLogUrl = previousTag?.let { config.github.changelogUrl(it, tag.name) }
        val date = tag.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        return Release(tag.name, previousTag, date, sections, releaseUrl, changeLogUrl)
    }

    private fun splitSections(issues: List<GHIssue>): List<Section> =
        issues.groupBy { findSectionTitle(it) }
            .map { (title, issues) -> Section(title, issues.map { it.toIssue() }) }

    private fun GHIssue.toIssue(): Issue = Issue(number, title, htmlUrl.toString(), user.login, this is GHPullRequest)

    private fun findSectionTitle(issue: GHIssue): String =
        issue.labels.asSequence().mapNotNull { sectionByLabel[it.name] }.firstOrNull() ?: defaultSection(issue)

    private fun defaultSection(issue: GHIssue): String = when (issue) {
        is GHPullRequest -> config.defaultPrSectionTitle
        else -> config.defaultIssueSectionTitle
    }
}
