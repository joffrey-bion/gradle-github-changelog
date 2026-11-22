package org.hildan.github.changelog.plugin

import org.gradle.testfixtures.*
import org.gradle.testkit.runner.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.*
import java.io.*
import kotlin.test.*

class GitHubChangelogPluginTest {

    @Test
    fun `creates generateChangelog task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GitHubChangelogPlugin::class.java)
        val changelogTask = project.tasks.getByName("generateChangelog")
        assertNotNull(changelogTask)
    }

    @Test
    fun `creates changelog extension`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GitHubChangelogPlugin::class.java)
        val changelogExt = project.extensions.getByName("changelog")
        assertNotNull(changelogExt)
    }

    @Test
    fun `supports configuration cache`(@TempDir testProjectDir: File) {
        testProjectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("org.hildan.github.changelog")
            }

            changelog {
                githubUser.set("some-user")
            }
        """.trimIndent())

        fun run(): BuildResult = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("--configuration-cache", "generateChangelog", "--dry-run")
            .withPluginClasspath()
            .build()

        val firstRun = run()
        assertTrue(firstRun.output.contains("Configuration cache entry stored"), firstRun.output)

        val secondRun = run()
        assertTrue(secondRun.output.contains("Reusing configuration cache"), secondRun.output)
    }
}
