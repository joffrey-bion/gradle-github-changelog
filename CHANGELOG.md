# Change Log

## [1.12.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.12.0) (2022-10-31)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/1.11.1...1.12.0)

**Implemented enhancements:**

- Generate release notes for the latest version as a build report [\#46](https://github.com/joffrey-bion/gradle-github-changelog/issues/46)

## [1.11.1](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.11.1) (2022-01-10)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/1.11.0...1.11.1)

**Fixed bugs:**

- First commit in first release diff URL is actually the last [\#51](https://github.com/joffrey-bion/gradle-github-changelog/issues/51)
- "body must not be null" in 1.11.0 [\#49](https://github.com/joffrey-bion/gradle-github-changelog/issues/49)

**Upgraded dependencies:**

- Upgrade Github API to 1.301 [\#50](https://github.com/joffrey-bion/gradle-github-changelog/issues/50)

## [1.11.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.11.0) (2022-01-10)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/1.10.0...1.11.0)

This release brings a new feature called "release summary".

The text you're reading is actually from a release summary itself.
It has been written as the body of [a GitHub issue](https://github.com/joffrey-bion/gradle-github-changelog/issues/48) tagged with `release-summary` and associated to the milestone 1.11.0.

This is sufficient for it to be automatically picked up by the generator and placed here as description.

**Implemented enhancements:**

- Allow to provide a release summary [\#32](https://github.com/joffrey-bion/gradle-github-changelog/issues/32)
- Respect markdown backticks in issue titles [\#47](https://github.com/joffrey-bion/gradle-github-changelog/issues/47)

## [1.10.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.10.0) (2022-01-08)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/1.9.0...1.10.0)

**Implemented enhancements:**

- Rename default sections Deprecations/Removals [\#45](https://github.com/joffrey-bion/gradle-github-changelog/issues/45)

## [1.9.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.9.0) (2021-12-30)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/1.8.0...1.9.0)

**Implemented enhancements:**

- Add default sections for breaking changes, deprecations and removals [\#43](https://github.com/joffrey-bion/gradle-github-changelog/issues/43)
- Add more default excluded labels: `internal`, `no\-changelog` [\#42](https://github.com/joffrey-bion/gradle-github-changelog/issues/42)
- Add default section for dependency upgrades [\#41](https://github.com/joffrey-bion/gradle-github-changelog/issues/41)

## [1.8.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.8.0) (2021-09-03)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/1.7.0...1.8.0)

**Implemented enhancements:**

- Replace deprecated basic auth with token [\#39](https://github.com/joffrey-bion/gradle-github-changelog/issues/39)
- Add more logging to see more granular steps [\#38](https://github.com/joffrey-bion/gradle-github-changelog/issues/38)
- Upgrade Github API client to 1.132 [\#37](https://github.com/joffrey-bion/gradle-github-changelog/issues/37)

## [1.7.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.7.0) (2021-05-28)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/1.6.0...1.7.0)

**Implemented enhancements:**

- SkipTags support  regular expression [\#36](https://github.com/joffrey-bion/gradle-github-changelog/issues/36)

## [1.6.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.6.0) (2021-02-27)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/1.5.0...1.6.0)

**Fixed bugs:**

- Exception when milestone has no description [\#35](https://github.com/joffrey-bion/gradle-github-changelog/issues/35)

**Implemented enhancements:**

- Add config param to enable/disable milestone override [\#34](https://github.com/joffrey-bion/gradle-github-changelog/issues/34)

## [1.5.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.5.0) (2021-02-27)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/1.4.0...1.5.0)

**Implemented enhancements:**

- Allow using Milestones to determine association of issues with tags [\#33](https://github.com/joffrey-bion/gradle-github-changelog/issues/33)

## [1.4.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.4.0) (2021-02-06)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/1.3.0...1.4.0)

**Implemented enhancements:**

- Provide full changelog link for first tag [\#31](https://github.com/joffrey-bion/gradle-github-changelog/issues/31)

## [1.3.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.3.0) (2021-01-24)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/1.2.0...1.3.0)

**Implemented enhancements:**

- Add default section for new features \(linked to "feature" label\) [\#27](https://github.com/joffrey-bion/gradle-github-changelog/issues/27)

## [1.2.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.2.0) (2021-01-24)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/1.1.0...1.2.0)

**Implemented enhancements:**

- Exclude issues with doc or documentation label by default [\#26](https://github.com/joffrey-bion/gradle-github-changelog/issues/26)

## [1.1.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.1.0) (2021-01-24)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/1.0.0...1.1.0)

**Implemented enhancements:**

- Make timezone used for local dates more stable and/or configurable [\#25](https://github.com/joffrey-bion/gradle-github-changelog/issues/25)

## [1.0.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/1.0.0) (2021-01-24)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/0.8.0...1.0.0)

**Implemented enhancements:**

- Upgrade to Kotlin 1.4.20 [\#24](https://github.com/joffrey-bion/gradle-github-changelog/issues/24)
- Cleanup class names to align changeLog \-\> changelog [\#23](https://github.com/joffrey-bion/gradle-github-changelog/issues/23)

## [0.8.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/0.8.0) (2019-01-31)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/0.7.0...0.8.0)

**Implemented enhancements:**

- Turn PR authors' names into links to their profile [\#21](https://github.com/joffrey-bion/gradle-github-changelog/issues/21)

## [0.7.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/0.7.0) (2019-01-29)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/0.6.0...0.7.0)

**Implemented enhancements:**

- Allow manually specifying which release an issue belongs to [\#16](https://github.com/joffrey-bion/gradle-github-changelog/issues/16)
- Make issue order deterministic within a section [\#20](https://github.com/joffrey-bion/gradle-github-changelog/issues/20)

## [0.6.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/0.6.0) (2019-01-29)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/0.5.2...0.6.0)

**Fixed bugs:**

- Custom sections don't override default ones for "bug" and "enhancement" labels [\#19](https://github.com/joffrey-bion/gradle-github-changelog/issues/19)

**Implemented enhancements:**

- Make section order deterministic \(sorted alphabetically\) [\#18](https://github.com/joffrey-bion/gradle-github-changelog/issues/18)

## [0.5.2](https://github.com/joffrey-bion/gradle-github-changelog/tree/0.5.2) (2019-01-29)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/0.5.1...0.5.2)

**Fixed bugs:**

- Pull\-Requests seen as regular issues [\#17](https://github.com/joffrey-bion/gradle-github-changelog/issues/17)

## [0.5.1](https://github.com/joffrey-bion/gradle-github-changelog/tree/0.5.1) (2019-01-28)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/0.5.0...0.5.1)

**Fixed bugs:**

- releaseUrlTagTransform / diffUrlTagTransform are read only and cannot be configured [\#15](https://github.com/joffrey-bion/gradle-github-changelog/issues/15)

## [0.5.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/0.5.0) (2019-01-28)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/0.4.0...0.5.0)

**Implemented enhancements:**

- Remove default future version and distinguish from unreleased [\#14](https://github.com/joffrey-bion/gradle-github-changelog/issues/14)
- Allow customization of the sections for custom labels [\#13](https://github.com/joffrey-bion/gradle-github-changelog/issues/13)
- Add some text to fill empty changelog or releases [\#8](https://github.com/joffrey-bion/gradle-github-changelog/issues/8)
- Add possibility to transform tags for releaseUrl and diffUrl [\#12](https://github.com/joffrey-bion/gradle-github-changelog/issues/12)

## [0.4.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/0.4.0) (2019-01-28)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/0.3.0...0.4.0)

**Fixed bugs:**

- Escape markdown in issue titles and other user text [\#9](https://github.com/joffrey-bion/gradle-github-changelog/issues/9)
- Default label "Closed issue:" should be plural [\#11](https://github.com/joffrey-bion/gradle-github-changelog/issues/11)

## [0.3.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/0.3.0) (2019-01-26)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/0.2.1...0.3.0)

**Fixed bugs:**

- Fix default "futureVersion" [\#6](https://github.com/joffrey-bion/gradle-github-changelog/issues/6)

**Implemented enhancements:**

- Add "skip tags" option [\#7](https://github.com/joffrey-bion/gradle-github-changelog/issues/7)
- Add "since tag" option [\#5](https://github.com/joffrey-bion/gradle-github-changelog/issues/5)

## [0.2.1](https://github.com/joffrey-bion/gradle-github-changelog/tree/0.2.1) (2019-01-24)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/0.2.0...0.2.1)

**Fixed bugs:**

- Fix sorting for releases that were published on the same date [\#4](https://github.com/joffrey-bion/gradle-github-changelog/issues/4)

## [0.2.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/0.2.0) (2019-01-22)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/0.1.1...0.2.0)

**Closed issues:**

- Publish to Gradle portal [\#2](https://github.com/joffrey-bion/gradle-github-changelog/issues/2)

**Implemented enhancements:**

- Make releaseUrl customizable [\#3](https://github.com/joffrey-bion/gradle-github-changelog/issues/3)

## [0.1.1](https://github.com/joffrey-bion/gradle-github-changelog/tree/0.1.1) (2019-01-20)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/0.1.0...0.1.1)


## [0.1.0](https://github.com/joffrey-bion/gradle-github-changelog/tree/0.1.0) (2019-01-20)
[View commits](https://github.com/joffrey-bion/gradle-github-changelog/compare/62a26be507776326de2b975098f48af6acea57df...0.1.0)

**Implemented enhancements:**

- Initial generation feature [\#1](https://github.com/joffrey-bion/gradle-github-changelog/issues/1)
