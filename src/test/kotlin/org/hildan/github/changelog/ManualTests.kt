package org.hildan.github.changelog

import org.hildan.github.changelog.builder.ChangelogConfig
import org.hildan.github.changelog.github.GitHubConfig
import kotlin.io.path.Path

fun main() {
    val gitHubConfig = GitHubConfig(
        user = "joffrey-bion",
        repo = "gradle-github-changelog",
        token = System.getenv("GITHUB_TOKEN"),
    )
    val config = GitHubChangelogGeneratorConfig(
        gitHubConfig = gitHubConfig,
        changelogConfig = ChangelogConfig(
            releaseUrlTemplate = gitHubConfig.releaseUrlTemplate,
            diffUrlTemplate = gitHubConfig.diffUrlTemplate,
        ),
    )
    GitHubChangeLogGenerator(config).generate(Path("CHANGELOG.md"))
}
