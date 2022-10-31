package org.hildan.github.changelog.formatter

import org.hildan.github.changelog.builder.Changelog
import org.hildan.github.changelog.builder.Issue
import org.hildan.github.changelog.builder.Release
import org.hildan.github.changelog.builder.Section
import org.hildan.github.changelog.builder.User
import java.time.format.DateTimeFormatter

open class MarkdownFormatter(
    protected val dateFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
) {
    private val escapedCharactersRegex = Regex("""([\\*_{}\[\]()#+\-!<>])""")

    fun formatChangeLog(changelog: Changelog): String = buildString {
        appendLine("# ${changelog.title.escapeMd()}")
        appendLine()
        append(formatReleases(changelog.releases))
    }

    protected fun formatReleases(releases: List<Release>): String = if (releases.isEmpty()) {
        "*Nothing much happened so far, actually...*\n"
    } else {
        releases.joinToString("\n", transform = ::formatRelease)
    }

    protected fun formatRelease(release: Release): String = buildString {
        appendLine("## ${formatTitle(release)}")

        if (release.diffUrl != null) {
            appendLine(formatDiffUrl(release.diffUrl))
        }
        appendLine()
        append(formatReleaseBody(release))
    }

    fun formatReleaseBody(release: Release): String = buildString {
        if (release.summary != null) {
            appendLine(release.summary)
            appendLine()
        }
        append(formatSections(release.sections))
    }

    protected fun formatDiffUrl(diffUrl: String): String = "[View commits]($diffUrl)"

    protected fun formatTitle(release: Release): String {
        val titleText = release.title.escapeMd()
        val title = if (release.releaseUrl == null) titleText else "[$titleText](${release.releaseUrl})"
        return "$title (${dateFormat.format(release.date)})"
    }

    protected fun formatSections(sections: List<Section>): String = sections.joinToString("\n", transform = ::formatSection)

    protected fun formatSection(section: Section): String = with(section) {
        buildString {
            appendLine("**$title**")
            appendLine()
            appendLine(formatIssues(issues))
        }
    }

    protected fun formatIssues(issues: List<Issue>): String = issues.joinToString("\n", transform = ::formatIssue)

    protected fun formatIssue(issue: Issue): String = with(issue) {
        "- ${title.escapeMd()} [\\#$number]($url)${formatIssueSuffix(issue)}"
    }

    protected fun formatIssueSuffix(issue: Issue): String = if (issue.isPullRequest) {
        " (${formatUserMentionLink(issue.author)})"
    } else {
        ""
    }

    protected fun formatUserMentionLink(user: User) = "[@${user.login}](${user.profileUrl})"

    protected fun String.escapeMd(): String = this.replace(escapedCharactersRegex, """\\$1""")
}
