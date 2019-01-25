package org.hildan.github.changelog.generator

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import java.time.Instant

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.TimeZone
import kotlin.test.assertEquals

class ChangeLogBuilderTest {

    private val pr45 = Issue(
        number = 45,
        title = "Fixed problem 45",
        authorLogin = "mike",
        closedAt = Instant.parse("2018-08-10T10:15:30.00Z"),
        isPullRequest = true,
        labels = emptyList(),
        url = "https://some.host/issue/45"
    )

    private val issue42 = Issue(
        number = 42,
        title = "Fixed problem 42",
        authorLogin = "bob",
        closedAt = Instant.parse("2018-08-05T10:15:30.00Z"),
        isPullRequest = false,
        labels = listOf("bug"),
        url = "https://some.host/issue/42"
    )

    private val issue43 = Issue(
        number = 43,
        title = "Fixed problem 43",
        authorLogin = "bob",
        closedAt = Instant.parse("2018-08-06T10:15:30.00Z"),
        isPullRequest = false,
        labels = listOf("bug"),
        url = "https://some.host/issue/43"
    )

    private val nonLabeledIssue = Issue(
        number = 99,
        title = "No label for me",
        authorLogin = "hipster",
        closedAt = Instant.parse("2018-05-01T08:00:00.00Z"),
        isPullRequest = false,
        labels = emptyList(),
        url = "https://some.host/issue/99"
    )

    private val fakeNow = LocalDate.of(2019, 1, 20).atTime(10, 0)

    @Before
    fun setUpFakeNow() {
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fakeNow
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"))
    }

