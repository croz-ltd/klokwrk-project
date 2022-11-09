# Using unreleased libraries with jitpack.io
- **Author:** Damir Murat
- **Created:** 16.06.2021.
- **Updated:** 16.06.2021.

Sometimes it is not practical to wait for the officially published release of a specific library. There are situations when we want or need to use features or fixes recently committed but not yet
released.

In such a scenario, if the library is developed on GitHub, we can use jitpack.io for creating a pre-release version.

The described scenario is common in klokwrk, especially when we want to employ recent PRs submitted by our team. For us, this commonly happens with the CodeNarc library because of its relatively
long release cycles. For this reason, we will use CodeNarc as an example, but everything explained can also be applied to other libraries.

## Create the pre-release library version with jitpack.io
Before introducing the pre-release library version, we have to create it somehow. For this purpose, jitpack.io is an excellent choice.

### With jitpack.io user interface
- browse to https://jitpack.io/
- provide the repo URL of a library in the input box. For example `https://github.com/CodeNarc/CodeNarc`

  The user interface will change your input into `[repo owner]/[repo name]` syntax. For our example, the repo owner is the `CodeNarc` organization, while the repo name is also `CodeNarc`.

  If you are building a library from the personal repository instead of an organization, supply a corresponding GitHub username. This can be handy if you want to build a library from a fork of the
  official repository.

- click on "Commits" tab
- click on the "Get it" button next to the shortened SHA of the last commit you want to include in the build and wait until the build finish

  If you need to build from an unlisted commit, use jitpack API instead of the user interface as described below.

### With jitpack.io web API
- list all existing builds for a library (CodeNarc example)

      curl https://jitpack.io/api/builds/com.github.codenarc/codenarc

  In the above URL, note the prepended `com.github.` (dot included) prefix on repo owner. For example, in the case of my fork of CodeNarc repository URL will be
  `https://jitpack.io/api/builds/com.github.dmurat/codenarc`.

- create a build for a particular commit (CodeNarc example for `0d01347e22` shortened commit SHA)

      curl https://jitpack.io/api/builds/com.github.codenarc/codenarc/0d01347e22

  Note that any commit SHA of 8 or more characters will work, but for consistency with jitpack, it is recommended to use commit SHA of 10 characters.

## Include the pre-release library in the build
Instructions here are specific to the `klokwrk-project` but they should be simple enough for straightforward adoption for other projects. Instructions are also specific for our CodeNarc library
example. For other libraries, one needs to adapt them as appropriate.

- add jitpack variant for the version of a library in the root `gradle.properties`. For example:

      ...
      codeNarcVersion = 2.1.0              # <--- released library version
      codeNarcJitpackVersion = 0d01347e22  # <--- pre-released jitpack version of the library
      ...

- add jitpack maven repository in the root `settings.gradle`:

      ...
      dependencyResolutionManagement {
        repositories {
          ...
          maven { url 'https://jitpack.io' }
        }
      }
      ...

- if appropriate, add new dependency coordinates in the relevant platform config. For our case, this will be `platform/klokwrk-platform-base/build.gradle`:

      ...
      dependencies {
        ...
        constraints {
          ...
          api "org.codenarc:CodeNarc:$codeNarcVersion" // <--- official library coordinates
          api "com.github.codenarc:codenarc:$codeNarcJitpackVersion" // <--- pre-released library coordinates
        }
      }
      ...

  Notice the change in the group part of GAV coordinates. The original library has a group as envisioned by its developers. However, the group of the pre-release library created with jitpack will
  always be in a form `com.github.[repo owner]`. In our case, as the repo owner is `codenarc` organization, the group is `com.github.codenarc`.

  There is no need to comment out the original library dependency as Gradle platforms only manage dependency versions. They are not included anywhere yet.

- replace original library dependency references with pre-release references. For our case, CodeNarc dependencies are declared only in
  `buildSrc/src/main/groovy/klokwrk-gradle-plugin-convention-groovy.gradle`:

      ...
      dependencies {
        ...
        // codenarc "org.codenarc:CodeNarc" // <--- original library commented out
        codenarc "com.github.codenarc:codenarc" // <-- pre-released library included as dependency
        ...
      }

  At this point, we have to comment out the original library to avoid duplicates in the classpath.
