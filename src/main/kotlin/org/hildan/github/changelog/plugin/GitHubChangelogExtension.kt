package org.hildan.github.changelog.plugin

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.hildan.github.changelog.generator.ChangelogConfig
import org.hildan.github.changelog.generator.DEFAULT_CHANGELOG_TITLE
import org.hildan.github.changelog.generator.DEFAULT_EXCLUDED_LABELS
import org.hildan.github.changelog.generator.DEFAULT_INCLUDED_LABELS
import org.hildan.github.changelog.generator.DEFAULT_ISSUES_SECTION_TITLE
import org.hildan.github.changelog.generator.DEFAULT_PR_SECTION_TITLE
import org.hildan.github.changelog.generator.DEFAULT_SECTIONS
import org.hildan.github.changelog.generator.DEFAULT_SHOW_UNRELEASED
import org.hildan.github.changelog.generator.DEFAULT_SKIPPED_TAGS
import org.hildan.github.changelog.generator.DEFAULT_UNRELEASED_VERSION_TITLE
import org.hildan.github.changelog.generator.GitHubConfig
import org.hildan.github.changelog.generator.SectionDefinition
import java.io.File

open class GitHubChangelogExtension(private val project: Project) {

    var githubUser: String? = null
    var githubToken: String? = null
    var githubRepository: String? = null

    var title: String = DEFAULT_CHANGELOG_TITLE
    var showUnreleased: Boolean = DEFAULT_SHOW_UNRELEASED
    var futureVersionTag: String? = null
    var unreleasedVersionTitle: String = DEFAULT_UNRELEASED_VERSION_TITLE
    var sections: List<SectionDefinition> = emptyList()
    var defaultIssueSectionTitle: String = DEFAULT_ISSUES_SECTION_TITLE
    var defaultPrSectionTitle: String = DEFAULT_PR_SECTION_TITLE
    var includeLabels: List<String> = DEFAULT_INCLUDED_LABELS
    var excludeLabels: List<String> = DEFAULT_EXCLUDED_LABELS
    var sinceTag: String? = null
    var skipTags: List<String> = DEFAULT_SKIPPED_TAGS
    var releaseUrlTemplate: String? = null
    var diffUrlTemplate: String? = null
    val releaseUrlTagTransform: (String) -> String = { it }
    val diffUrlTagTransform: (String) -> String = { it }

    var outputFile: File = File("${project.projectDir}/CHANGELOG.md")

    fun toConfig(): ChangelogConfig =
        ChangelogConfig(
            github = createGithubConfig(),
            globalHeader = title,
            showUnreleased = showUnreleased,
            futureVersionTag = futureVersionTag,
            unreleasedVersionTitle = unreleasedVersionTitle,
            sections = sections + DEFAULT_SECTIONS,
            defaultIssueSectionTitle = defaultIssueSectionTitle,
            defaultPrSectionTitle = defaultPrSectionTitle,
            includeLabels = includeLabels,
            excludeLabels = excludeLabels,
            sinceTag = sinceTag,
            skipTags = skipTags,
            customReleaseUrlTemplate = releaseUrlTemplate,
            customDiffUrlTemplate = diffUrlTemplate,
            releaseUrlTagTransform = releaseUrlTagTransform,
            diffUrlTagTransform = diffUrlTagTransform
        )

    private fun createGithubConfig(): GitHubConfig {
        val user = githubUser ?: project.getPropOrEnv("githubUser", "GITHUB_USER")
            ?: throw GradleException("You must specify your GitHub username for changelog generation, using the " +
                "githubUser project property, or the GITHUB_USER environment variable")
        val repo = githubRepository ?: project.rootProject.name
        val token = githubToken ?: project.getPropOrEnv("githubToken", "GITHUB_TOKEN")

        return GitHubConfig(user, repo, token)
    }
}

private fun Project.getPropOrEnv(propName: String, envVar: String? = null, defaultValue: String? = null): String? =
    findProperty(propName) as String? ?: System.getenv(envVar) ?: defaultValue