    @Test
    fun `no tags and no issues yield empty changelog`() {
        val clConfig = ChangelogConfig(GitHubConfig(user = "someuser", repo = "somerepo"))
        val builder = ChangeLogBuilder(clConfig)

        val actualChangeLog = builder.createChangeLog(emptyList(), emptyList())
        val expectedChangeLog = ChangeLog(title = "Changelog", releases = emptyList())

        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `custom changelog title`() {
        val clConfig = ChangelogConfig(GitHubConfig(user = "someuser", repo = "somerepo"), globalHeader = "Custom")
        val builder = ChangeLogBuilder(clConfig)

        val actualChangeLog = builder.createChangeLog(emptyList(), emptyList())
        val expectedChangeLog = ChangeLog(title = "Custom", releases = emptyList())

        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `unreleased issues only`() {
        val clConfig = ChangelogConfig(GitHubConfig(user = "someuser", repo = "somerepo"))
        val builder = ChangeLogBuilder(clConfig)

        val issues = listOf(issue42, issue43, pr45)
        val actualChangeLog = builder.createChangeLog(issues, emptyList())
        val expectedChangeLog = ChangeLog(
            title = "Changelog",
            releases = listOf(
                Release(
                    tag = null,
                    title = "Unreleased",
                    date = fakeNow,
                    diffUrl = null,
                    releaseUrl = null,
                    sections = listOf(
                        Section("Fixed bugs:", listOf(issue42, issue43)),
                        Section("Merged pull requests:", listOf(pr45))
                    )
                )
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `default issue section`() {
        val clConfig = ChangelogConfig(GitHubConfig(user = "someuser", repo = "somerepo"))
        val builder = ChangeLogBuilder(clConfig)

        val issues = listOf(nonLabeledIssue, pr45)
        val actualChangeLog = builder.createChangeLog(issues, emptyList())
        val expectedChangeLog = ChangeLog(
            title = "Changelog",
            releases = listOf(
                Release(
                    tag = null,
                    title = "Unreleased",
                    date = fakeNow,
                    diffUrl = null,
                    releaseUrl = null,
                    sections = listOf(
                        Section("Closed issues:", listOf(nonLabeledIssue)),
                        Section("Merged pull requests:", listOf(pr45))
                    )
                )
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `custom default issues and PR section titles`() {
        val clConfig = ChangelogConfig(
            GitHubConfig(user = "someuser", repo = "somerepo"),
            defaultIssueSectionTitle = "Issues:",
            defaultPrSectionTitle = "PRs:"
        )
        val builder = ChangeLogBuilder(clConfig)

        val issues = listOf(nonLabeledIssue, pr45)
        val actualChangeLog = builder.createChangeLog(issues, emptyList())
        val expectedChangeLog = ChangeLog(
            title = "Changelog",
            releases = listOf(
                Release(
                    tag = null,
                    title = "Unreleased",
                    date = fakeNow,
                    diffUrl = null,
                    releaseUrl = null,
                    sections = listOf(
                        Section("Issues:", listOf(nonLabeledIssue)),
                        Section("PRs:", listOf(pr45))
                    )
                )
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `custom future version title`() {
        val clConfig = ChangelogConfig(GitHubConfig(user = "someuser", repo = "somerepo"), futureVersion = "Coming up")
        val builder = ChangeLogBuilder(clConfig)

        val issues = listOf(issue42)
        val actualChangeLog = builder.createChangeLog(issues, emptyList())
        val expectedChangeLog = ChangeLog(
            title = "Changelog",
            releases = listOf(
                Release(
                    tag = null,
                    title = "Coming up",
                    date = fakeNow,
                    diffUrl = null,
                    releaseUrl = null,
                    sections = listOf(Section("Fixed bugs:", listOf(issue42)))
                )
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `standard case`() {
        val clConfig = ChangelogConfig(GitHubConfig(user = "someuser", repo = "somerepo"))
        val builder = ChangeLogBuilder(clConfig)

        val issues = listOf(pr45, issue42, issue43, nonLabeledIssue) // unordered on purpose
        val tag180 = Tag("1.8.0", Instant.parse("2018-05-06T11:00:00.00Z"))
        val tag182 = Tag("1.8.2", Instant.parse("2018-07-07T11:00:00.00Z"))
        val tag200 = Tag("2.0.0", Instant.parse("2018-08-10T10:00:00.00Z"))
        val tags = listOf(tag180, tag200, tag182) // unordered on purpose
        val actualChangeLog = builder.createChangeLog(issues, tags)


        val expectedChangeLog = ChangeLog(
            title = "Changelog",
            releases = listOf(
                Release(
                    tag = null,
                    title = "Unreleased",
                    date = fakeNow,
                    diffUrl = null,
                    releaseUrl = null,
                    sections = listOf(Section("Merged pull requests:", listOf(pr45)))
                ),
                Release(
                    tag = "2.0.0",
                    title = "2.0.0",
                    date = LocalDate.of(2018, 8, 10).atTime(10, 0),
                    diffUrl = "https://github.com/someuser/somerepo/compare/1.8.2...2.0.0",
                    releaseUrl = "https://github.com/someuser/somerepo/tree/2.0.0",
                    sections = listOf(Section("Fixed bugs:", listOf(issue42, issue43)))
                ),
                Release(
                    tag = "1.8.2",
                    title = "1.8.2",
                    date = LocalDate.of(2018, 7, 7).atTime(11, 0),
                    diffUrl = "https://github.com/someuser/somerepo/compare/1.8.0...1.8.2",
                    releaseUrl = "https://github.com/someuser/somerepo/tree/1.8.2",
                    sections = emptyList()
                ),
                Release(
                    tag = "1.8.0",
                    title = "1.8.0",
                    date = LocalDate.of(2018, 5, 6).atTime(11, 0),
                    diffUrl = null,
                    releaseUrl = "https://github.com/someuser/somerepo/tree/1.8.0",
                    sections = listOf(Section("Closed issues:", listOf(nonLabeledIssue)))
                )
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `sinceTag option should limit the output releases`() {
        val clConfig = ChangelogConfig(GitHubConfig(user = "someuser", repo = "somerepo"), sinceTag = "1.8.2")
        val builder = ChangeLogBuilder(clConfig)

        val issues = listOf(pr45, issue42, issue43, nonLabeledIssue) // unordered on purpose
        val tag180 = Tag("1.8.0", Instant.parse("2018-05-06T11:00:00.00Z"))
        val tag182 = Tag("1.8.2", Instant.parse("2018-07-07T11:00:00.00Z"))
        val tag200 = Tag("2.0.0", Instant.parse("2018-08-10T10:00:00.00Z"))
        val tags = listOf(tag180, tag200, tag182) // unordered on purpose
        val actualChangeLog = builder.createChangeLog(issues, tags)


        val expectedChangeLog = ChangeLog(
            title = "Changelog",
            releases = listOf(
                Release(
                    tag = null,
                    title = "Unreleased",
                    date = fakeNow,
                    diffUrl = null,
                    releaseUrl = null,
                    sections = listOf(Section("Merged pull requests:", listOf(pr45)))
                ),
                Release(
                    tag = "2.0.0",
                    title = "2.0.0",
                    date = LocalDate.of(2018, 8, 10).atTime(10, 0),
                    diffUrl = "https://github.com/someuser/somerepo/compare/1.8.2...2.0.0",
                    releaseUrl = "https://github.com/someuser/somerepo/tree/2.0.0",
                    sections = listOf(Section("Fixed bugs:", listOf(issue42, issue43)))
                ),
                Release(
                    tag = "1.8.2",
                    title = "1.8.2",
                    date = LocalDate.of(2018, 7, 7).atTime(11, 0),
                    diffUrl = "https://github.com/someuser/somerepo/compare/1.8.0...1.8.2",
                    releaseUrl = "https://github.com/someuser/somerepo/tree/1.8.2",
                    sections = emptyList()
                )
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }
}
