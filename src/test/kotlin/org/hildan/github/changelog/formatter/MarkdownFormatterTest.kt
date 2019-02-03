package org.hildan.github.changelog.formatter

import org.hildan.github.changelog.builder.ChangeLog
import org.hildan.github.changelog.builder.DEFAULT_CHANGELOG_TITLE
import org.hildan.github.changelog.builder.Issue
import org.hildan.github.changelog.builder.Release
import org.hildan.github.changelog.builder.Section
import org.hildan.github.changelog.builder.User
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals

class MarkdownFormatterTest {

    @Test
    fun `format empty change log`() {
        val changelog = ChangeLog(
            title = DEFAULT_CHANGELOG_TITLE,
            releases = emptyList()
        )

        val formatter = MarkdownFormatter()

        val expected = """
            # $DEFAULT_CHANGELOG_TITLE

            *Nothing much happened so far, actually...*
        """.trimIndent()

        assertEquals(expected, formatter.formatChangeLog(changelog))
    }

    @Test
    fun `format standard case`() {
        val now = LocalDate.of(2019, 1, 3).atTime(10, 0)
        val date2 = now.minusDays(1)
        val date1 = now.minusDays(2)

        val tag2 = "2.0.0"
        val tag1 = "1.0.0"

        val bob = User("bob", "http://github.com/bob")
        val alex = User("alex", "http://github.com/alex")
        val lee = User("lee", "http://github.com/lee")

        val prs3 = listOf(
            Issue(
                5,
                "Latest PR",
                Instant.now(),
                emptyList(),
                "http://github.com/issues/5",
                bob,
                true
            )
        )
        val sectionsNext = listOf(
            Section("Pull requests", prs3)
        )

        val prs2 = listOf(
            Issue(
                4,
                "Some PR with <Things> *to* [escape]",
                Instant.now(),
                emptyList(),
                "http://github.com/issues/4",
                bob,
                true
            )
        )
        val bugs2 = listOf(
            Issue(
                3,
                "Some bug",
                Instant.now(),
                emptyList(),
                "http://github.com/issues/3",
                alex,
                false
            )
        )
        val sections2 = listOf(
            Section("Pull requests", prs2),
            Section("Bug fixes", bugs2)
        )

        val prs1 = listOf(
            Issue(
                2,
                "Some PR",
                Instant.now(),
                emptyList(),
                "http://github.com/issues/2",
                lee,
                true
            )
        )
        val enhancements1 = listOf(
            Issue(
                1,
                "Some feature",
                Instant.now(),
                emptyList(),
                "http://github.com/issues/1",
                bob,
                false
            )
        )
        val sections1 = listOf(
            Section("Pull requests", prs1),
            Section("Enhancements", enhancements1)
        )

        val unreleased = Release(null, "Unreleased", now, sectionsNext, null, null)
        val release2 = Release(
            tag2, tag2, date2, sections2, "http://github.com/tree/$tag2", "http://github.com/compare/$tag1...$tag2"
        )
        val release1 = Release(
            tag1,
            tag1,
            date1,
            sections1,
            "http://github.com/tree/$tag1",
            null
        )

        val releases = listOf(unreleased, release2, release1)
        val changelog = ChangeLog("My Title", releases)

        val formatter = MarkdownFormatter()

        val expected = """
            # My Title

            ## Unreleased (2019-01-03)

            **Pull requests**

            - Latest PR [\#5](http://github.com/issues/5) ([@bob](http://github.com/bob))

            ## [2.0.0](http://github.com/tree/2.0.0) (2019-01-02)
            [Full Changelog](http://github.com/compare/1.0.0...2.0.0)

            **Pull requests**

            - Some PR with \<Things\> \*to\* \[escape\] [\#4](http://github.com/issues/4) ([@bob](http://github.com/bob))

            **Bug fixes**

            - Some bug [\#3](http://github.com/issues/3)

            ## [1.0.0](http://github.com/tree/1.0.0) (2019-01-01)

            **Pull requests**

            - Some PR [\#2](http://github.com/issues/2) ([@lee](http://github.com/lee))

            **Enhancements**

            - Some feature [\#1](http://github.com/issues/1)

        """.trimIndent()

        assertEquals(expected, formatter.formatChangeLog(changelog))
    }
}
