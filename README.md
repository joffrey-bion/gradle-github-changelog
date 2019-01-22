[![Build Status](https://travis-ci.org/joffrey-bion/gradle-github-changelog.svg?branch=master)](https://travis-ci.org/joffrey-bion/gradle-github-changelog)
[![image](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/org/hildan/github/changelog/org.hildan.github.changelog.gradle.plugin/maven-metadata.xml.svg?label=gradle)](https://plugins.gradle.org/plugin/org.hildan.github.changelog)

# GitHub Changelog Generator (Gradle plugin)

Generates a changelog from GitHub issues and pull-requests.

This project mimics the functionality of the great 
[GitHub Changelog Generator](https://github.com/github-changelog-generator/github-changelog-generator)
as a Gradle plugin.

### Applying the plugin

Simply add the following line to your `plugins` block:

```groovy
plugins {
    id 'org.hildan.github.changelog' version '0.2.0'
}
```

Or if you're using the Gradle Kotlin DSL:

```kotlin
plugins {
    id("org.hildan.github.changelog") version "0.2.0"
}
```

### Configuration

The minimal required configuration is your **GitHub username**.

It can be provided in 3 ways:
- the `githubUser` project property
- the `GITHUB_USER` environment variable
- the `githubUser` property of the `changelog` DSL (see below)

Here is the extension DSL with all options and their default values:

```groovy
changelog {
    githubUser = // [mandatory] project property "githubUser" or env variable "GITHUB_USER"
    githubToken = null // [optional] project property "githubToken" or env variable "GITHUB_TOKEN"
    githubRepository = rootProject.name

    title = "Changelog"
    releaseUrlTemplate = null // defaults to "https://github.com/$user/$repo/tree/%s"
    unreleasedVersionTitle = "Unreleased"
    showUnreleased = true
    futureVersion = "$version"
    defaultIssueSectionTitle = "Closed issue:"
    defaultPrSectionTitle = "Merged pull requests:"
    includeLabels = []
    excludeLabels = ["duplicate", "invalid", "question", "wontfix"]

    outputFile = file("${projectDir}/CHANGELOG.md")
}
```

The GitHub API token is not necessary, but removes the limit of API calls this plugin can make to your project's GitHub.

The `releaseUrlTemplate` option allows to customize the release URL by providing a `%s` placeholder for the tag. 
By default, it points to the source code of the git repository at the given tag.
