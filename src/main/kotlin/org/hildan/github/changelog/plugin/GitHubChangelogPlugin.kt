package org.hildan.github.changelog.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
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
import org.hildan.github.changelog.generator.GitHubChangelogGenerator
import org.hildan.github.changelog.generator.SectionDefinition
import java.io.File
import javax.inject.Inject

const val EXTENSION_NAME = "changelog"
const val TASK_NAME = "generateChangelog"

open class GitHubChangelogPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val ext = project.extensions.create(EXTENSION_NAME, GitHubChangelogExtension::class.java, project)
        project.tasks.create(TASK_NAME, GenerateChangelogTask::class.java, ext)
    }
}

open class GenerateChangelogTask @Inject constructor(
    private val ext: GitHubChangelogExtension
) : DefaultTask() {

    init {
        group = "documentation"
        description = "Generates the changelog of the project based on GitHub tags, issues and pull-requests."
    }

    @TaskAction
    fun generate() {
        val configuration = ext.toConfig()
        val outFile = ext.outputFile
        project.logger.info("Generating changelog into $outFile...")
        GitHubChangelogGenerator(configuration).generate(outFile)
    }
}

open class GitHubChangelogExtension(private val project: Project) {

    var githubUser: String? = null
    var githubToken: String? = null
    var githubRepository: String? = null

    var title: String = DEFAULT_CHANGELOG_TITLE
    var showUnreleased: Boolean = DEFAULT_SHOW_UNRELEASED
    var unreleasedVersionTitle: String? = null
    var sections: List<SectionDefinition> = DEFAULT_SECTIONS
    var defaultIssueSectionTitle: String = DEFAULT_ISSUES_SECTION_TITLE
    var defaultPrSectionTitle: String = DEFAULT_PR_SECTION_TITLE
    var includeLabels: List<String> = DEFAULT_INCLUDED_LABELS
    var excludeLabels: List<String> = DEFAULT_EXCLUDED_LABELS
    var sinceTag: String? = null
    var skipTags: List<String> = DEFAULT_SKIPPED_TAGS
    var releaseUrlTemplate: String? = null
    var diffUrlTemplate: String? = null

    var outputFile: File = File("${project.projectDir}/CHANGELOG.md")

    fun toConfig(): ChangelogConfig =
        ChangelogConfig(
            github = createGithubConfig(),
            globalHeader = title,
            showUnreleased = showUnreleased,
            futureVersion = unreleasedVersionTitle ?: project.versionOrNull() ?: DEFAULT_UNRELEASED_VERSION_TITLE,
            sections = sections,
            defaultIssueSectionTitle = defaultIssueSectionTitle,
            defaultPrSectionTitle = defaultPrSectionTitle,
            includeLabels = includeLabels,
            excludeLabels = excludeLabels,
            sinceTag = sinceTag,
            skipTags = skipTags,
            customReleaseUrlTemplate = releaseUrlTemplate,
            customDiffUrlTemplate = diffUrlTemplate
        )

    private fun createGithubConfig(): GitHubConfig {
        val user = githubUser ?: project.getPropOrEnv("githubUser", "GITHUB_USER")
            ?: throw GradleException("you must specify your github username for changelog generation")
        val repo = githubRepository ?: project.rootProject.name
        val token = githubToken ?: project.getPropOrEnv("githubToken", "GITHUB_TOKEN")

        return GitHubConfig(user, repo, token)
    }

    private fun Project.versionOrNull(): String? {
        val versionStr = version.toString()
        return if (versionStr == "unspecified") null else versionStr
    }
}

private fun Project.getPropOrEnv(propName: String, envVar: String? = null, defaultValue: String? = null): String? =
    findProperty(propName) as String? ?: System.getenv(envVar) ?: defaultValue
