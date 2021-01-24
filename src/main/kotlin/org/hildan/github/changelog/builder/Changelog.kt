package org.hildan.github.changelog.builder

import java.time.Instant
import java.time.LocalDateTime

data class Changelog(
    val title: String,
    val releases: List<Release>
)

data class Release(
    val tag: String?,
    val title: String,
    val date: LocalDateTime,
    val sections: List<Section>,
    val releaseUrl: String?,
    val diffUrl: String?
)

data class Section(
    val title: String,
    val issues: List<Issue>
)

data class Issue(
    val number: Int,
    val title: String,
    val closedAt: Instant,
    val labels: List<String>,
    val url: String,
    val author: User,
    val isPullRequest: Boolean
)

data class User(
    val login: String,
    val profileUrl: String
)

data class Tag(
    val name: String,
    val date: Instant
)
