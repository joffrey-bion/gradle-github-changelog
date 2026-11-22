package org.hildan.github.changelog

import org.hildan.github.changelog.builder.*
import org.hildan.github.changelog.formatter.*
import org.hildan.github.changelog.github.*
import java.nio.file.*
import kotlin.io.path.*

data class GitHubChangelogGeneratorConfig(
    val gitHubConfig: GitHubConfig,
    val changelogConfig: ChangelogConfig,
    val formatter: MarkdownFormatter = MarkdownFormatter(),
)

class GitHubChangeLogGenerator(private val config: GitHubChangelogGeneratorConfig) {

    private val changeLogBuilder = ChangelogBuilder(config.changelogConfig)

    fun generate(outputFile: Path, releaseBodyFile: Path? = null) {
        val repo = fetchRepositoryInfo(config.gitHubConfig)
        val changeLog = changeLogBuilder.createChangeLog(repo)

        val markdown = config.formatter.formatChangeLog(changeLog)
        outputFile.createParentDirectories().writeText(markdown)

        if (releaseBodyFile != null) {
            val latestRelease = changeLog.releases.firstOrNull()
                ?: error("Can't generate release notes file because there are no releases and 'showUnreleased' is disabled.")
            val releaseNotes = config.formatter.formatReleaseBody(latestRelease)
            releaseBodyFile.createParentDirectories().writeText(releaseNotes)
        }
    }
}
