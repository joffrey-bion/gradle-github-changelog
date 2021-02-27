package org.hildan.github.changelog.builder

import io.mockk.every
import io.mockk.mockkStatic
import org.hildan.github.changelog.github.Repository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class ChangeLogBuilderTest {

    private val defaultConfig = ChangelogConfig(
        releaseUrlTemplate = "https://github.com/someuser/somerepo/tree/%s",
        diffUrlTemplate = "https://github.com/someuser/somerepo/compare/%s...%s"
    )

    private val fakeNow = LocalDate.of(2019, 1, 20).atTime(10, 0)

    private val fakeInitSha = "fake_init_sha"

    private val issue1unlabeled = Issue(
        number = 1,
        title = "No label for me",
        author = User("hipster", "http://github.com/hipster"),
        closedAt = Instant.parse("2018-05-01T08:00:00.00Z"),
        isPullRequest = false,
        labels = emptyList(),
        url = "https://some.host/issue/1"
    )

    private val issue2bug = Issue(
        number = 2,
        title = "Issue 2",
        author = User("hipster", "http://github.com/hipster"),
        closedAt = Instant.parse("2018-05-02T15:20:00.00Z"),
        isPullRequest = false,
        labels = listOf("bug"),
        url = "https://some.host/issue/2"
    )

    private val issue42bug = Issue(
        number = 42,
        title = "Fixed problem 42",
        author = User("bob", "http://github.com/bob"),
        closedAt = Instant.parse("2018-08-05T10:15:30.00Z"),
        isPullRequest = false,
        labels = listOf("bug"),
        url = "https://some.host/issue/42"
    )

    private val issue43bug = Issue(
        number = 43,
        title = "Fixed problem 43",
        author = User("bob", "http://github.com/bob"),
        closedAt = Instant.parse("2018-08-06T10:15:30.00Z"),
        isPullRequest = false,
        labels = listOf("bug"),
        url = "https://some.host/issue/43"
    )

    private val issue44enhancement = Issue(
        number = 44,
        title = "Add thing 44",
        author = User("bob", "http://github.com/bob"),
        closedAt = Instant.parse("2018-08-06T10:30:00.00Z"),
        isPullRequest = false,
        labels = listOf("enhancement"),
        url = "https://some.host/issue/44"
    )

    private val pr45bugfix = Issue(
        number = 45,
        title = "Fixed problem 45",
        author = User("mike", "http://github.com/mike"),
        closedAt = Instant.parse("2018-08-09T10:15:30.00Z"),
        isPullRequest = true,
        labels = listOf("bug"),
        url = "https://some.host/issue/45"
    )

    private val pr46unlabeled = Issue(
        number = 46,
        title = "Fixed problem 46",
        author = User("mike", "http://github.com/mike"),
        closedAt = Instant.parse("2018-08-10T10:15:30.00Z"),
        isPullRequest = true,
        labels = emptyList(),
        url = "https://some.host/issue/46"
    )

    // unordered on purpose
    private val issues = listOf(
        pr46unlabeled, issue42bug, issue2bug, issue43bug, issue1unlabeled, pr45bugfix, issue44enhancement
    )

    private val oldBugsSection = Section("Fixed bugs:", listOf(issue2bug))
    private val bugsSection = Section("Fixed bugs:", listOf(pr45bugfix, issue43bug, issue42bug))
    private val enhancementsSection = Section("Implemented enhancements:", listOf(issue44enhancement))
    private val unlabeledIssuesSection = Section(DEFAULT_ISSUES_SECTION_TITLE, listOf(issue1unlabeled))
    private val unlabeledPrsSection = Section(DEFAULT_PR_SECTION_TITLE, listOf(pr46unlabeled))

    private val tag180 = Tag("1.8.0", Instant.parse("2018-05-06T11:00:00.00Z"))
    private val tag182 = Tag("1.8.2", Instant.parse("2018-07-07T11:00:00.00Z"))
    private val tag200 = Tag("2.0.0", Instant.parse("2018-08-10T10:00:00.00Z"))

    // unordered on purpose
    private val tags = listOf(tag180, tag200, tag182)

    private val release180 = Release(
        tag = "1.8.0",
        title = "1.8.0",
        date = LocalDate.of(2018, 5, 6).atTime(11, 0),
        diffUrl = "https://github.com/someuser/somerepo/compare/$fakeInitSha...1.8.0",
        releaseUrl = "https://github.com/someuser/somerepo/tree/1.8.0",
        sections = listOf(unlabeledIssuesSection, oldBugsSection)
    )

    private val release182 = Release(
        tag = "1.8.2",
        title = "1.8.2",
        date = LocalDate.of(2018, 7, 7).atTime(11, 0),
        diffUrl = "https://github.com/someuser/somerepo/compare/1.8.0...1.8.2",
        releaseUrl = "https://github.com/someuser/somerepo/tree/1.8.2",
        sections = emptyList()
    )

    private val release200 = Release(
        tag = "2.0.0",
        title = "2.0.0",
        date = LocalDate.of(2018, 8, 10).atTime(10, 0),
        diffUrl = "https://github.com/someuser/somerepo/compare/1.8.2...2.0.0",
        releaseUrl = "https://github.com/someuser/somerepo/tree/2.0.0",
        sections = listOf(bugsSection, enhancementsSection)
    )

    private val releaseUnreleased = Release(
        tag = null,
        title = DEFAULT_UNRELEASED_VERSION_TITLE,
        date = fakeNow,
        diffUrl = null,
        releaseUrl = null,
        sections = listOf(unlabeledPrsSection)
    )

    private val expectedChangeLog = Changelog(
        title = DEFAULT_CHANGELOG_TITLE,
        releases = listOf(releaseUnreleased, release200, release182, release180)
    )

    @BeforeEach
    fun setUpFakeNow() {
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fakeNow
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"))
    }

    @Test
    fun `no tags and no issues yield empty changelog`() {
        val builder = ChangelogBuilder(defaultConfig)

        val actualChangeLog = builder.createChangeLog(emptyList(), emptyList())
        val expectedChangeLog = Changelog(
            title = DEFAULT_CHANGELOG_TITLE,
            releases = emptyList()
        )

        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `unreleased issues only - no tags`() {
        val builder = ChangelogBuilder(defaultConfig)

        val actualChangeLog = builder.createChangeLog(issues, emptyList())
        val expectedChangeLog = Changelog(
            title = DEFAULT_CHANGELOG_TITLE, releases = listOf(
                Release(
                    tag = null,
                    title = DEFAULT_UNRELEASED_VERSION_TITLE,
                    date = fakeNow,
                    diffUrl = null,
                    releaseUrl = null,
                    sections = listOf(
                        unlabeledIssuesSection,
                        bugsSection.copy(issues = bugsSection.issues + oldBugsSection.issues),
                        enhancementsSection,
                        unlabeledPrsSection
                    )
                )
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `standard case with default config`() {
        val builder = ChangelogBuilder(defaultConfig)

        val actualChangeLog = builder.createChangeLog(issues, tags)

        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `custom changelog title`() {
        val customGlobalTitle = "Custom"
        val clConfig = defaultConfig.copy(globalHeader = customGlobalTitle)
        val builder = ChangelogBuilder(clConfig)

        val actualChangeLog = builder.createChangeLog(issues, tags)

        val expectedChangeLog = expectedChangeLog.copy(title = customGlobalTitle)
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `custom default issues and PR section titles`() {
        val customIssuesSectionTitle = "Issues:"
        val customPrSectionTitle = "PRs:"
        val clConfig = defaultConfig.copy(
            defaultIssueSectionTitle = customIssuesSectionTitle,
            defaultPrSectionTitle = customPrSectionTitle
        )
        val builder = ChangelogBuilder(clConfig)

        val actualChangeLog = builder.createChangeLog(issues, tags)

        val expectedChangeLog = expectedChangeLog.copy(
            releases = listOf(
                releaseUnreleased.copy(
                    sections = listOf(
                        unlabeledPrsSection.copy(title = customPrSectionTitle)
                    )
                ),
                release200,
                release182,
                release180.copy(
                    sections = listOf(
                        oldBugsSection,
                        unlabeledIssuesSection.copy(title = customIssuesSectionTitle)
                    )
                )
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `custom unreleased version title`() {
        val customUnreleasedVersionTitle = "Coming up"
        val clConfig = defaultConfig.copy(unreleasedVersionTitle = customUnreleasedVersionTitle)
        val builder = ChangelogBuilder(clConfig)

        val actualChangeLog = builder.createChangeLog(issues, tags)

        val expectedChangeLog = expectedChangeLog.copy(
            releases = listOf(
                releaseUnreleased.copy(title = customUnreleasedVersionTitle),
                release200,
                release182,
                release180
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `futureVersionTag option should change the unreleased title and URLs`() {
        val customFutureVersionTag = "3.0.0"
        val clConfig = defaultConfig.copy(futureVersionTag = customFutureVersionTag)
        val builder = ChangelogBuilder(clConfig)

        val actualChangeLog = builder.createChangeLog(issues, tags)

        val expectedChangeLog = expectedChangeLog.copy(
            releases = listOf(
                releaseUnreleased.copy(
                    tag = customFutureVersionTag,
                    title = customFutureVersionTag,
                    releaseUrl = "https://github.com/someuser/somerepo/tree/$customFutureVersionTag",
                    diffUrl = "https://github.com/someuser/somerepo/compare/2.0.0...$customFutureVersionTag"
                ),
                release200,
                release182,
                release180
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `sinceTag option should limit the output releases`() {
        val clConfig = defaultConfig.copy(sinceTag = "1.8.2")
        val builder = ChangelogBuilder(clConfig)

        val actualChangeLog = builder.createChangeLog(issues, tags)

        val expectedChangeLog = expectedChangeLog.copy(
            releases = listOf(releaseUnreleased, release200, release182)
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `skipTags option should limit the output releases`() {
        val clConfig = defaultConfig.copy(skipTags = listOf("2.0.0"))
        val builder = ChangelogBuilder(clConfig)

        val actualChangeLog = builder.createChangeLog(issues, tags)

        val expectedChangeLog = expectedChangeLog.copy(
            releases = listOf(releaseUnreleased, release182, release180)
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `tag transforms options should change the release and diff URLs`() {
        val clConfig = defaultConfig.copy(
            releaseUrlTagTransform = { "tag-prefix-$it" },
            diffUrlTagTransform = { "$it-suffix" }
        )
        val builder = ChangelogBuilder(clConfig)

        val actualChangeLog = builder.createChangeLog(issues, tags)

        val expectedChangeLog = expectedChangeLog.copy(
            releases = listOf(
                releaseUnreleased,
                release200.copy(
                    diffUrl = "https://github.com/someuser/somerepo/compare/1.8.2-suffix...2.0.0-suffix",
                    releaseUrl = "https://github.com/someuser/somerepo/tree/tag-prefix-2.0.0",
                ),
                release182.copy(
                    diffUrl = "https://github.com/someuser/somerepo/compare/1.8.0-suffix...1.8.2-suffix",
                    releaseUrl = "https://github.com/someuser/somerepo/tree/tag-prefix-1.8.2",
                ),
                release180.copy(
                    diffUrl = "https://github.com/someuser/somerepo/compare/$fakeInitSha...1.8.0-suffix",
                    releaseUrl = "https://github.com/someuser/somerepo/tree/tag-prefix-1.8.0",
                )
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `customTagByIssueNumber option should move arbitrary issues in specified tag`() {
        val clConfig = defaultConfig.copy(customTagByIssueNumber = mapOf(44 to "1.8.2", 46 to "1.8.2"))
        val builder = ChangelogBuilder(clConfig)

        val actualChangeLog = builder.createChangeLog(issues, tags)

        val expectedChangeLog = expectedChangeLog.copy(
            releases = listOf(
                release200.copy(sections = listOf(bugsSection)),
                release182.copy(sections = listOf(enhancementsSection, unlabeledPrsSection)),
                release180
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `issues with milestone matching tag should be moved to that tag`() {
        val modifiedIssues = issues.forceMilestone("1.8.2", 44, 46)
        val builder = ChangelogBuilder(defaultConfig)

        val actualChangeLog = builder.createChangeLog(modifiedIssues, tags)

        val expectedChangeLog = expectedChangeLog.copy(
            releases = listOf(
                release200.copy(sections = listOf(bugsSection)),
                release182.copy(
                    sections = listOf(
                        enhancementsSection.forceMilestone("1.8.2", 44),
                        unlabeledPrsSection.forceMilestone("1.8.2", 46),
                    ),
                ),
                release180,
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    @Test
    fun `milestone override should be disabled if useMilestonesAsTags is false`() {
        val modifiedIssues = issues.forceMilestone("1.8.2", 44, 46)
        val builder = ChangelogBuilder(defaultConfig.copy(useMilestoneAsTag = false))

        val actualChangeLog = builder.createChangeLog(modifiedIssues, tags)

        val expectedChangeLog = expectedChangeLog.copy(
            releases = listOf(
                releaseUnreleased.copy(
                    sections = listOf(
                        unlabeledPrsSection.forceMilestone("1.8.2", 46),
                    ),
                ),
                release200.copy(
                    sections = listOf(
                        bugsSection,
                        enhancementsSection.forceMilestone("1.8.2", 44),
                    ),
                ),
                release182,
                release180,
            )
        )
        assertEquals(expectedChangeLog, actualChangeLog)
    }

    private fun Section.forceMilestone(milestone: String, vararg issueNumbers: Int) = copy(
        issues = issues.forceMilestone(milestone, *issueNumbers)
    )

    private fun List<Issue>.forceMilestone(milestone: String, vararg issueNumbers: Int) = map {
        if (it.number in issueNumbers) {
            it.copy(milestone = Milestone(milestone, ""))
        } else {
            it
        }
    }

    private fun ChangelogBuilder.createChangeLog(issues: List<Issue>, tags: List<Tag>): Changelog {
        return createChangeLog(Repository(tags, issues, fakeInitSha))
    }
}
