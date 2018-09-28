package org.hildan.github.changelog.generator

import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHPullRequest
import java.time.format.DateTimeFormatter

class MarkdownFormatter(
    private val config: Configuration,
    private val tagTransform: (String) -> String = { it },
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
) {
    fun format(changelog: ChangeLog): String = """# Changelog
        |
        |${formatReleases(changelog.releases)}
        |""".trimMargin()

    protected fun formatReleases(releases: List<Release>): String = releases.joinToString("\n", transform = ::format)

    protected fun format(release: Release): String = """## ${formatTitle(release)}${formatChangelogLink(release)}
        |
        |${formatGroups(release.sections)}
        |""".trimMargin()

    protected fun formatChangelogLink(release: Release): String =
        if (release.tag != null && release.previousTag != null) {
            "\n[Full Changelog](${config.github.changelogUrl(release.previousTag, release.tag)})"
        } else {
            ""
        }

    protected fun formatTitle(release: Release): String = if (release.tag != null) {
        "[${tagTransform(release.tag)}](${config.github.releaseUrl(release.tag)}) ${dateFormat.format(release.date)}"
    } else {
        "${config.unreleasedTitle} ${dateFormat.format(release.date)}"
    }

    protected fun formatGroups(groups: List<Section>): String = groups.joinToString("\n", transform = ::format)

    protected fun format(group: Section): String = with(group) {
        """**$title**
            |
            | ${format(issues)}
            | """.trimMargin()
    }

    protected fun format(issues: List<GHIssue>) = issues.joinToString("\n", transform = ::formatIssueOrPr)

    private fun formatIssueOrPr(issue: GHIssue): String = when (issue) {
        is GHPullRequest -> formatPullRequest(issue)
        else -> formatIssue(issue)
    }

    protected fun formatIssue(issue: GHIssue): String = with(issue) { "- $title [#$number]($htmlUrl)" }

    protected fun formatPullRequest(pr: GHPullRequest): String = "${formatIssue(pr)} (@${pr.user.login})"
}
