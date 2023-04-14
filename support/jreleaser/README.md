# support/jreleaser readme

- `CHANGELOG-RELEASE.md`

  File `CHANGELOG-RELEASE.md` is a changelog placeholder. Before each release, it should be updated with the latest generated and manually checked/updated changelog.

  Changelog can be generated with jreleaser (via jbang) from the project root:

      env JRELEASER_PROJECT_VERSION=1.5.1 JRELEASER_GITHUB_TOKEN=1 \
      jbang --verbose jreleaser@jreleaser changelog --basedir=. --config-file=./support/jreleaser/jreleaser-draft.yml --debug

  or for jreleaser snapshot version (mind clearing maven cache at `.m2/repository/com/github/jreleaser`)

      env JRELEASER_PROJECT_VERSION=1.5.2-SNAPSHOT JRELEASER_GITHUB_TOKEN=1 \
      jbang --verbose jreleaser-snapshot@jreleaser changelog --basedir=. --config-file=./support/jreleaser/jreleaser-draft.yml --debug

  Changelog will be created in `out/jreleaser/release/CHANGELOG.md` file.
