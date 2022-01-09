package org.hildan.github.changelog.builder

import org.hildan.github.changelog.github.Repository
import java.time.LocalDateTime

private const val RELEASE_SUMMARY_LABEL = "release-summary"

class ChangelogBuilder(private val config: ChangelogConfig) {

    private val sectionByLabel: Map<String, String> =
        config.sections.flatMap { s -> s.labels.map { it to s.title } }.toMap()

    fun createChangeLog(repo: Repository): Changelog {
        val releases = createReleases(repo)
        val displayedReleases = filterReleases(releases)
        return Changelog(config.globalHeader, displayedReleases)
    }

    private fun filterReleases(releases: List<Release>): List<Release> {
        val nonSkippedReleases = releases.filterNot { it.shouldBeSkipped() }
        return if (config.sinceTag != null) {
            nonSkippedReleases.dropLastWhile { it.tag != config.sinceTag }
        } else {
            nonSkippedReleases
        }
    }

    private fun Release.shouldBeSkipped() =
        tag in config.skipTags || (tag != null && config.skipTagsRegex.any { it.matches(tag) })

    private fun createReleases(repo: Repository): List<Release> {
        val (regularIssues, overriddenIssuesByTag, releaseSummaries) = sortIssues(repo.closedIssues, repo.tags)
        val summariesByTag = releaseSummaries.associate {
            val milestone = it.milestone ?: error(
                "Issue #${it.number} is a release summary but doesn't have a milestone. " +
                    "Please add a milestone with a title that matches the release tag that this summary should apply to."
            )
            milestone.title to it.body
        }

        var remainingIssues = regularIssues
        val releases = mutableListOf<Release>()
        var previousRef: Ref = Ref.Sha(repo.initialCommitSha)
        for (tag in repo.tags.sortedBy { it.date }) {
            val (closedBeforeTag, closedAfterTag) = remainingIssues.partition { it.closedAt <= tag.date }

            val tagOverriddenIssues = overriddenIssuesByTag[tag.name] ?: emptyList()
            val issuesForTag = closedBeforeTag + tagOverriddenIssues

            releases.add(createExistingRelease(tag, previousRef, issuesForTag, summariesByTag[tag.name]))

            remainingIssues = closedAfterTag
            previousRef = Ref.Tag(tag.name)
        }
        if (remainingIssues.isNotEmpty() && config.showUnreleased) {
            val futureVersionSummary = config.futureVersionTag?.let { summariesByTag[it] }
            releases.add(createFutureRelease(previousRef, remainingIssues, futureVersionSummary))
        }
        return releases.sortedByDescending { it.date }
    }

    private data class SortedIssues(
        val regularIssues: List<Issue>,
        val overriddenIssuesByTag: Map<String, List<Issue>>,
        val releaseSummaries: List<Issue>,
    )

    private fun sortIssues(issues: List<Issue>, tags: List<Tag>): SortedIssues {
        val tagNames = tags.mapTo(HashSet()) { it.name }
        val regularIssues = mutableListOf<Issue>()
        val overriddenIssues = mutableMapOf<String, MutableList<Issue>>()
        val releaseSummaries = mutableListOf<Issue>()

        issues.asSequence().filter { shouldInclude(it) }.forEach { issue ->
            val tagOverride = issue.getTagOverride(tagNames)
            when {
                RELEASE_SUMMARY_LABEL in issue.labels -> releaseSummaries.add(issue)
                tagOverride != null -> overriddenIssues.getOrPut(tagOverride) { mutableListOf() }.add(issue)
                else -> regularIssues.add(issue)
            }
        }
        return SortedIssues(regularIssues, overriddenIssues, releaseSummaries)
    }

    private fun shouldInclude(issue: Issue) = !isExcluded(issue) && isIncluded(issue)

    private fun isIncluded(issue: Issue) =
        config.includeLabels.isEmpty() || issue.labels.any { config.includeLabels.contains(it) }

    private fun isExcluded(issue: Issue) = issue.labels.any { config.excludeLabels.contains(it) }

    private fun Issue.getTagOverride(allTags: Set<String>): String? {
        val tagOverride = config.customTagByIssueNumber[number]
        return when {
            tagOverride != null -> tagOverride
            config.useMilestoneAsTag && milestone != null && milestone.title in allTags -> milestone.title
            else -> null // no override
        }
    }

    private fun createExistingRelease(
        tag: Tag,
        previousRef: Ref,
        issues: List<Issue>,
        summary: String?,
    ): Release {
        val date = tag.date.atZone(config.timezone).toLocalDateTime()
        return createRelease(tag.name, previousRef, date, issues, summary)
    }

    private fun createFutureRelease(previousTagName: Ref, issues: List<Issue>, summary: String?): Release =
        if (config.futureVersionTag != null) {
            createRelease(
                tagName = config.futureVersionTag,
                previousRef = previousTagName,
                date = LocalDateTime.now(),
                issues = issues,
                summary = summary,
            )
        } else {
            Release(
                tag = null,
                title = config.unreleasedVersionTitle,
                summary = null,
                date = LocalDateTime.now(),
                sections = dispatchInSections(issues),
                releaseUrl = null,
                diffUrl = null,
            )
        }

    private fun createRelease(
        tagName: String,
        previousRef: Ref,
        date: LocalDateTime,
        issues: List<Issue>,
        summary: String?,
    ): Release = Release(
        tag = tagName,
        title = tagName,
        summary = summary,
        date = date,
        sections = dispatchInSections(issues),
        releaseUrl = releaseUrl(tagName),
        diffUrl = diffUrl(previousRef, tagName),
    )

    private fun dispatchInSections(issues: List<Issue>): List<Section> =
        issues.groupBy { findSectionTitle(it) }
            .map { (title, issues) -> Section(title, issues.sortedByDescending { it.closedAt }) }
            .sortedBy { it.title }

    private fun findSectionTitle(issue: Issue): String =
        issue.labels.asSequence().mapNotNull { sectionByLabel[it] }.firstOrNull() ?: defaultSection(issue)

    private fun defaultSection(issue: Issue): String = if (issue.isPullRequest) {
        config.defaultPrSectionTitle
    } else {
        config.defaultIssueSectionTitle
    }

    private fun releaseUrl(tag: String) = String.format(config.releaseUrlTemplate, config.releaseUrlTagTransform(tag))

    private fun diffUrl(fromRef: Ref, toTag: String): String {
        val from = when (fromRef) {
            is Ref.Tag -> config.diffUrlTagTransform(fromRef.name)
            is Ref.Sha -> fromRef.value
        }
        return String.format(config.diffUrlTemplate, from, config.diffUrlTagTransform(toTag))
    }
}

sealed class Ref {
    data class Tag(val name: String): Ref()
    data class Sha(val value: String): Ref()
}
