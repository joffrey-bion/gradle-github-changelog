package org.hildan.github.changelog.plugin

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.hildan.github.changelog.GitHubChangelogGeneratorConfig
import org.hildan.github.changelog.builder.*
import org.hildan.github.changelog.github.GitHubConfig
import java.io.File
import java.time.ZoneId

open class GitHubChangelogExtension(private val project: Project) {
    /**
     * Your GitHub username.
     * It is mandatory, but you can also provide it via the GITHUB_USER environment variable, or the `githubUser`
     * Gradle property (from `gradle.properties`).
     */
    var githubUser: String? = null
    /**
     * GitHub only allows 50 unauthenticated requests per hour.
     * By providing an API token, you allow this plugin to log in and thus remove the limit.
     * If you don't have one yet, you may generate a personal token for your repo.
     * You don't need to tick any permissions for the plugin to work, it only accesses public stuff.
     */
    var githubToken: String? = null
    /**
     * The repository from which to get the issues to generate the change log.
     * Defaults to the name of the root project.
     */
    var githubRepository: String? = null

    /**
     * The title of the change log.
     */
    var title: String = DEFAULT_CHANGELOG_TITLE
    /**
     * If true, issues that were closed since the last tag will appear at the top of the change log.
     * By default they will appear as "unreleased", unless a custom [unreleasedVersionTitle] or a [futureVersionTag] is
     * provided.
     */
    var showUnreleased: Boolean = DEFAULT_SHOW_UNRELEASED
    /**
     * If provided, and if [showUnreleased] is true, the unreleased issues will appear at the top of the change log
     * under the provided tag (even if this tag doesn't exist yet).
     * This allows to consider unreleased issues as part of an actual tag prior to actually creating the tag.
     * It is useful during a release build.
     */
    var futureVersionTag: String? = null
    /**
     * The title for the unreleased issues at the top of the change log.
     * Ignored if [futureVersionTag] is provided.
     * This title will only be visible if [showUnreleased] is `true`.
     */
    var unreleasedVersionTitle: String = DEFAULT_UNRELEASED_VERSION_TITLE
    /**
     * Custom sections to classify the issues within each release.
     *
     * The section definitions are used to build a label-to-section mapping.
     * Issues are placed into a section according to the first of their labels that is associated to a section.
     *
     * The provided custom sections are appended to the default sections (they don't replace them).
     * However, if a custom section is associated to a label that is usually handled by a default section, the custom
     * section takes precedence.
     * In fact, the last section defining a mapping for a given issue label wins, and default sections are listed first.
     */
    var sections: List<SectionDefinition> = emptyList()
    /**
     * Section title for issues that are not classified in a specific section due to their labels.
     */
    var defaultIssueSectionTitle: String = DEFAULT_ISSUES_SECTION_TITLE
    /**
     * Section title for pull-requests that are not classified in a specific section due to their labels
     */
    var defaultPrSectionTitle: String = DEFAULT_PR_SECTION_TITLE
    /**
     * If not empty, only issues that have at least one of these labels can appear in the change log.
     */
    var includeLabels: List<String> = DEFAULT_INCLUDED_LABELS
    /**
     * Issues that have at least one of these labels will not appear in the change log, even if they have labels that
     * are present in [includeLabels].
     */
    var excludeLabels: List<String> = DEFAULT_EXCLUDED_LABELS
    /**
     * If provided, all prior tags will be excluded from the change log.
     */
    var sinceTag: String? = null
    /**
     * Some specific tags to exclude from the change log.
     *
     * The issues that are part of the excluded tags are also excluded from the change log.
     * They are not reported under the next tag.
     */
    var skipTags: List<String> = DEFAULT_SKIPPED_TAGS
    /**
     * Tags matching one of these regexes are excluded from the change log.
     *
     * The issues that are part of the excluded tags are also excluded from the change log.
     * They are not reported under the next tag.
     */
    var skipTagsRegex: List<Regex> = DEFAULT_SKIPPED_TAGS_REGEX
    /**
     * Custom template for the URL of releases to use in the hyperlink on the title.
     * If present, a `%s` placeholder will be replaced by the tag of the release.
     * By default, it points to the source code of the git repository at the given tag.
     */
    var releaseUrlTemplate: String? = null
    /**
     * Custom template for the URL to the full diff of the release.
     * If present, 2 `%s` placeholders are replaced by the tag of the previous release and the current release, respectively.
     * If you need to reverse the order, you may use `%1$s` for the "from" (previous) tag, and `%2$s` for the "to" (current) tag.
     */
    var diffUrlTemplate: String? = null
    /**
     * A function to transform the tag string before injection in the [releaseUrlTemplate].
     * By default, this is the identity function and doesn't change the tag.
     * It may be handy to remove or add a "v" prefix for instance.
     */
    var releaseUrlTagTransform: (String) -> String = DEFAULT_RELEASE_URL_TAG_TRANSFORM
    /**
     * A function to transform the tag strings before injection in the diffUrlTemplate.
     * By default, this is the identity function and doesn't change the tags.
     * It may be handy to remove or add a "v" prefix for instance.
     */
    var diffUrlTagTransform: (String) -> String = DEFAULT_DIFF_URL_TAG_TRANSFORM
    /**
     * A mapping from issue numbers to tags.
     * An issue may be incorrectly classified due to late closing date or other timing problems.
     * If this is the case, use this map to override the tag to use for a particular issue.
     */
    var customTagByIssueNumber: Map<Int, String> = DEFAULT_CUSTOM_TAG_BY_ISSUE_NUMBER
    /**
     * If true, issues associated to a milestone with a title that matches a tag will be associated to that tag,
     * regardless of their close date.
     */
    var useMilestoneAsTag: Boolean = DEFAULT_USE_MILESTONE_AS_TAG
    /**
     * The timezone used to convert the tags timestamps to local dates for releases (defaults to GMT).
     */
    var timezone: ZoneId = DEFAULT_TIMEZONE

    /**
     * The file to write the change log to.
     */
    var outputFile: File = File("${project.projectDir}/CHANGELOG.md")

    internal fun toConfig(): GitHubChangelogGeneratorConfig {
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
        skipTagsRegex = skipTagsRegex,
        releaseUrlTemplate = releaseUrlTemplate ?: gitHub.releaseUrlTemplate,
        diffUrlTemplate = diffUrlTemplate ?: gitHub.diffUrlTemplate,
        releaseUrlTagTransform = releaseUrlTagTransform,
        diffUrlTagTransform = diffUrlTagTransform,
        customTagByIssueNumber = customTagByIssueNumber,
        useMilestoneAsTag = useMilestoneAsTag,
        timezone = timezone,
    )
}

private fun Project.getPropOrEnv(propName: String, envVar: String): String? =
    findProperty(propName) as String? ?: System.getenv(envVar)
