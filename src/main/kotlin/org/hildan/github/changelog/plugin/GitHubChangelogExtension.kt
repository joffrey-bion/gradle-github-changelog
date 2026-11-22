package org.hildan.github.changelog.plugin

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import org.hildan.github.changelog.builder.*
import java.time.ZoneId
import javax.inject.Inject

open class GitHubChangelogExtension @Inject constructor(objectFactory: ObjectFactory) {
    /**
     * Your GitHub username. It is mandatory, but you can also provide it via the GITHUB_USER environment variable, or
     * the `githubUser` Gradle property (from `gradle.properties`).
     */
    val githubUser: Property<String> = objectFactory.property()

    /**
     * GitHub only allows 50 unauthenticated requests per hour. By providing an API token, you allow this plugin to log
     * in and thus remove the limit. If you don't have one yet, you may generate a personal token for your repo. You
     * don't need to tick any permissions for the plugin to work, it only accesses public stuff.
     */
    val githubToken: Property<String> = objectFactory.property()

    /**
     * The repository from which to get the issues to generate the change log. Defaults to the name of the root project.
     */
    val githubRepository: Property<String> = objectFactory.property()

    /**
     * The title of the change log.
     */
    val title: Property<String> = objectFactory.property<String>().convention(DEFAULT_CHANGELOG_TITLE)

    /**
     * If true, issues that were closed since the last tag will appear at the top of the change log. By default, they
     * will appear as "unreleased", unless a custom [unreleasedVersionTitle] or a [futureVersionTag] is provided.
     */
    val showUnreleased: Property<Boolean> = objectFactory.property<Boolean>().convention(DEFAULT_SHOW_UNRELEASED)

    /**
     * If provided, and if [showUnreleased] is true, the unreleased issues will appear at the top of the change log
     * under the provided tag (even if this tag doesn't exist yet). This allows to consider unreleased issues as part of
     * an actual tag prior to actually creating the tag. It is useful during a release build.
     */
    val futureVersionTag: Property<String> = objectFactory.property()

    /**
     * The title for the unreleased issues at the top of the change log. Ignored if [futureVersionTag] is provided. This
     * title will only be visible if [showUnreleased] is `true`.
     */
    val unreleasedVersionTitle: Property<String> =
        objectFactory.property<String>().convention(DEFAULT_UNRELEASED_VERSION_TITLE)

    /**
     * Custom sections to classify the issues within each release.
     *
     * The section definitions are used to build a label-to-section mapping. When several of an issue's labels are
     * associated to sections, it is placed in the section with the smallest order, i.e. the one that appears first in
     * the changelog.
     *
     * The provided custom sections are appended to the default sections (they don't replace them). However, if a custom
     * section is associated to a label that is usually handled by a default section, the custom section takes
     * precedence. In fact, the last section defining a mapping for a given issue label wins, and default sections are
     * listed first.
     */
    val sections: ListProperty<SectionDefinition> = objectFactory.listProperty()

    /**
     * Section title for issues that are not classified in a specific section due to their labels.
     */
    val defaultIssueSectionTitle: Property<String> =
        objectFactory.property<String>().convention(DEFAULT_ISSUES_SECTION_TITLE)

    /**
     * Section order for issues that are not classified in a specific section due to their labels. Smaller orders appear
     * first.
     */
    val defaultIssueSectionOrder: Property<Int> =
        objectFactory.property<Int>().convention(DEFAULT_ISSUES_SECTION_ORDER)

    /**
     * Section title for pull-requests that are not classified in a specific section due to their labels
     */
    val defaultPrSectionTitle: Property<String> =
        objectFactory.property<String>().convention(DEFAULT_PR_SECTION_TITLE)

    /**
     * Section order for pull-requests that are not classified in a specific section due to their labels. Smaller orders
     * appear first.
     */
    val defaultPrSectionOrder: Property<Int> = objectFactory.property<Int>().convention(DEFAULT_PR_SECTION_ORDER)

    /**
     * If not empty, only issues that have at least one of these labels can appear in the change log.
     */
    val includeLabels: SetProperty<String> = objectFactory.setProperty<String>().convention(DEFAULT_INCLUDED_LABELS)

    /**
     * Issues that have at least one of these labels will not appear in the change log, even if they have labels that
     * are present in [includeLabels].
     */
    val excludeLabels: SetProperty<String> = objectFactory.setProperty<String>().convention(DEFAULT_EXCLUDED_LABELS)

    /**
     * If provided, all prior tags will be excluded from the change log.
     */
    val sinceTag: Property<String> = objectFactory.property()

    /**
     * Some specific tags to exclude from the change log.
     *
     * The issues that are part of the excluded tags are also excluded from the change log. They are not reported under
     * the next tag.
     */
    val skipTags: SetProperty<String> = objectFactory.setProperty<String>().convention(DEFAULT_SKIPPED_TAGS)

    /**
     * Tags matching one of these regexes are excluded from the change log.
     *
     * The issues that are part of the excluded tags are also excluded from the change log. They are not reported under
     * the next tag.
     */
    val skipTagsRegex: ListProperty<Regex> = objectFactory.listProperty<Regex>().convention(DEFAULT_SKIPPED_TAGS_REGEX)

    /**
     * Custom template for the URL of releases to use in the hyperlink on the title. If present, a `%s` placeholder will
     * be replaced by the tag of the release. By default, it points to the source code of the git repository at the
     * given tag.
     */
    val releaseUrlTemplate: Property<String> = objectFactory.property()

    /**
     * Custom template for the URL to the full diff of the release. If present, 2 `%s` placeholders are replaced by the
     * tag of the previous release and the current release, respectively. If you need to reverse the order, you may use
     * `%1$s` for the "from" (previous) tag, and `%2$s` for the "to" (current) tag.
     */
    val diffUrlTemplate: Property<String> = objectFactory.property()

    /**
     * A function to transform the tag string before injection in the [releaseUrlTemplate]. By default, this is the
     * identity function and doesn't change the tag. It may be handy to remove or add a "v" prefix for instance.
     */
    var releaseUrlTagTransform: (String) -> String = DEFAULT_RELEASE_URL_TAG_TRANSFORM

    /**
     * A function to transform the tag strings before injection in the diffUrlTemplate. By default, this is the identity
     * function and doesn't change the tags. It may be handy to remove or add a "v" prefix for instance.
     */
    var diffUrlTagTransform: (String) -> String = DEFAULT_DIFF_URL_TAG_TRANSFORM

    /**
     * A mapping from issue numbers to tags. An issue may be incorrectly classified due to late closing date or other
     * timing problems. If this is the case, use this map to override the tag to use for a particular issue.
     */
    val customTagByIssueNumber: MapProperty<Int, String> = objectFactory.mapProperty()

    /**
     * If true, issues associated to a milestone with a title that matches a tag will be associated to that tag,
     * regardless of their close date.
     */
    val useMilestoneAsTag: Property<Boolean> =
        objectFactory.property<Boolean>().convention(DEFAULT_USE_MILESTONE_AS_TAG)

    /**
     * The timezone used to convert the tags timestamps to local dates for releases (defaults to GMT).
     */
    val timezone: Property<ZoneId> = objectFactory.property<ZoneId>().convention(DEFAULT_TIMEZONE)

    /**
     * The file to write the change log to.
     */
    val outputFile: RegularFileProperty = objectFactory.fileProperty()

    /**
     * The file to write the change log of the unreleased changes to. This is meant to be used as body for the new
     * GitHub release.
     */
    val latestReleaseBodyFile: RegularFileProperty = objectFactory.fileProperty()
}
