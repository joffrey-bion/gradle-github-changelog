package org.hildan.github.changelog.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GitHubChangelogPluginTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

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
    fun `generateChangelog task creates changelog file`() {
        val buildFile = testProjectDir.newFile("build.gradle")
        buildFile.writeText("""
            plugins {
                id 'org.hildan.github.changelog'
            }

            changelog {
                githubUser = "joffrey-bion"
                githubRepository = "fx-gson"
            }
        """.trimIndent())

        GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("generateChangelog")
            .withPluginClasspath()
            .build()

        val files = testProjectDir.root.listFiles()
        val changelogFile = files.firstOrNull { it.name == "CHANGELOG.md" }
        assertNotNull(changelogFile)
        assertTrue(changelogFile!!.readText().startsWith("# Changelog"))
    }

    @Test
    fun `generateChangelog task creates custom changelog file`() {
        val buildFile = testProjectDir.newFile("build.gradle")
        buildFile.writeText("""
            plugins {
                id 'org.hildan.github.changelog'
            }

            changelog {
                title = "My Change Log"
                githubUser = "joffrey-bion"
                githubRepository = "fx-gson"
                releaseUrlTemplate = "https://my.server/releases/%s"
            }
        """.trimIndent())

        GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("generateChangelog")
            .withPluginClasspath()
            .build()

        val files = testProjectDir.root.listFiles()
        val changelogFile = files.firstOrNull { it.name == "CHANGELOG.md" }
        assertNotNull(changelogFile)
        val changelogText = changelogFile!!.readText()
        assertTrue(changelogText.startsWith("# My Change Log"))
        assertTrue(changelogText.contains("https://my.server/releases/v1.2"))
    }
}
