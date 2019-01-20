plugins {
    kotlin("jvm") version "1.3.10"
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.10.0"
}

group = "org.hildan.gradle"
version = "0.1.1"

repositories {
    jcenter()
}

dependencies {
    compileOnly(gradleApi())
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.kohsuke:github-api:1.93")

    testCompileOnly(gradleTestKit())
    testImplementation("junit:junit:4.12")
    testImplementation("org.assertj:assertj-core:3.6.2")
}

pluginBundle {
    website = "https://github.com/joffrey-bion/gradle-github-changelog"
    vcsUrl = "https://github.com/joffrey-bion/gradle-github-changelog"
    tags = listOf("github", "changelog", "generator")
}

gradlePlugin {
    plugins {
        create("githubChangelogPlugin") {
            id = "org.hildan.github.changelog"
            displayName = "GitHub Changelog Plugin"
            description = "Generates a changelog for your project based on GitHub issues"
            implementationClass = "org.hildan.github.changelog.plugin.GitHubChangelogPlugin"
        }
    }
}
