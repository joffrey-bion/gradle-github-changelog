package org.hildan.github.changelog.builder

import java.time.LocalDateTime
import java.time.ZoneId

class ChangeLogBuilder(private val config: ChangelogConfig) {

    private val sectionByLabel: Map<String, String> =
        config.sections.flatMap { s -> s.labels.map { it to s.title } }.toMap()

    fun createChangeLog(issues: List<Issue>, tags: List<Tag>): ChangeLog {
        val releases = createReleases(issues, tags)
        val displayedReleases = filterReleases(releases)
        return ChangeLog(config.globalHeader, displayedReleases)
    }

    private fun filterReleases(releases: List<Release>): List<Release> {
        val nonSkippedReleases = releases.filterNot { it.tag in config.skipTags }
        return if (config.sinceTag != null) {
            nonSkippedReleases.dropLastWhile { it.tag != config.sinceTag }
        } else {
            nonSkippedReleases
        }
    }

    private fun createReleases(issues: List<Issue>, tags: List<Tag>): List<Release> {
        val (regularIssues, overriddenIssuesByTag) = sortIssues(issues)

        var remainingIssues = regularIssues
        val releases = mutableListOf<Release>()
        var previousTag: String? = null
        for (tag in tags.sortedBy { it.date }) {
            val (closedBeforeTag, closedAfterTag) = remainingIssues.partition { it.closedAt <= tag.date }

            val tagOverriddenIssues = overriddenIssuesByTag[tag.name] ?: emptyList()
            val issuesForTag = closedBeforeTag + tagOverriddenIssues

            releases.add(createExistingRelease(tag, previousTag, issuesForTag))

            remainingIssues = closedAfterTag
            previousTag = tag.name
        }
        if (remainingIssues.isNotEmpty() && config.showUnreleased) {
            releases.add(createFutureRelease(previousTag, remainingIssues))
        }
        return releases.sortedByDescending { it.date }
    }

    private data class SortedIssues(
        val regularIssues: List<Issue>,
        val overriddenIssuesByTag: Map<String, List<Issue>>
    )

    private fun sortIssues(issues: List<Issue>): SortedIssues {
        val (overriddenIssues, regularIssues) = issues
            .filter(this::shouldInclude)
            .partition { config.customTagByIssueNumber.containsKey(it.number) }
        val issuesByTag = overriddenIssues
            .groupBy { config.customTagByIssueNumber.getValue(it.number) }
        return SortedIssues(regularIssues, issuesByTag)
    }

    private fun shouldInclude(issue: Issue) = !isExcluded(issue) && isIncluded(issue)

    private fun isIncluded(issue: Issue) =
        config.includeLabels.isEmpty() || issue.labels.any { config.includeLabels.contains(it) }

    private fun isExcluded(issue: Issue) = issue.labels.any { config.excludeLabels.contains(it) }

    private fun createExistingRelease(tag: Tag, previousTagName: String?, issues: List<Issue>): Release {
        val tagName = tag.name
        val date = tag.date.atZone(ZoneId.systemDefault()).toLocalDateTime()
        return createRelease(tagName, previousTagName, date, issues)
    }

    private fun createFutureRelease(previousTagName: String?, issues: List<Issue>): Release =
        if (config.futureVersionTag != null) {
            createRelease(config.futureVersionTag, previousTagName, LocalDateTime.now(), issues)
        } else {
            val sections = dispatchInSections(issues)
            Release(
                null,
                config.unreleasedVersionTitle,
                LocalDateTime.now(),
                sections,
                null,
                null
            )
        }

    private fun createRelease(tagName: String, previousTagName: String?, date: LocalDateTime, issues: List<Issue>): Release {
        val sections = dispatchInSections(issues)
        val releaseUrl = releaseUrl(tagName)
        val diffUrl = previousTagName?.let { diffUrl(it, tagName) }
        return Release(tagName, tagName, date, sections, releaseUrl, diffUrl)
    }

    private fun dispatchInSections(issues: List<Issue>): List<Section> =
        issues.groupBy { findSectionTitle(it) }
            .map { (title, issues) ->
                Section(
                    title,
                    issues.sortedByDescending { it.closedAt })
            }
            .sortedBy { it.title }

    private fun findSectionTitle(issue: Issue): String =
        issue.labels.asSequence().mapNotNull { sectionByLabel[it] }.firstOrNull() ?: defaultSection(issue)

    private fun defaultSection(issue: Issue): String = if (issue.isPullRequest) {
        config.defaultPrSectionTitle
    } else {
        config.defaultIssueSectionTitle
    }

    private fun releaseUrl(tag: String) = String.format(config.releaseUrlTemplate, config.releaseUrlTagTransform(tag))

    private fun diffUrl(fromTag: String, toTag: String) =
        String.format(config.diffUrlTemplate, config.diffUrlTagTransform(fromTag), config.diffUrlTagTransform(toTag))
}
