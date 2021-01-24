package org.hildan.github.changelog.plugin

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.hildan.github.changelog.GitHubChangelogGeneratorConfig
import org.hildan.github.changelog.builder.*
import org.hildan.github.changelog.github.GitHubConfig
import java.io.File
import java.time.ZoneId

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
    var releaseUrlTagTransform: (String) -> String = DEFAULT_RELEASE_URL_TAG_TRANSFORM
    var diffUrlTagTransform: (String) -> String = DEFAULT_DIFF_URL_TAG_TRANSFORM
    var customTagByIssueNumber: Map<Int, String> = DEFAULT_CUSTOM_TAG_BY_ISSUE_NUMBER
    var timezone: ZoneId = DEFAULT_TIMEZONE

    var outputFile: File = File("${project.projectDir}/CHANGELOG.md")

    fun toConfig(): GitHubChangelogGeneratorConfig {
        val gitHub = createGitHubConfig()
        val changeLog = createChangeLogConfig(gitHub)
        return GitHubChangelogGeneratorConfig(gitHub, changeLog)
    }

    private fun createGitHubConfig(): GitHubConfig {
        val user = githubUser
            ?: project.getPropOrEnv("githubUser", "GITHUB_USER")
            ?: throw GradleException("You must specify your GitHub username for changelog generation, using the " +
                "githubUser project property, the githubUser property of the changelog extension, or the GITHUB_USER " +
                "environment variable")
        val repo = githubRepository ?: project.rootProject.name
        val token = githubToken ?: project.getPropOrEnv("githubToken", "GITHUB_TOKEN")

        return GitHubConfig(user, repo, token)
    }

    private fun createChangeLogConfig(gitHub: GitHubConfig): ChangelogConfig = ChangelogConfig(
        globalHeader = title,
        showUnreleased = showUnreleased,
        futureVersionTag = futureVersionTag,
        unreleasedVersionTitle = unreleasedVersionTitle,
        sections = DEFAULT_SECTIONS + sections,
        defaultIssueSectionTitle = defaultIssueSectionTitle,
        defaultPrSectionTitle = defaultPrSectionTitle,
        includeLabels = includeLabels,
        excludeLabels = excludeLabels,
        sinceTag = sinceTag,
        skipTags = skipTags,
        releaseUrlTemplate = releaseUrlTemplate ?: gitHub.releaseUrlTemplate,
        diffUrlTemplate = diffUrlTemplate ?: gitHub.diffUrlTemplate,
        releaseUrlTagTransform = releaseUrlTagTransform,
        diffUrlTagTransform = diffUrlTagTransform,
        customTagByIssueNumber = customTagByIssueNumber,
        timezone = timezone,
    )
}

private fun Project.getPropOrEnv(propName: String, envVar: String): String? =
    findProperty(propName) as String? ?: System.getenv(envVar)
