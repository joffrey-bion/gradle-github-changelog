package org.hildan.github.changelog.builder

import java.time.Instant
import java.time.LocalDateTime

data class Changelog(
    val title: String,
    val releases: List<Release>,
)

data class Release(
    val tag: String?,
    val title: String,
    val summary: String?,
    val date: LocalDateTime,
    val sections: List<Section>,
    val releaseUrl: String?,
    val diffUrl: String?,
)

data class Section(
    val title: String,
    val order: Int,
    val issues: List<Issue>,
)

data class Issue(
    val number: Int,
    val title: String,
    val body: String?,
    val closedAt: Instant,
    val labels: List<String>,
    val url: String,
    val author: User,
    val isPullRequest: Boolean,
    val milestone: Milestone? = null,
)

data class Milestone(
    val title: String,
    val description: String?,
)

data class User(
    val login: String,
    val profileUrl: String,
)

data class Tag(
    val name: String,
    val date: Instant,
)
