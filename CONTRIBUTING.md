# Contributing to klokwrk-project
Thank you for your interest in contributing to Project Klokwrk. Anyone is welcome to open [issues](https://github.com/croz-ltd/klokwrk-project/issues) or
[pull requests](https://github.com/croz-ltd/klokwrk-project/pulls) for bug fixes, feature requests, or ideas. If unsure where to start, you can open a
[discussion](https://github.com/croz-ltd/klokwrk-project/discussions) topic first.

As Project Klokwrk evolves, the policies described here might change.

## Code of conduct
Our [Code of conduct](./CODE_OF_CONDUCT.md) governs this project and everyone participating in it. By participating, you are expected to uphold this code.

## Code Contributions
If you're contributing code, please read through the following documents:
- [Starting up and trying the whole thing](support/documentation/article/starting-up/startingUp.md)
- [ADR-0007 - Git Workflow with Linear History](support/documentation/adr/content/0007-git-workflow-with-linear-history.md)
- [ADR-0014 - Commit Message Format](support/documentation/adr/content/0014-commit-message-format.md)

To make sure your commits and their messages adhere to the project's policies, before start working on PRs, please install the project's Git hooks:

      git config core.hooksPath support/git/hooks

### Code style
We're trying very hard to maintain a consistent style of coding throughout the codebase. We try to codify that style as much as possible with the IDE we use (IDEA), but there might be cases not
codified yet or not supported by IDE.

We define our style with the `.editorconfig` file at the root of the project. Besides a small amount of standard EditorConfig configurations (http://editorconfig.org), the vast majority of entries
are IDEA editor config extensions. That configuration is enabled in IDEA by default. To make sure it is applied, check if IDEA setting
"Preferences -> Editor -> Code Style -> Enable EditorConfig support" is enabled.

With EditorConfig support enabled, simple code reformatting from IDE should apply desired code style. If the reformatted code is not in the desired style, please submit an issue, and we will see if
we can fix it.

In PRs, please, always separate code reformatting in individual commits. Otherwise, it is tough to distinguish style changes from actual "useful" changes.

Codenarc is another tool that we use for static code analysis. Besides checking some style issues, it also checks for other Groovy code idioms. Before submitting PRs, please check your changes with

      gw aggregateCodenarc

After all that work, it might happen that maintainers still are not satisfied with the non-functional shape of the code. Please, be patient with us as we go through the issues.
