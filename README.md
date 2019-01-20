[![Build Status](https://travis-ci.org/joffrey-bion/gradle-github-changelog.svg?branch=master)](https://travis-ci.org/joffrey-bion/gradle-github-changelog)

# GitHub Changelog Generator (Gradle plugin)

Generates a changelog from GitHub issues and pull-requests.

This project mimics the functionality of the great 
[GitHub Changelog Generator](https://github.com/github-changelog-generator/github-changelog-generator)
as a Gradle plugin.

### Applying the plugin

**NOTE: the plugin is currently pending approval, it is not yet published to the Gradle plugin portal.**

Simply add the following line to your `plugins` block:

```groovy
plugins {
    id 'org.hildan.github.changelog' version '0.1.0'
}
```

Or if you're using the Gradle Kotlin DSL:

```kotlin
plugins {
    id("org.hildan.github.changelog") version "0.1.0"
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
