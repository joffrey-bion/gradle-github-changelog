package org.hildan.github.changelog.builder

import java.time.ZoneId

const val DEFAULT_CHANGELOG_TITLE = "Change Log"
const val DEFAULT_SHOW_UNRELEASED = true
const val DEFAULT_UNRELEASED_VERSION_TITLE = "Unreleased"
const val DEFAULT_ISSUES_SECTION_TITLE = "Closed issues:"
const val DEFAULT_PR_SECTION_TITLE = "Merged pull requests:"

val DEFAULT_INCLUDED_LABELS = emptyList<String>()
val DEFAULT_EXCLUDED_LABELS = listOf("duplicate", "invalid", "question", "wontfix")
val DEFAULT_SKIPPED_TAGS = emptyList<String>()

val DEFAULT_SECTIONS = listOf(
    SectionDefinition("Implemented enhancements:", listOf("enhancement")),
    SectionDefinition("Fixed bugs:", listOf("bug"))
)
val DEFAULT_RELEASE_URL_TAG_TRANSFORM: (String) -> String = { it }
val DEFAULT_DIFF_URL_TAG_TRANSFORM: (String) -> String = { it }
val DEFAULT_CUSTOM_TAG_BY_ISSUE_NUMBER: Map<Int, String> = emptyMap()

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
    val releaseUrlTemplate: String,
    val diffUrlTemplate: String,
    val releaseUrlTagTransform: (String) -> String = DEFAULT_RELEASE_URL_TAG_TRANSFORM,
    val diffUrlTagTransform: (String) -> String = DEFAULT_DIFF_URL_TAG_TRANSFORM,
    val customTagByIssueNumber: Map<Int, String> = DEFAULT_CUSTOM_TAG_BY_ISSUE_NUMBER,
    val timezone: ZoneId = DEFAULT_TIMEZONE,
)

data class SectionDefinition(val title: String, val labels: List<String>)
