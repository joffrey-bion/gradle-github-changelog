package org.hildan.github.changelog.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.hildan.github.changelog.generator.ChangelogConfig
import org.hildan.github.changelog.generator.DEFAULT_SECTIONS
import org.hildan.github.changelog.generator.GitHubConfig
import org.hildan.github.changelog.generator.GitHubChangelogGenerator
import org.hildan.github.changelog.generator.SectionDefinition
import java.io.File
import javax.inject.Inject

open class GitHubChangelogPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val ext = project.extensions.create("changelog", GitHubChangelogExtension::class.java, project)
        project.tasks.create("generateChangelog", GenerateChangelogTask::class.java, ext)
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
        project.logger.info("Generating changelog into ${ext.outputFile}...")
        GitHubChangelogGenerator(configuration).generate(ext.outputFile)
    }
}

open class GitHubChangelogExtension(project: Project) {

    var githubUser: String? = project.getPropOrEnv("githubUser", "GITHUB_USER")
    var githubToken: String? = project.getPropOrEnv("githubToken", "GITHUB_TOKEN")
    var githubRepository: String = project.rootProject.name

    var title: String = "Changelog"
    var unreleasedVersionTitle: String = "Unreleased"
    var showUnreleased: Boolean = true
    var futureVersion: String? = project.version.toString()
    var sections: List<SectionDefinition> = DEFAULT_SECTIONS
    var defaultIssueSectionTitle: String = "Closed issue:"
    var defaultPrSectionTitle: String = "Merged pull requests:"
    var includeLabels: List<String> = emptyList()
    var excludeLabels: List<String> = listOf("duplicate", "invalid", "question", "wontfix")
    var releaseUrlTemplate: String? = null
    var diffUrlTemplate: String? = null

    var outputFile: File = File("${project.projectDir}/CHANGELOG.md")

    fun toConfig(): ChangelogConfig =
        ChangelogConfig(
            github = createGithubConfig(),
            globalHeader = title,
            showUnreleased = showUnreleased,
            unreleasedTitle = unreleasedVersionTitle,
            futureVersion = futureVersion,
            sections = sections,
            defaultIssueSectionTitle = defaultIssueSectionTitle,
            defaultPrSectionTitle = defaultPrSectionTitle,
            includeLabels = includeLabels,
            excludeLabels = excludeLabels,
            customReleaseUrlTemplate = releaseUrlTemplate,
            customDiffUrlTemplate = diffUrlTemplate
        )

    private fun createGithubConfig(): GitHubConfig {
        return GitHubConfig(
            githubUser ?: throw GradleException("you must specify your github username for changelog generation"),
            githubRepository,
            githubToken
        )
    }
}

private fun Project.getPropOrEnv(propName: String, envVar: String? = null, defaultValue: String? = null): String? =
    findProperty(propName) as String? ?: System.getenv(envVar) ?: defaultValue
