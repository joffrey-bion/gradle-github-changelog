package org.hildan.github.changelog.generator

import io.mockk.every
import io.mockk.mockkStatic
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

    @Test
    fun createChangeLog() {
        mockkStatic(LocalDateTime::class)
        val fakeNow = LocalDate.of(2019, 1, 20).atTime(10, 0)
        every { LocalDateTime.now() } returns fakeNow
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"))

        val clConfig = ChangelogConfig(GitHubConfig(user = "someuser", repo = "somerepo"))
        val builder = ChangeLogBuilder(clConfig)

        val issues = listOf(issue42, pr45)
        val tag182 = Tag("1.8.2", Instant.parse("2018-07-07T11:00:00.00Z"))
        val tag200 = Tag("2.0.0", Instant.parse("2018-08-10T10:00:00.00Z"))
        val tags = listOf(tag182, tag200)
        val actualChangeLog = builder.createChangeLog(issues, tags)


        val expectedChangeLog = ChangeLog(
            title = "Changelog",
            releases = listOf(
                Release(
                    title = "Unreleased",
                    date = fakeNow,
                    diffUrl = null,
                    releaseUrl = null,
                    sections = listOf(Section("Merged pull requests:", listOf(pr45)))
                ),
                Release(
                    title = "2.0.0",
                    date = LocalDate.of(2018, 8, 10).atTime(10, 0),
                    diffUrl = "https://github.com/someuser/somerepo/compare/1.8.2...2.0.0",
                    releaseUrl = "https://github.com/someuser/somerepo/tree/2.0.0",
                    sections = listOf(Section("Fixed bugs:", listOf(issue42)))
                ),
                Release(
                    title = "1.8.2",
                    date = LocalDate.of(2018, 7, 7).atTime(11, 0),
                    diffUrl = null,
                    releaseUrl = "https://github.com/someuser/somerepo/tree/1.8.2",
                    sections = emptyList()
                )
            )
        )

        assertEquals(expectedChangeLog, actualChangeLog)
    }
}
