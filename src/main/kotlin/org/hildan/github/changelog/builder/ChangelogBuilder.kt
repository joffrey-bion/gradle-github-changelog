package org.hildan.github.changelog.builder

import org.hildan.github.changelog.github.Repository
import java.time.LocalDateTime

private const val RELEASE_SUMMARY_LABEL = "release-summary"

class ChangelogBuilder(private val config: ChangelogConfig) {

    private val sectionByLabel: Map<String, SectionDefinition> =
        config.sections.flatMap { s -> s.labels.map { it to s } }.toMap()

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
        val (regularIssues, overriddenIssuesByTag, summariesByTag) = sortIssues(repo.closedIssues, repo.tags)

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
        if (config.showUnreleased) {
            val futureTag = config.futureVersionTag
            if (futureTag != null) {
                releases.add(createFutureRelease(futureTag, previousRef, remainingIssues, summariesByTag[futureTag]))
            } else if (remainingIssues.isNotEmpty()) {
                releases.add(createUnreleasedRelease(remainingIssues))
            }
        }
        return releases.sortedByDescending { it.date }
    }

    private data class SortedIssues(
        val regularIssues: List<Issue>,
        val overriddenIssuesByTag: Map<String, List<Issue>>,
        val releaseSummariesByTag: Map<String, String?>,
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
        val releaseSummariesByTag = releaseSummaries.associate {
            val milestone = it.milestone ?: error(
                "Issue #${it.number} is a release summary but doesn't have a milestone. " +
                        "Please add a milestone with a title that matches the release tag that this summary should apply to."
            )
            milestone.title to it.body
        }
        return SortedIssues(regularIssues, overriddenIssues, releaseSummariesByTag)
    }

    private fun shouldInclude(issue: Issue) = !isExcluded(issue) && isIncluded(issue)

    private fun isIncluded(issue: Issue) =
        config.includeLabels.isEmpty() || issue.labels.any { it in config.includeLabels }

    private fun isExcluded(issue: Issue) = issue.labels.any { it in config.excludeLabels }

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

    private fun createFutureRelease(
        futureVersionTag: String,
        previousTagName: Ref,
        issues: List<Issue>,
        summary: String?,
    ) = createRelease(
        tagName = futureVersionTag,
        previousRef = previousTagName,
        date = LocalDateTime.now(),
        issues = issues,
        summary = summary,
    )

    private fun createUnreleasedRelease(issues: List<Issue>) = Release(
        tag = null,
        title = config.unreleasedVersionTitle,
        summary = null,
        date = LocalDateTime.now(),
        sections = dispatchInSections(issues),
        releaseUrl = null,
        diffUrl = null,
    )

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
        issues.groupBy { findSection(it) }
            .map { (section, issues) -> Section(section.title, section.order, issues.sortedByDescending { it.closedAt }) }
            .sortedBy { it.order }

    private fun findSection(issue: Issue): SectionDefinition =
        issue.labels.asSequence().mapNotNull { sectionByLabel[it] }.firstOrNull() ?: defaultSection(issue)

    private fun defaultSection(issue: Issue): SectionDefinition = if (issue.isPullRequest) {
        config.defaultPrSection
    } else {
        config.defaultIssueSection
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
