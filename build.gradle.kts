plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.3.0"
    id("org.hildan.github.changelog") version "2.2.0"
    id("ru.vyarus.github-info") version "2.0.0"
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
    implementation("org.kohsuke:github-api:1.326")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("io.mockk:mockk:1.13.12")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("githubChangelogPlugin") {
            id = "org.hildan.github.changelog"
            displayName = "GitHub Changelog Plugin"
            description = "Generates a changelog for your project based on GitHub issues"
            implementationClass = "org.hildan.github.changelog.plugin.GitHubChangelogPlugin"
            tags.set(listOf("github", "changelog", "generator"))
        }
    }
}

changelog {
    githubUser = github.user
    futureVersionTag = project.version.toString()
}
