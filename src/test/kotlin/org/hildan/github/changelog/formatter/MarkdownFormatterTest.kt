package org.hildan.github.changelog.formatter

import org.hildan.github.changelog.builder.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals

class MarkdownFormatterTest {

    @Test
    fun `format empty change log`() {
        val changelog = Changelog(
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
                number = 5,
                title = "Latest PR",
                body = "Latest PR description",
                closedAt = Instant.now(),
                labels = emptyList(),
                url = "http://github.com/issues/5",
                author = bob,
                isPullRequest = true,
            )
        )
        val sectionsNext = listOf(
            Section("Pull requests", DEFAULT_PR_SECTION_ORDER, prs3)
        )

        val prs2 = listOf(
            Issue(
                number = 4,
                title = "Some PR with <Things> *to* [escape]",
                body = "Some PR description",
                closedAt = Instant.now(),
                labels = emptyList(),
                url = "http://github.com/issues/4",
                author = bob,
                isPullRequest = true,
            )
        )
        val bugs2 = listOf(
            Issue(
                number = 3,
                title = "Some bug",
                body = "Some bug description",
                closedAt = Instant.now(),
                labels = emptyList(),
                url = "http://github.com/issues/3",
                author = alex,
                isPullRequest = false,
            )
        )
        val sections2 = listOf(
            Section("Pull requests", DEFAULT_PR_SECTION_ORDER, prs2),
            Section("Bug fixes", DEFAULT_BUGS_SECTION_ORDER, bugs2)
        )

        val prs1 = listOf(
            Issue(
                number = 2,
                title = "Some PR",
                body = "Some PR description",
                closedAt = Instant.now(),
                labels = emptyList(),
                url = "http://github.com/issues/2",
                author = lee,
                isPullRequest = true,
            )
        )
        val enhancements1 = listOf(
            Issue(
                number = 1,
                title = "Some feature with `markdown`",
                body = "Some feature description",
                closedAt = Instant.now(),
                labels = emptyList(),
                url = "http://github.com/issues/1",
                author = bob,
                isPullRequest = false,
            )
        )
        val sections1 = listOf(
            Section("Pull requests", DEFAULT_PR_SECTION_ORDER, prs1),
            Section("Enhancements", DEFAULT_ENHANCEMENTS_SECTION_ORDER, enhancements1)
        )

        val unreleased = Release(null, "Unreleased", null, now, sectionsNext, null, null)
        val release2 =
            Release(
                tag = tag2,
                title = tag2,
                summary = """
                    |This is a summary with *markdown*:
                    |
                    | - bullet point 1
                    | - bullet point 2 [with link](https://google.com)
                """.trimMargin(),
                date = date2,
                sections = sections2,
                releaseUrl = "http://github.com/tree/$tag2",
                diffUrl = "http://github.com/compare/$tag1...$tag2",
            )
        val release1 = Release(
            tag1,
            tag1,
            null,
            date1,
            sections1,
            "http://github.com/tree/$tag1",
            null
        )

        val releases = listOf(unreleased, release2, release1)
        val changelog = Changelog("My Title", releases)

        val formatter = MarkdownFormatter()

        val expected = """
            # My Title

            ## Unreleased (2019-01-03)

            **Pull requests**

            - Latest PR [\#5](http://github.com/issues/5) ([@bob](http://github.com/bob))

            ## [2.0.0](http://github.com/tree/2.0.0) (2019-01-02)
            [View commits](http://github.com/compare/1.0.0...2.0.0)

            This is a summary with *markdown*:
            
             - bullet point 1
             - bullet point 2 [with link](https://google.com)
            
            **Pull requests**

            - Some PR with \<Things\> \*to\* \[escape\] [\#4](http://github.com/issues/4) ([@bob](http://github.com/bob))

            **Bug fixes**

            - Some bug [\#3](http://github.com/issues/3)

            ## [1.0.0](http://github.com/tree/1.0.0) (2019-01-01)

            **Pull requests**

            - Some PR [\#2](http://github.com/issues/2) ([@lee](http://github.com/lee))

            **Enhancements**

            - Some feature with `markdown` [\#1](http://github.com/issues/1)

        """.trimIndent()

        assertEquals(expected, formatter.formatChangeLog(changelog))
    }
}
