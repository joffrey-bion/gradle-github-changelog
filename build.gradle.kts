import com.expediagroup.graphql.plugin.gradle.config.*
import com.expediagroup.graphql.plugin.gradle.tasks.*

plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.1.0"
    id("org.hildan.github.changelog") version "1.12.1"
    id("ru.vyarus.github-info") version "1.5.0"
    id("com.expediagroup.graphql") version "7.0.0-alpha.3"
    kotlin("plugin.serialization") version "1.8.10"
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
    implementation("org.kohsuke:github-api:1.314")

    implementation("com.expediagroup", "graphql-kotlin-ktor-client", "7.0.0-alpha.3")
    implementation("io.ktor:ktor-client-auth:2.2.4")

    val ktorVersion = "2.2.4"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("io.mockk:mockk:1.13.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
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

// This allows to keep the GitHub schema up-to-date by dynamically fetching it
tasks.withType<GraphQLIntrospectSchemaTask> {
    headers.set(mapOf("Authorization" to "Bearer ${System.getenv("GITHUB_TOKEN")}"))
}

tasks.withType<GraphQLGenerateClientTask> {
    serializer.set(GraphQLSerializer.KOTLINX)
}

graphql {
    client {
        endpoint = "https://api.github.com/graphql"
        packageName = "org.hildan.github.graphql"
        customScalars = listOf(
            // A (potentially binary) string encoded using base64.
            GraphQLScalar("Base64String", "kotlin.String", "org.hildan.github.graphql.scalars.Base64Converter"),
            // An ISO-8601 encoded date string.
            GraphQLScalar("Date", "java.time.LocalDate", "org.hildan.github.graphql.scalars.LocalDateConverter"),
            // An ISO-8601 encoded UTC date string.
            GraphQLScalar("DateTime", "java.time.Instant", "org.hildan.github.graphql.scalars.InstantConverter"),
            // A Git object ID.
            GraphQLScalar("GitObjectID", "org.hildan.github.graphql.scalars.GitObjectID", "org.hildan.github.graphql.scalars.GitObjectIDConverter"),
            // A fully qualified reference name (e.g. `refs/heads/master`).
            GraphQLScalar("GitRefname", "org.hildan.github.graphql.scalars.GitRefname", "org.hildan.github.graphql.scalars.GitRefnameConverter"),
            // Git SSH string
            GraphQLScalar("GitSSHRemote", "org.hildan.github.graphql.scalars.GitSSHRemote", "org.hildan.github.graphql.scalars.GitSSHRemoteConverter"),
            // An ISO-8601 encoded date string. Unlike the DateTime type, GitTimestamp is not converted in UTC.
            GraphQLScalar("GitTimestamp", "java.time.LocalDateTime", "org.hildan.github.graphql.scalars.LocalDateTimeConverter"),
            // A string containing HTML code.
            GraphQLScalar("HTML", "org.hildan.github.graphql.scalars.HtmlContent", "org.hildan.github.graphql.scalars.HtmlContentConverter"),
            // An ISO-8601 encoded UTC date string with millisecond precision.
            GraphQLScalar("PreciseDateTime", "java.time.Instant", "org.hildan.github.graphql.scalars.InstantConverter"),
            // An RFC 3986, RFC 3987, and RFC 6570 (level 4) compliant URI string.
            //GraphQLScalar("URI", "kotlin.String", ""), // TODO typeOf("io.ktor.http", "Url") ?
            // A valid x509 certificate string
            //GraphQLScalar("X509Certificate", "kotlin.String", ""),
        )
        queryFileDirectory = "src/main/resources"
    }
}
