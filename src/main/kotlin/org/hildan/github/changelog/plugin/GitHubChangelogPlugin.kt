package org.hildan.github.changelog.plugin

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.work.DisableCachingByDefault
import org.hildan.github.changelog.GitHubChangeLogGenerator
import org.hildan.github.changelog.GitHubChangelogGeneratorConfig
import org.hildan.github.changelog.builder.ChangelogConfig
import org.hildan.github.changelog.builder.DEFAULT_SECTIONS
import org.hildan.github.changelog.builder.SectionDefinition
import org.hildan.github.changelog.github.GitHubConfig
import kotlin.collections.plus

private const val EXTENSION_NAME = "changelog"
private const val TASK_NAME = "generateChangelog"

class GitHubChangelogPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val ext = project.extensions.create(EXTENSION_NAME, GitHubChangelogExtension::class.java)
        val providers = project.providers
        ext.githubUser.convention(providers.gradleProperty("githubUser").orElse(providers.environmentVariable("GITHUB_USER")))
        ext.githubToken.convention(providers.gradleProperty("githubToken").orElse(providers.environmentVariable("GITHUB_TOKEN")))
        ext.githubRepository.convention(project.rootProject.name)
        ext.outputFile.convention(project.layout.projectDirectory.file("CHANGELOG.md"))
        ext.latestReleaseBodyFile.convention(project.layout.buildDirectory.file("reports/changelog/latest-release-body.md"))

        val gitHubConfigProvider = project.provider {
            val user = ext.githubUser.orNull
                ?: throw GradleException("You must specify your GitHub username for changelog generation, using the " +
                    "githubUser project property, the githubUser property of the changelog extension, or the GITHUB_USER " +
                    "environment variable")
            GitHubConfig(user, ext.githubRepository.get(), ext.githubToken.orNull)
        }

        project.tasks.register<GenerateChangelogTask>(TASK_NAME) {
            group = "documentation"
            description = "Generates the changelog of the project based on GitHub tags, issues and pull-requests."
            // The GitHub state that drives the changelog is invisible to Gradle's input fingerprints,
            // so the task must never be skipped as up-to-date.
            outputs.upToDateWhen { false }
            gitHubConfig.set(gitHubConfigProvider)
            changelogConfig.set(gitHubConfigProvider.map { ext.toChangelogConfig(it) })
            outputFile.set(ext.outputFile)
            latestReleaseBodyFile.set(ext.latestReleaseBodyFile)
        }
    }
}

@DisableCachingByDefault(because = "The change log depends on external GitHub issues state that may change over time")
abstract class GenerateChangelogTask : DefaultTask() {

    @get:Internal
    abstract val gitHubConfig: Property<GitHubConfig>
    @get:Internal
    abstract val changelogConfig: Property<ChangelogConfig>
    @get:OutputFile
    abstract val outputFile: RegularFileProperty
    @get:OutputFile
    abstract val latestReleaseBodyFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val config = GitHubChangelogGeneratorConfig(
            gitHubConfig = gitHubConfig.get(),
            changelogConfig = changelogConfig.get(),
        )

        val outFile = outputFile.get().asFile
        logger.info("Generating changelog into $outFile...")
        GitHubChangeLogGenerator(config).generate(outFile.toPath(), latestReleaseBodyFile.get().asFile.toPath())
    }
}

internal fun GitHubChangelogExtension.toChangelogConfig(gitHub: GitHubConfig): ChangelogConfig = ChangelogConfig(
    globalHeader = title.get(),
    showUnreleased = showUnreleased.get(),
    futureVersionTag = futureVersionTag.orNull,
    unreleasedVersionTitle = unreleasedVersionTitle.get(),
    sections = DEFAULT_SECTIONS + sections.get(),
    defaultIssueSection = SectionDefinition(defaultIssueSectionTitle.get(), defaultIssueSectionOrder.get(), emptyList()),
    defaultPrSection = SectionDefinition(defaultPrSectionTitle.get(), defaultPrSectionOrder.get(), emptyList()),
    includeLabels = includeLabels.get(),
    excludeLabels = excludeLabels.get(),
    sinceTag = sinceTag.orNull,
    skipTags = skipTags.get(),
    skipTagsRegex = skipTagsRegex.get(),
    releaseUrlTemplate = releaseUrlTemplate.orNull ?: gitHub.releaseUrlTemplate,
    diffUrlTemplate = diffUrlTemplate.orNull ?: gitHub.diffUrlTemplate,
    releaseUrlTagTransform = releaseUrlTagTransform,
    diffUrlTagTransform = diffUrlTagTransform,
    customTagByIssueNumber = customTagByIssueNumber.get(),
    useMilestoneAsTag = useMilestoneAsTag.get(),
    timezone = timezone.get(),
)
