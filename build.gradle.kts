plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.0.0-rc-1"
    id("org.hildan.github.changelog") version "1.12.1"
    id("ru.vyarus.github-info") version "1.4.0"
}

group = "org.hildan.gradle"

github {
    user = "joffrey-bion"
    license = "MIT"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.kohsuke:github-api:1.313")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("io.mockk:mockk:1.13.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks.test {
    useJUnitPlatform()
}

pluginBundle {
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
    githubUser = github.user
    futureVersionTag = project.version.toString()
}
