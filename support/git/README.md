# support/git readme

## hooks
Directory `hooks` contains git hooks that should be installed locally.

The most elegant way for enabling hooks is to configure git configuration of the project with our custom path to the hooks:

    git config core.hooksPath support/git/hooks
