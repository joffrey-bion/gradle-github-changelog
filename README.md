# GitHub Changelog Generator (Gradle plugin)

[![Gradle plugin version](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/org/hildan/github/changelog/org.hildan.github.changelog.gradle.plugin/maven-metadata.xml.svg?label=gradle&logo=gradle)](https://plugins.gradle.org/plugin/org.hildan.github.changelog)
[![Github Build](https://img.shields.io/github/workflow/status/joffrey-bion/gradle-github-changelog/CI%20Build?label=build&logo=github)](https://github.com/joffrey-bion/gradle-github-changelog/actions?query=workflow%3A%22CI+Build%22)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/joffrey-bion/gradle-github-changelog/blob/master/LICENSE)

Generates a changelog from GitHub issues and pull-requests.

This project is similar (in functionality and output) to the great 
[GitHub Changelog Generator](https://github.com/github-changelog-generator/github-changelog-generator),
but as a Gradle plugin.

When applied, the plugin automatically adds the `generateChangelog` task.
This task calls GitHub to get information about your issues and pull requests,
and generates a `CHANGELOG.md` file in your project's root folder.

## How it works

The releases in the changelog are determined by the git tags of the repository.
The date of the release is the date of the tag.

Closed issues are sorted into releases using their close date, but this behaviour can be overridden.

More specifically, the following rules apply (by order of precedence):

1. if the issue is forced into a tag via `customTagByIssueNumber` mapping, it is put under this tag
2. if the issue has a milestone, and the title of that milestone matches a git tag, then the issue is put under this tag
3. otherwise, the issue is put under the first tag that follows its close date

For example, all issues closed between the date of tag `1.0` and the date of the tag `1.1` are considered to be in release `1.1`.

To see an example output, take a look at this project's [CHANGELOG.md](CHANGELOG.md), which was generated with this plugin.

## Usage

### Applying the plugin

Add the following line to your `plugins` block:

```kotlin
plugins {
    id("org.hildan.github.changelog") version "<version>"
}
```

Where `<version>` is the desired version (see badge above without `v` prefix), e.g. `1.3.0`.

Gradle compatibility:

|    Plugin     | Gradle |
|:-------------:|:------:|
|     1.9.0     |  7.3+  |
|     1.8.0     |  7.2+  |
| 1.0.0 - 1.7.0 |  6.8+  |
|    < 1.0.0    |  6.7+  |

### Minimal configuration

The minimal required configuration is your **GitHub username**.

It can be provided in 3 different ways depending on your needs:
- the `githubUser` project property (add `githubUser=user-or-org` to `gradle.properties`)
- the `GITHUB_USER` environment variable
- the `githubUser` property of the `changelog` DSL (see below)

If you're using the project property or the environment variable, and stick with the defaults, you don't even need 
to define the `changelog` extension at all in your gradle file.

You can then simply run:
```
gradle generateChangelog
```

### Complete configuration

Here is the extension DSL with all options and their default values:

```groovy
changelog {
    githubUser = // [mandatory] project property "githubUser" or env variable "GITHUB_USER"
    githubToken = null // [optional] project property "githubToken" or env variable "GITHUB_TOKEN"
    githubRepository = rootProject.name
    
    title = "Change Log"
    showUnreleased = true
    unreleasedVersionTitle = "Unreleased"
    futureVersionTag = null
    sections = [] // no custom sections by default, but default sections are prepended
    defaultIssueSectionTitle = "Closed issues:"
    defaultPrSectionTitle = "Merged pull requests:"
    includeLabels = []
    excludeLabels = ["duplicate", "invalid", "question", "wontfix"]
    sinceTag = null
    skipTags = []
    skipTagsRegex = []
    releaseUrlTemplate = null // defaults to "https://github.com/$user/$repo/tree/%s"
    diffUrlTemplate = null // defaults to "https://github.com/$user/$repo/compare/%s...%s"
    releaseUrlTagTransform = { it }
    diffUrlTagTransform = { it }
    customTagByIssueNumber = [:]
    useMilestoneAsTag = true
    timezone = ZoneId.of("GMT")
    
    outputFile = file("${projectDir}/CHANGELOG.md")
}
```

- `githubUser`: your GitHub username

- `githubToken`: GitHub only allows 50 unauthenticated requests per hour. By providing an API token, you allow this 
plugin to log in and thus remove the limit. If you don't have one yet, you may 
[generate a personal token](https://github.com/settings/tokens/new?description=GitHub%20Changelog%20Generator%20token) 
for your repo. You don't need to tick any permissions for the plugin to work on public repositories, but you need
the `repo` permission for private repositories ("full access to private repo").

- `githubRepository`: the repository from which to get the issues to generate the change log.

- `title`: the title of the change log

- `showUnreleased`: if true, issues that were closed since the last tag will appear at the top of the change log. 
By default they will appear as "unreleased", unless a `futureVersionTag` is provided.

- `unreleasedVersionTitle`: the title for the unreleased issues at the top of the change log. Ignored if 
`futureVersionTag` is provided.

- `futureVersionTag`: if provided, and if `showUnreleased` is true, the unreleased issues will appear at the top of 
the change log under the provided tag. This allows to consider unreleased issues as part of an actual tag prior to 
actually creating the tag.

- `sections`: custom sections to classify the issues within each release.
The section definitions are used to build a label-to-section mapping.
The custom sections should be provided as a list of `SectionDefinition`s, with a title and one or more associated
issue labels.
Issues are placed into a section according to the first of their labels that is associated to a section.
The provided custom sections are appended to the default sections (they don't replace them).
However, if a custom section is associated to a label that is usually handled by a default section, the custom
section takes precedence.
In fact, the last section defining a mapping for a given issue label wins, and default sections are listed first.

- `defaultIssueSectionTitle`: section title for issues that are not classified in a specific section due to their labels

- `defaultPrSectionTitle`: section title for pull-requests that are not classified in a specific section due to their 
labels

- `includeLabels`: if not empty, only issues that have at least one of these labels can appear in the change log.

- `excludeLabels`: issues that have at least one of these labels will not appear in the change log, even if they have 
  labels that are present in `includeLabels`.

- `sinceTag`: if provided, all prior tags will be excluded from the change log.

- `skipTags`: some specific tags to exclude from the change log. The issues that are part of the excluded tags are 
also excluded from the change log. They are not reported under the next tag.

- `skipTagsRegex`: tags matching one of these regexes are excluded from the change log.. The issues that are part of the excluded tags are 
also excluded from the change log. They are not reported under the next tag.

- `releaseUrlTemplate`: custom template for the URL of releases to use in the hyperlink on the title. If present, a `%s` 
placeholder will be replaced by the tag of the release. By default, it points to the source code of the git repository 
at the given tag.

- `diffUrlTemplate`: custom template for the URL to the full diff of the release. If present, 2 `%s` placeholders 
are replaced by the tag of the previous release and the current release, respectively. If you need to reverse the 
 order, you may use `%1$s` for the "from" (previous) tag, and `%2$s` for the "to" (current) tag.
 
- `releaseUrlTagTransform`: a function to transform the tag string before injection in the `releaseUrlTemplate`. By 
default, this is the identity function and doesn't change the tag. It may be handy to remove or add a "v" prefix for 
instance.
 
- `diffUrlTagTransform`: a function to transform the tag strings before injection in the `diffUrlTemplate`. By 
default, this is the identity function and doesn't change the tags. It may be handy to remove or add a "v" prefix for 
instance.
 
- `customTagByIssueNumber`: a mapping from issue numbers to tags. An issue may be incorrectly classified due to late 
closing date or other timing problems. If this is the case, use this map to override the tag to use for a particular 
issue.

- `useMilestoneAsTag`: if true, issues associated to a milestone with a title that matches a tag will be associated to 
that tag, regardless of their close date.

- `timezone`: the timezone used to convert the tags timestamps to local dates for releases (defaults to GMT).

- `outputFile`: the file to write the change log to.
