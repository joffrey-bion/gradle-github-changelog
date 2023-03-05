package org.hildan.github.changelog.plugin

import org.gradle.kotlin.dsl.*
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import ru.vyarus.gradle.plugin.github.GithubInfoExtension
import ru.vyarus.gradle.plugin.github.GithubInfoPlugin
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
    fun `reads github user from Github Info plugin when applied before`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GithubInfoPlugin::class.java)
        project.pluginManager.apply(GitHubChangelogPlugin::class.java)

        val githubInfoExtension = project.extensions.getByType<GithubInfoExtension>()
        githubInfoExtension.user = "test-user"

        val changelogExt = project.extensions.getByType<GitHubChangelogExtension>()
        assertEquals(githubInfoExtension.user, changelogExt.githubUser.get())
    }
}
