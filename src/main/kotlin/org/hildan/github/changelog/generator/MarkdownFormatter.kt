package org.hildan.github.changelog.generator

import java.time.format.DateTimeFormatter

open class MarkdownFormatter(
    protected val dateFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
) {
    fun format(changelog: ChangeLog): String = """# ${changelog.title}
        |
        |${formatReleases(changelog.releases)}""".trimMargin()

    protected fun formatReleases(releases: List<Release>): String = releases.joinToString("", transform = ::format)

    protected fun format(release: Release): String = """## ${formatTitle(release)}${formatDiffUrl(release.diffUrl)}
        |
        |${formatGroups(release.sections)}
        |""".trimMargin()

    protected fun formatDiffUrl(diffUrl: String?): String =
        if (diffUrl != null) {
            "\n[Full Changelog]($diffUrl)"
        } else {
            ""
        }

    protected fun formatTitle(release: Release): String = if (release.releaseUrl != null) {
        "[${release.title}](${release.releaseUrl}) (${dateFormat.format(release.date)})"
    } else {
        "${release.title} (${dateFormat.format(release.date)})"
    }

    protected fun formatGroups(groups: List<Section>): String = groups.joinToString("\n", transform = ::format)

    protected fun format(group: Section): String = with(group) {
        """**$title**
            |
            |${format(issues)}
            |""".trimMargin()
    }

    protected fun format(issues: List<Issue>) = issues.joinToString("\n", transform = ::formatIssue)

    protected fun formatIssue(issue: Issue): String = with(issue) {
        " - $title [#$number]($url)${formatIssueSuffix(issue)}"
    }

    protected fun formatIssueSuffix(issue: Issue): String = if (issue.isPullRequest) " (@${issue.authorLogin})" else ""
}
