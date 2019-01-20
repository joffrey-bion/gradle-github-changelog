package org.hildan.github.changelog.generator

import java.time.LocalDate

data class ChangeLog(
    val title: String,
    val releases: List<Release>
)

data class Release(
    /** The tag of this release. Null if this object represents unreleased issues. */
    val tag: String?,
    /** The tag of the previous release. Null if this is the first release. */
    val previousTag: String?,
    val date: LocalDate,
    val sections: List<Section>,
    val releaseUrl: String?,
    val changeLogUrl: String?
)

data class Section(
    val title: String,
    val issues: List<Issue>
)

data class Issue(
    val number: Int,
    val title: String,
    val url: String,
    val authorLogin: String,
    val isPullRequest: Boolean
)
