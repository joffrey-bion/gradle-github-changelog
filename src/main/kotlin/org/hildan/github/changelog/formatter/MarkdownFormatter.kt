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
    private val escapedCharactersRegex = Regex("""([\\`*_{}\[\]()#+\-!<>])""")

    fun formatChangeLog(changelog: Changelog): String = """# ${changelog.title.escapeMd()}
        |
        |${formatReleases(changelog.releases)}""".trimMargin()

    protected fun formatReleases(releases: List<Release>): String = if (releases.isEmpty()) {
        "*Nothing much happened so far, actually...*"
    } else {
        releases.joinToString("",transform =::formatRelease)
    }

    protected fun formatRelease(release: Release): String = """## ${formatTitle(release)}${formatDiffUrl(release.diffUrl)}
        |
        |${formatSections(release.sections)}
        |""".trimMargin()

    protected fun formatDiffUrl(diffUrl: String?): String =
        if (diffUrl != null) {
            "\n[View commits]($diffUrl)"
        } else {
            ""
        }

    protected fun formatTitle(release: Release): String = if (release.releaseUrl != null) {
        "[${release.title.escapeMd()}](${release.releaseUrl}) (${dateFormat.format(release.date)})"
    } else {
        "${release.title.escapeMd()} (${dateFormat.format(release.date)})"
    }

    protected fun formatSections(sections: List<Section>): String = sections.joinToString("\n", transform = ::formatSection)

    protected fun formatSection(section: Section): String = with(section) {
        """**$title**
            |
            |${formatIssues(issues)}
            |""".trimMargin()
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
