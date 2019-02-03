package org.hildan.github.changelog

import org.hildan.github.changelog.builder.ChangeLogBuilder
import org.hildan.github.changelog.builder.ChangelogConfig
import org.hildan.github.changelog.formatter.MarkdownFormatter
import org.hildan.github.changelog.github.GitHubConfig
import org.hildan.github.changelog.github.fetchRepositoryInfo
import java.io.File

data class GitHubChangeLogGeneratorConfig(
    val gitHubConfig: GitHubConfig,
    val changeLogConfig: ChangelogConfig,
    val formatter: MarkdownFormatter = MarkdownFormatter()
)

class GitHubChangeLogGenerator(private val config: GitHubChangeLogGeneratorConfig) {

    private val changeLogBuilder = ChangeLogBuilder(config.changeLogConfig)

    fun generate(outputFile: File? = null) {
        val repo = fetchRepositoryInfo(config.gitHubConfig)
        val changeLog = changeLogBuilder.createChangeLog(repo.closedIssues, repo.tags)
        val markdown = config.formatter.formatChangeLog(changeLog)
        if (outputFile != null) {
            outputFile.writeText(markdown)
        } else {
            print(markdown)
        }
    }
}

