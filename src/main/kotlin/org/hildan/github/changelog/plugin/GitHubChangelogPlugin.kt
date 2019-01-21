package org.hildan.github.changelog.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.hildan.github.changelog.generator.Configuration
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
        val generator = GitHubChangelogGenerator(configuration, ext.outputFile)
        generator.generate()
    }
}

open class GitHubChangelogExtension(project: Project) {

    var githubUser: String? = project.getPropOrEnv("githubUser", "GITHUB_USER")
    var githubToken: String? = project.getPropOrEnv("githubToken", "GITHUB_TOKEN")
    var githubRepository: String = project.rootProject.name

    var title: String = "Changelog"
    var releaseUrlTemplate: String? = null
    var unreleasedVersionTitle: String = "Unreleased"
    var showUnreleased: Boolean = true
    var futureVersion: String? = project.version.toString()
    var sections: List<SectionDefinition> = DEFAULT_SECTIONS
    var defaultIssueSectionTitle: String = "Closed issue:"
    var defaultPrSectionTitle: String = "Merged pull requests:"
    var includeLabels: List<String> = emptyList()
    var excludeLabels: List<String> = listOf("duplicate", "invalid", "question", "wontfix")

    var outputFile: File = File("${project.projectDir}/CHANGELOG.md")

    fun toConfig(): Configuration =
        Configuration(
            github = createGithubConfig(),
            globalHeader = title,
            unreleasedTitle = unreleasedVersionTitle,
            futureVersion = futureVersion,
            sections = sections,
            showUnreleased = showUnreleased,
            defaultIssueSectionTitle = defaultIssueSectionTitle,
            defaultPrSectionTitle = defaultPrSectionTitle,
            includeLabels = includeLabels,
            excludeLabels = excludeLabels
        )

    private fun createGithubConfig(): GitHubConfig {
        val githubConfig = GitHubConfig(
            githubUser ?: throw GradleException("you must specify your github username for changelog generation"),
            githubToken,
            githubRepository
        )
        return releaseUrlTemplate?.let { githubConfig.copy(releaseUrlTemplate = it) } ?: githubConfig
    }
}

private fun Project.getPropOrEnv(propName: String, envVar: String? = null, defaultValue: String? = null): String? =
    findProperty(propName) as String? ?: System.getenv(envVar) ?: defaultValue
