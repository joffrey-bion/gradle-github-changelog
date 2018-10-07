package org.hildan.github.changelog.generator

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

open class GithubChangelogPlugin : Plugin<Project> {

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
        val generator = GithubChangelogGenerator(configuration, ext.outputFile)
        generator.generate()
    }
}

open class GitHubChangelogExtension(project: Project) {

    val github: GitHubInfo = project.objects.newInstance(GitHubInfo::class.java, project)
    var outputFile: File = File("${project.projectDir}/CHANGELOG.md")

    fun toConfig(): Configuration = Configuration(github.toGitHubConfig())
}

open class GitHubInfo @Inject constructor(project: Project) {

    var user: String? = project.getPropOrEnv("githubUser", "GITHUB_USER")
    var token: String? = project.getPropOrEnv("githubToken", "GITHUB_TOKEN")
    var repo: String = project.name

    fun toGitHubConfig(): GitHubConfig = GitHubConfig(
        user ?: throw GradleException("you must specify your github username for changelog generation"),
        token ?: throw GradleException("you must specify your github token for changelog generation"),
        repo
    )
}

private fun Project.getPropOrEnv(propName: String, envVar: String? = null, defaultValue: String? = null): String? =
    if (hasProperty(propName)) {
        property(propName) as String
    } else {
        System.getenv(envVar) ?: defaultValue
    }
