package org.hildan.github.changelog.generator

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MarkdownFormatterTest {

    @Test
    fun format() {

        val now = LocalDate.now()
        val date2 = now.minusDays(1)
        val date1 = now.minusDays(2)

        val tag2 = "2.0.0"
        val tag1 = "1.0.0"

        val prs3 = listOf(
            Issue(5, "Latest PR", "http://github.com/issues/5", "bob", true)
        )
        val sectionsNext = listOf(
            Section("Pull requests", prs3)
        )

        val prs2 = listOf(
            Issue(4, "Some other PR", "http://github.com/issues/4", "bob", true)
        )
        val bugs2 = listOf(
            Issue(3, "Some bug", "http://github.com/issues/3", "alex", false)
        )
        val sections2 = listOf(
            Section("Pull requests", prs2),
            Section("Bug fixes", bugs2)
        )

        val prs1 = listOf(
                Issue(2, "Some PR", "http://github.com/issues/2", "lee", true)
        )
        val enhancements1 = listOf(
                Issue(1, "Some feature", "http://github.com/issues/1", "bob", false)
        )
        val sections1 = listOf(
            Section("Pull requests", prs1),
            Section("Enhancements", enhancements1)
        )

        val unreleased = Release(null, tag2, now, sectionsNext, null, null)
        val release2 = Release(tag2, tag1, date2, sections2, "http://github.com/tree/$tag2",
            "http://github.com/compare/$tag1...$tag2")
        val release1 = Release(tag1, null, date1, sections1, "http://github.com/tree/$tag1", null)

        val releases = listOf(unreleased, release2, release1)
        val changelog = ChangeLog("My Title", releases)

        val formatter = MarkdownFormatter("Unreleased")

        val expected = """
            # My Title

            ## Unreleased (2019-01-20)

            **Pull requests**

             - Latest PR [#5](http://github.com/issues/5) (@bob)

            ## [2.0.0](http://github.com/tree/2.0.0) (2019-01-19)
            [Full Changelog](http://github.com/compare/1.0.0...2.0.0)

            **Pull requests**

             - Some other PR [#4](http://github.com/issues/4) (@bob)

            **Bug fixes**

             - Some bug [#3](http://github.com/issues/3)

            ## [1.0.0](http://github.com/tree/1.0.0) (2019-01-18)

            **Pull requests**

             - Some PR [#2](http://github.com/issues/2) (@lee)

            **Enhancements**

             - Some feature [#1](http://github.com/issues/1)

        """.trimIndent()

        assertEquals(expected, formatter.format(changelog))
    }
}
