plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "0.12.0"
    id("org.hildan.github.changelog") version "1.10.0"
}

group = "org.hildan.gradle"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.kohsuke:github-api:1.301")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("io.mockk:mockk:1.12.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
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
