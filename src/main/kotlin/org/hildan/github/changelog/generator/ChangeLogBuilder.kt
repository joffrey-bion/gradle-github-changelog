package org.hildan.github.changelog.generator

import java.time.LocalDateTime
import java.time.ZoneId

class ChangeLogBuilder(private val config: ChangelogConfig) {

    private val sectionByLabel: Map<String, String> =
        config.sections.flatMap { s -> s.labels.map { it to s.title } }.toMap()

    fun createChangeLog(issues: List<Issue>, tags: List<Tag>): ChangeLog {
        val releases = createReleases(issues, tags)
        return ChangeLog(config.globalHeader, releases)
    }

    private fun createReleases(issues: List<Issue>, tags: List<Tag>): List<Release> {
        val releases = mutableListOf<Release>()
        var remainingIssues = issues.filter(this::shouldInclude)
        var previousTag: String? = null
        for (tag in tags.sortedBy { it.date }) {
            val (closedBeforeTag, closedAfterTag) = remainingIssues.partition { it.closedAt <= tag.date }
            remainingIssues = closedAfterTag
            releases.add(createRelease(tag, previousTag, closedBeforeTag))
            previousTag = tag.name
        }
        if (remainingIssues.isNotEmpty() && config.showUnreleased) {
            releases.add(createFutureRelease(remainingIssues))
        }
        return releases.sortedByDescending { it.date }
    }

    private fun shouldInclude(issue: Issue) = !isExcluded(issue) && isIncluded(issue)

    private fun isIncluded(issue: Issue) =
        config.includeLabels.isEmpty() || issue.labels.any { config.includeLabels.contains(it) }

    private fun isExcluded(issue: Issue) = issue.labels.any { config.excludeLabels.contains(it) }

    private fun createFutureRelease(issues: List<Issue>): Release {
        val sections = dispatchInSections(issues)
        val title = config.futureVersion ?: config.unreleasedTitle
        return Release(title, LocalDateTime.now(), sections, null, null)
    }

    private fun createRelease(tag: Tag, previousTagName: String?, issues: List<Issue>): Release {
        val sections = dispatchInSections(issues)
        val releaseUrl = releaseUrl(tag.name)
        val diffUrl = previousTagName?.let { diffUrl(it, tag.name) }
        val date = tag.date.atZone(ZoneId.systemDefault()).toLocalDateTime()
        return Release(tag.name, date, sections, releaseUrl, diffUrl)
    }

    private fun dispatchInSections(issues: List<Issue>): List<Section> =
        issues.groupBy { findSectionTitle(it) }.map { (title, issues) -> Section(title, issues) }

    private fun findSectionTitle(issue: Issue): String =
        issue.labels.asSequence().mapNotNull { sectionByLabel[it] }.firstOrNull() ?: defaultSection(issue)

    private fun defaultSection(issue: Issue): String = if (issue.isPullRequest) {
        config.defaultPrSectionTitle
    } else {
        config.defaultIssueSectionTitle
    }

    private fun releaseUrl(tag: String) = String.format(config.releaseUrlTemplate, tag)

    private fun diffUrl(fromTag: String, toTag: String) = String.format(config.diffUrlTemplate, fromTag, toTag)
}
