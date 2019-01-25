package org.hildan.github.changelog.generator

import java.time.Instant
import java.time.LocalDateTime

data class ChangeLog(
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
    val authorLogin: String,
    val isPullRequest: Boolean
)

data class Tag(
    val name: String,
    val date: Instant
)
