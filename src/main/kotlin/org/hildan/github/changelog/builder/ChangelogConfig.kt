package org.hildan.github.changelog.builder

import java.time.ZoneId

const val DEFAULT_CHANGELOG_TITLE = "Change Log"
const val DEFAULT_SHOW_UNRELEASED = true
const val DEFAULT_UNRELEASED_VERSION_TITLE = "Unreleased"
const val DEFAULT_ISSUES_SECTION_TITLE = "Closed issues:"
const val DEFAULT_PR_SECTION_TITLE = "Merged pull requests:"

val DEFAULT_INCLUDED_LABELS = emptyList<String>()
val DEFAULT_EXCLUDED_LABELS = listOf("doc", "documentation", "duplicate", "invalid", "question", "wontfix")
val DEFAULT_SKIPPED_TAGS = emptyList<String>()
val DEFAULT_SKIPPED_TAGS_REGEX = emptyList<Regex>()

val DEFAULT_SECTIONS = listOf(
    SectionDefinition("New features:", "feature"),
    SectionDefinition("Implemented enhancements:", "enhancement"),
    SectionDefinition("Fixed bugs:", "bug"),
)
val DEFAULT_RELEASE_URL_TAG_TRANSFORM: (String) -> String = { it }
val DEFAULT_DIFF_URL_TAG_TRANSFORM: (String) -> String = { it }
val DEFAULT_CUSTOM_TAG_BY_ISSUE_NUMBER: Map<Int, String> = emptyMap()
const val DEFAULT_USE_MILESTONE_AS_TAG: Boolean = true

val DEFAULT_TIMEZONE: ZoneId = ZoneId.of("GMT")

data class ChangelogConfig(
    val globalHeader: String = DEFAULT_CHANGELOG_TITLE,
    val showUnreleased: Boolean = DEFAULT_SHOW_UNRELEASED,
    val futureVersionTag: String? = null,
    val unreleasedVersionTitle: String = DEFAULT_UNRELEASED_VERSION_TITLE,
    val sections: List<SectionDefinition> = DEFAULT_SECTIONS,
    val defaultIssueSectionTitle: String = DEFAULT_ISSUES_SECTION_TITLE,
    val defaultPrSectionTitle: String = DEFAULT_PR_SECTION_TITLE,
    val includeLabels: List<String> = DEFAULT_INCLUDED_LABELS,
    val excludeLabels: List<String> = DEFAULT_EXCLUDED_LABELS,
    val sinceTag: String? = null,
    val skipTags: List<String> = DEFAULT_SKIPPED_TAGS,
    val skipTagsRegex: List<Regex> = DEFAULT_SKIPPED_TAGS_REGEX,
    val releaseUrlTemplate: String,
    val diffUrlTemplate: String,
    val releaseUrlTagTransform: (String) -> String = DEFAULT_RELEASE_URL_TAG_TRANSFORM,
    val diffUrlTagTransform: (String) -> String = DEFAULT_DIFF_URL_TAG_TRANSFORM,
    val customTagByIssueNumber: Map<Int, String> = DEFAULT_CUSTOM_TAG_BY_ISSUE_NUMBER,
    val useMilestoneAsTag: Boolean = DEFAULT_USE_MILESTONE_AS_TAG,
    val timezone: ZoneId = DEFAULT_TIMEZONE,
)

/**
 * Defines a section of issues within a release in the changelog.
 */
data class SectionDefinition(
    /**
     * The title of the section.
     */
    val title: String,
    /**
     * The labels of the issues to include in this section.
     * Any issue with at least one of these labels will be listed under this section.
     *
     * If multiple sections list the same label, issues with this label will appear in the last section that was
     * defined with this label.
     */
    val labels: List<String>,
) {
    /**
     * Creates a section of issues within a release in the changelog.
     *
     * This constructor creates a section with the given [title], under which any issue with the given [label] will
     * be listed.
     *
     * If multiple sections list the same label, issues with this label will appear in the last section that was
     * defined with this label.
     */
    constructor(title: String, label: String) : this(title, listOf(label))
}
