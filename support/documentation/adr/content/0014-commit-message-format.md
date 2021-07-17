# ADR-0014 - Commit Message Format
* **Status: accepted**
* Dates: proposed - 2021-05-10
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
Using commit messages without any structure looks convenient for developers as they do not have to think about messages too much. Unfortunately, that freedom and lack of thinking can impose some
additional burden on long-term project maintenance.

Quite often, we can find incomprehensible commit messages that do not communicate anything useful. Hopefully, imposing some lightweight rules and guidance will help developers create commit messages
that are helpful for their colleagues.

In addition, with unstructured commit messages, there is much less opportunity to introduce any tools on top of commit history. For example, we would like to employ an automated changelog generator
based on extracting some semantical meaning from commits, but this will not work if commit messages lack any structure. Without the commit message structure, we can just dump the commit log in the
changelog, which does not make the changelog more helpful than looking at the history of commits in the first place.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**We will use a customized [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/) format for writing commit messages.**

Conventional commits format is nice and short and defines the simple structure that is easy to learn and follow. Here is basic structure of our customized conventional commits format:

      <type>(optional <scope>): <description> {optional <metadata>}

Our customization:
- defines additional message types as an extension of [types defined by the Angular team](https://github.com/angular/angular/blob/22b96b9/CONTRIBUTING.md#-commit-message-guidelines)
- allows adding additional metadata in the message title if useful and appropriate (see details section for more info)
- requires format compliance only for messages of "significant" commits (see details section for more info)

### Decision details
Details about the decision are given mainly as a set of strong recommendations (strongly recommended) and rules enforced by tooling (rule). In our case, the tooling is implemented as git commit hooks.
Every contributor should install git hooks provided in this repository. That can be done with following command (executed from the project root):

    git config core.hooksPath support/git/hooks

There might be cases when implemented rules are not appropriate and should be updated or removed or just temporarily ignored. In such scenarios, hooks can be skipped with git's `--no-verify` option.

While describing details, following terms are used as described:
- *commit message title*: refers to the first line of a commit message
- *commit message description*: refers to the part of the title describing a commit with human-readable message. In conventional commits specification that part is called `description`.

#### General guidance and rules for all commit messages
- (strongly recommended) - avoid trivial commit messages titles or descriptions
- (strongly recommended) - use imperative mood in title or description (add instead of adding or added, update instead of updating or updated etc.) as you are spelling out a command
- (rule) - message title or description must start with the uppercase letter <br/>
  <br/>
  The main reason is a desire for better readability as we want easily spot the beginning message description or title. There are some arguments for using the lowercase like "message titles are not
  sentences". While this is true, we prefer to have better readability than comply with some vague constraints.<br/>
  <br/>
- (rule) - message title or description should not end with common punctuation characters: `.!?`
- (strongly recommended) - message title or description should not be comprised of multiple sentences
- (rule) - message title should not be longer than 120 characters. Use the message body if more space for description is needed<br/>
  <br/>
  Actually, there is a common convention that we should not use more than 69 characters in the message title. It looks like the main reason for it is that GitHub truncates anything above 69 chars
  from message titles. Having such a tight constraint seems unreasonable today, and the apparent shortcomings of any tool shouldn't restrict us, even if the tool is GitHub.<br/>
  <br/>
- (strongly recommended) - commit message title or description should describe "what" (and sometimes "why"), instead of "how"<br/>
  <br/>
  For describing "why", the message body is more appropriate as we have more space there. If needed, the message body may contain "how" too, but it should be clearly separated (at least with a blank
  line) from "what" and "why".<br/>
  <br/>
- (recommended) - commit message title should provide optional scope (from conventional commit specification) if applicable
  - if commit refers to multiple scopes, scopes should be separated with `/` character
  - if commit refers to the work which influences the whole project, the scope should be `project` or it can be left out
  - the scope should be a single word in lowercase<br/>
  <br/>
- (strongly recommended) - message body must be separated from message title with a single blank line
- (option) - message body can contain additional blank lines
- (recommended) - message body should not use lines longer than 150 characters
- (strongly recommended) - include relevant references to issues or pull request to the metadata section of message title<br/>
  <br/>
  Example: `feat(some-module): Adding a new feature {m, fixes i#123, pr#13, related to i#333, resolves i#444}`<br/>
  <br/>
- (option) - include relevant feature/bug ticket links in message footer according to conventional commits guidelines<br/>
  <br/>
  Footer is separated from body with a single blank line.

#### Guidance and rules for "normal" commits to the main development branch
- (rule) - all commits to the main development branch must have a message title in customized conventional commit format

#### Guidance and rules for merge commits to the main development branch
When used with [semi-linear commit history](./0007-git-workflow-with-linear-history.md), merge commits are the primary carriers of completed work units. As such, they are the most interesting for
creating a changelog.

Before merging, merge commits must be rebased against main development branch, and merging must be executed with no-fast-forward option (`--no-ff`).

- (rule) - merge commits must have 'merge' metadata (`{m}`) present at the end of the title <br/>
  <br/>
  That way, merge commits can be easily distinguished on GitHub and in the changelog.
- (option) - merge commit metadata can carry additional information related to the issues and PRs references like in the following example

      feat(klokwrk-tool-gradle-source-repack): Adding a new feature {m, fixes i#123, pr#13, related to i#333, resolves i#444}

  Here, `i#123` is a reference to the issue, while `pr#12` is a reference to the pull request. Additional metadata are not controlled or enforced by git hooks.

#### Guidance and rules for normal commits to the feature branches
- (option) - normal commits don't have to follow custom conventional commits format for message title
- (strongly recommended) - normal commits should use conventional commits format when contained change is significant enough on its own to be placed in the changelog

When all useful changelog entries are contained in normal commits of a feature branch, we can do two different things depending on the situation:
  - use merge commit with type of `notype`. Such merge commit will be ignored when creating a changelog.
  - merge a branch with fast-forward option (no merge commit will be present)

Preferably, use `notype` merge commits, as they are still useful for clear separation of related work.

#### Types for conventional commits format
- common (angular)
    - `feat` or `feature` - a new feature
    - `fix` - a bug fix
    - `docs` - documentation only changes
    - `style` - changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
    - `test` - adding missing tests or correcting existing tests
    - `build` - changes that affect the build system or external dependencies
    - `ci` - changes to our CI configuration files and scripts
    - `refactor` - a code change that neither fixes a bug nor adds a feature
    - `perf` - a code change that improves performance
    - `chore` - routine task


- custom
    - `enhance` or `enhancement` - improvements to the existing features
    - `deps` - dependencies updates (use instead of `build` when commit only updates dependencies)<br/>
      <br/>
      There are two main scenarios when upgrading dependencies, a simple version bump and the more involved upgrade requiring resolving various issues like compilation errors, API upgrades, etc.<br/>
      <br/>
      Simple version bumps should be contained in a single individual commit with a description message starting with the word "Bump". For example: `deps: Bump Micronaut to 2.5.2 version`.<br/>
      <br/>
      More complicated upgrades should be organized as feature branches where each non-conventional commit resolves a single step in the process. When finished, the feature branch should be merged
      into the main development branch with a description starting with the word "Upgrade". For example: `deps: Upgrade Spring Boot to 2.5.0 version {m}`.<br/>
      <br/>
    - `task` - same meaning as `chore`. Prefer using `task`.
    - `article` - use instead of `docs` when changes are related only to articles
    - `misc` - anything that does not fit into previous categories
    - `notype` - only for merge commits in situations where contained plain commits carries all relevant types and merge commit doesn't add anything useful. It is ignored in changelog.<br/>
      <br/>
      Typical example is when we are updating number of dependencies through commits in a feature branch. In this situation each feature branch commit should contain updates to a single dependency
      and communicate the update through its own commit message in conventional commit format. When all updates in the branch are tested, we can merge them in the main branch. However, that merge
      should be ignored by changelog as it does not communicate anything new.

## Consequences
### Positive
- facilities thinking about commit messages at they should provide useful changelog information
- facilities thinking about organizing feature branches. If we have too many conventional commits in a single branch, maybe we should separate it into two or more.
- when applied consistently, provides basis for automating changelog creation

### Neutral
- while adopting, it may be hard to come up with an appropriate message title or description

### Negative
- additional expectation and responsibility for developers

## Considered Options
- not prescribing the commit message format

## References
- Conventional Commits Specification v1.0.0 (https://www.conventionalcommits.org/en/v1.0.0/)
- Commit Message Guidelines for Angular (https://github.com/angular/angular/blob/22b96b9/CONTRIBUTING.md#-commit-message-guidelines)
- ADR-0007 - Git Workflow with Linear History
