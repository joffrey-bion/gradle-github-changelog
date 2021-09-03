plugins {
    kotlin("jvm") version "1.4.20" // aligned with Gradle 6.8.1
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.12.0"
    id("org.hildan.github.changelog") version "1.3.0"
}

group = "org.hildan.gradle"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.kohsuke:github-api:1.122")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("io.mockk:mockk:1.10.6")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.test {
    useJUnitPlatform()
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

changelog {
    futureVersionTag = project.version.toString()
}
