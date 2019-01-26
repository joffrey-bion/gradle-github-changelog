package org.hildan.github.changelog.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertNotNull
import org.junit.Test

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
}
