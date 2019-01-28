package org.hildan.github.changelog.generator

const val DEFAULT_CHANGELOG_TITLE = "Change Log"
const val DEFAULT_SHOW_UNRELEASED = true
const val DEFAULT_UNRELEASED_VERSION_TITLE = "Unreleased"
const val DEFAULT_ISSUES_SECTION_TITLE = "Closed issues:"
const val DEFAULT_PR_SECTION_TITLE = "Merged pull requests:"

val DEFAULT_INCLUDED_LABELS = emptyList<String>()
val DEFAULT_EXCLUDED_LABELS = listOf("duplicate", "invalid", "question", "wontfix")
val DEFAULT_SKIPPED_TAGS = emptyList<String>()

val DEFAULT_SECTIONS = listOf(
    SectionDefinition("Breaking changes:", listOf("backwards-incompatible", "breaking")),
    SectionDefinition("Implemented enhancements:", listOf("enhancement", "Enhancement")),
    SectionDefinition("Fixed bugs:", listOf("bug", "Bug")),
    SectionDefinition("Deprecated:", listOf("deprecated", "Deprecated")),
    SectionDefinition("Removed:", listOf("removed", "Removed")),
    SectionDefinition("Security fixes:", listOf("security", "Security"))
)

data class ChangelogConfig(
    val github: GitHubConfig,
    val globalHeader: String = DEFAULT_CHANGELOG_TITLE,
    val showUnreleased: Boolean = DEFAULT_SHOW_UNRELEASED,
    val futureVersion: String = DEFAULT_UNRELEASED_VERSION_TITLE,
    val sections: List<SectionDefinition> = DEFAULT_SECTIONS,
    val defaultIssueSectionTitle: String = DEFAULT_ISSUES_SECTION_TITLE,
    val defaultPrSectionTitle: String = DEFAULT_PR_SECTION_TITLE,
    val includeLabels: List<String> = DEFAULT_INCLUDED_LABELS,
    val excludeLabels: List<String> = DEFAULT_EXCLUDED_LABELS,
    val sinceTag: String? = null,
    val skipTags: List<String> = DEFAULT_SKIPPED_TAGS,
    val customReleaseUrlTemplate: String? = null,
    val customDiffUrlTemplate: String? = null,
    val releaseUrlTagTransform: (String) -> String = { it },
    val diffUrlTagTransform: (String) -> String = { it }
) {
    val releaseUrlTemplate: String = customReleaseUrlTemplate ?: github.releaseUrlTemplate
    val diffUrlTemplate: String = customDiffUrlTemplate ?: github.diffUrlTemplate
}

data class GitHubConfig(
    val user: String,
    val repo: String,
    val token: String? = null
) {
    val releaseUrlTemplate: String = "https://github.com/$user/$repo/tree/%s"
    val diffUrlTemplate: String = "https://github.com/$user/$repo/compare/%s...%s"
}

data class SectionDefinition(val title: String, val labels: List<String>)
