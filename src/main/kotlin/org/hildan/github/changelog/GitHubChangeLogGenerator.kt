package org.hildan.github.changelog

import org.hildan.github.changelog.builder.ChangelogBuilder
import org.hildan.github.changelog.builder.ChangelogConfig
import org.hildan.github.changelog.formatter.MarkdownFormatter
import org.hildan.github.changelog.github.GitHubConfig
import org.hildan.github.changelog.github.fetchRepositoryInfo
import java.io.File
import java.nio.file.Files

data class GitHubChangelogGeneratorConfig(
    val gitHubConfig: GitHubConfig,
    val changelogConfig: ChangelogConfig,
    val formatter: MarkdownFormatter = MarkdownFormatter(),
)

class GitHubChangeLogGenerator(private val config: GitHubChangelogGeneratorConfig) {

    private val changeLogBuilder = ChangelogBuilder(config.changelogConfig)

    fun generate(outputFile: File, releaseBodyFile: File? = null) {
        val repo = fetchRepositoryInfo(config.gitHubConfig)
        val changeLog = changeLogBuilder.createChangeLog(repo)

        val markdown = config.formatter.formatChangeLog(changeLog)
        outputFile.toPath().parent?.let { Files.createDirectories(it) }
        outputFile.writeText(markdown)

        if (releaseBodyFile != null) {
            val releaseNotes = config.formatter.formatReleaseBody(changeLog.releases.first())
            releaseBodyFile.toPath().parent?.let { Files.createDirectories(it) }
            releaseBodyFile.writeText(releaseNotes)
        }
    }
}

