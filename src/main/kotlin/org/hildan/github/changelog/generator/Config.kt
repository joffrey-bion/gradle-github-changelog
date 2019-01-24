package org.hildan.github.changelog.generator

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
    val globalHeader: String = "Changelog",
    val showUnreleased: Boolean = true,
    val unreleasedTitle: String = "Unreleased",
    val futureVersion: String? = null,
    val sections: List<SectionDefinition> = DEFAULT_SECTIONS,
    val defaultIssueSectionTitle: String = "Closed issues:",
    val defaultPrSectionTitle: String = "Merged pull requests:",
    val includeLabels: List<String> = emptyList(),
    val excludeLabels: List<String> = listOf("duplicate", "invalid", "question", "wontfix"),
    val customReleaseUrlTemplate: String? = null,
    val customDiffUrlTemplate: String? = null
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
