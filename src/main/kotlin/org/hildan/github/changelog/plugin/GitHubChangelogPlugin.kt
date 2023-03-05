package org.hildan.github.changelog.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.*
import org.hildan.github.changelog.GitHubChangeLogGenerator
import ru.vyarus.gradle.plugin.github.*
import javax.inject.Inject

private const val EXTENSION_NAME = "changelog"
private const val TASK_NAME = "generateChangelog"

open class GitHubChangelogPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val ext = project.extensions.create<GitHubChangelogExtension>(EXTENSION_NAME, project)
        project.tasks.register<GenerateChangelogTask>(TASK_NAME, ext)

        project.pluginManager.withPlugin("ru.vyarus.github-info") {
            val github = project.extensions.getByType<GithubInfoExtension>()
            if (ext.githubUser == null) {
                ext.githubUser = github.user
            }
        }
    }
}

open class GenerateChangelogTask @Inject constructor(private val ext: GitHubChangelogExtension) : DefaultTask() {

    init {
        group = "documentation"
        description = "Generates the changelog of the project based on GitHub tags, issues and pull-requests."
    }

    @TaskAction
    fun generate() {
        val configuration = ext.toConfig()
        val outFile = ext.outputFile
        project.logger.info("Generating changelog into $outFile...")
        GitHubChangeLogGenerator(configuration).generate(outFile, ext.latestReleaseBodyFile)
    }
}

