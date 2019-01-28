package org.hildan.github.changelog.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.hildan.github.changelog.generator.GitHubChangelogGenerator
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

