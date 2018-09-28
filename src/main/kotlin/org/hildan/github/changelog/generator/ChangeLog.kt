package org.hildan.github.changelog.generator

import org.kohsuke.github.GHIssue
import java.time.Instant

data class ChangeLog(
    val title: String,
    val releases: List<Release>
)

data class Release(
    /**
     * The tag of this release. Null if this object represents unreleased issues.
     */
    val tag: String?,
    /**
     * The tag of the previous release. Null if this is the first release.
     */
    val previousTag: String?,
    val date: Instant,
    val sections: List<Section>
)

data class Section(
    val title: String,
    val issues: List<GHIssue>
)
