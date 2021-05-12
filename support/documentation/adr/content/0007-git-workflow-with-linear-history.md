# ADR-0007 - Git Workflow with Linear History
* **Status: accepted**
* Dates:
  - proposed - 2020-11-03
  - updated - 2021-05-10
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
The value of tidy and [semi-linear commit history](https://fangpenlin.com/images/2013-09-30-keep-a-readable-git-history/source_tree_new_branch_rebase_merge.png) is often overlooked in many Git-based
projects. This is unfortunate since non-linear git commit history might be a [horrible mess](https://tugberkugurlu.blob.core.windows.net/bloggyimages/d773c1fe-4db8-4d2f-a994-c60f3f8cb6f0.png) that
does not provide any useful information. We want to use as simple as possible git workflow that promotes and ensures a semi-linear history.

> * **Semi-linear** commit history usually refers to a history that uses merge commits (git "no-fast-forward" merge option) to clearly denote which commits are meant to be together and represent a
>   coherent whole.
> * **Linear** commit history usually refers to completely flat history (git default "fast-forward") where it is impossible to tell at first glance which commits belong together.

When working on individual features, related git commits can be organized either as "work log" or as a "recipe". When working in a team, it is crucial that team members and/or reviewers can easily
comprehend what is going on in a particular feature. For this reason, we prefer features to be organized as "recipes".

> * **Work log** style of organizing feature commits refers to the style without any organization. Commits are added solely as they are developed through time.
> * **Recipe** style of organizing feature commits refers to the style where commits have a sensible organization where peer developers can clearly see and learn how the feature is created. This
>   style requires some additional work as its primary goal is communication, instead of just implementing a feature.

Very often, in bigger teams, common git workflows have a problem of broken continuous integration builds. We want to embrace and use as simple as possible workflow that resolves that problem.

Chosen git workflow should seamlessly support release versioning and, if needed, related work on release branches.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**We will use a [stable mainline branching model for Git](https://www.bitsnbites.eu/a-stable-mainline-branching-model-for-git/).** It
[supports semi-linear Git history](https://www.bitsnbites.eu/a-tidy-linear-git-history/) and helps to resolve the problem of broken continuous integration builds.

**We will, however, introduce several tweaks to the "stable mainline branching model":**
* We will use the following naming pattern for feature branches: **`feature_<name>`** instead of `feature/name`.
* We will not use new branches when remote feature branches need rebasing. Instead, we will just inform all collaborators that rebasing is pending for a feature branch. It is important
  to communicate with collaborators **before** rebasing and force-pushing.
* We will not create a release branch for each release. In general, we will just tag a release. However, we will create a release branch when a particular release needs fixing.

We will organize our [feature commits as recipes](https://www.bitsnbites.eu/git-history-work-log-vs-recipe/) because we want to promote the team's learning and communication.

We will also use specific commit message format as described in [ADR-0014 - Commit Message Format](./0014-commit-message-format.md)

## Consequences
### Positive
* Semi-linear Git history.
* Feature commits organized as recipes.
* Releasing can be simplified just to tagging.

### Negative
* Some tools (e.g., GitHub) does not provide out-of-the-box support for semi-linear history.

## Considered Options
* Merge based workflows.

## References
* [A stable mainline branching model for Git](https://www.bitsnbites.eu/a-stable-mainline-branching-model-for-git/)
* [A tidy, linear Git history](https://www.bitsnbites.eu/a-tidy-linear-git-history/)
* [Git history: work log vs recipe](https://www.bitsnbites.eu/git-history-work-log-vs-recipe/)
* [ADR-0014 - Commit Message Format](./0014-commit-message-format.md)
