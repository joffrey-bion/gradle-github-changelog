package org.hildan.github.changelog.generator

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GithubChangelogPluginTest {

    lateinit var project: Project

    @Before
    fun setUp() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GithubChangelogPlugin::class.java)
    }

    @Test
    fun `creates generateChangelog task`() {
        val changelogTask = project.tasks.getByName("generateChangelog")
        assertNotNull(changelogTask)
    }

    @Test
    fun `creates changelog extension`() {
        val changelogExt = project.extensions.getByName("changelog")
        assertNotNull(changelogExt)
    }
}
