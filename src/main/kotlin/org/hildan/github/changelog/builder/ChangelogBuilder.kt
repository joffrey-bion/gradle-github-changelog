package org.hildan.github.changelog.builder

import org.hildan.github.changelog.github.Repository
import java.time.LocalDateTime

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
        val (regularIssues, overriddenIssuesByTag) = sortIssues(repo.closedIssues, repo.tags)

        var remainingIssues = regularIssues
        val releases = mutableListOf<Release>()
        var previousRef: Ref = Ref.Sha(repo.initialCommitSha)
        for (tag in repo.tags.sortedBy { it.date }) {
            val (closedBeforeTag, closedAfterTag) = remainingIssues.partition { it.closedAt <= tag.date }

            val tagOverriddenIssues = overriddenIssuesByTag[tag.name] ?: emptyList()
            val issuesForTag = closedBeforeTag + tagOverriddenIssues

            releases.add(createExistingRelease(tag, previousRef, issuesForTag))

            remainingIssues = closedAfterTag
            previousRef = Ref.Tag(tag.name)
        }
        if (remainingIssues.isNotEmpty() && config.showUnreleased) {
            releases.add(createFutureRelease(previousRef, remainingIssues))
        }
        return releases.sortedByDescending { it.date }
    }

    private data class SortedIssues(
        val regularIssues: List<Issue>,
        val overriddenIssuesByTag: Map<String, List<Issue>>
    )

    private fun sortIssues(issues: List<Issue>, tags: List<Tag>): SortedIssues {
        val tagNames = tags.mapTo(HashSet()) { it.name }
        val regularIssues = mutableListOf<Issue>()
        val overriddenIssues = mutableMapOf<String, MutableList<Issue>>()

        issues.asSequence().filter { shouldInclude(it) }.forEach { issue ->
            val tagOverride = issue.getTagOverride(tagNames)
            when {
                tagOverride != null -> overriddenIssues.getOrPut(tagOverride) { mutableListOf() }.add(issue)
                else -> regularIssues.add(issue)
            }
        }
        return SortedIssues(regularIssues, overriddenIssues)
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

    private fun createExistingRelease(tag: Tag, previousRef: Ref, issues: List<Issue>): Release {
        val tagName = tag.name
        val date = tag.date.atZone(config.timezone).toLocalDateTime()
        return createRelease(tagName, previousRef, date, issues)
    }

    private fun createFutureRelease(previousTagName: Ref, issues: List<Issue>): Release =
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

    private fun createRelease(tagName: String, previousRef: Ref, date: LocalDateTime, issues: List<Issue>): Release {
        val sections = dispatchInSections(issues)
        val releaseUrl = releaseUrl(tagName)
        val diffUrl = diffUrl(previousRef, tagName)
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
