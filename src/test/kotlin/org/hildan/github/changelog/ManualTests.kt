package org.hildan.github.changelog

import org.hildan.github.changelog.builder.ChangelogConfig
import org.hildan.github.changelog.github.GitHubConfig
import java.io.File

fun main() {
    val gitHubConfig = GitHubConfig(
        user = "joffrey-bion",
        repo = "gradle-github-changelog",
        token = System.getenv("GITHUB_TOKEN"),
    )
    val config = GitHubChangelogGeneratorConfig(
        gitHubConfig,
        ChangelogConfig(
            releaseUrlTemplate = gitHubConfig.releaseUrlTemplate,
            diffUrlTemplate = gitHubConfig.diffUrlTemplate,
        ),
    )
    GitHubChangeLogGenerator(config).generate(File("CHANGELOG.md"))
}
