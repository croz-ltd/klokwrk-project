# test-retry-disabler Gradle plugin

The `test-retry-disabler` is a simple Gradle plugin for disabling `org.gradle.test-retry` plugin based on Gradle property, Java system property, the environment variable, or when run from IDEA.

One can configure the `test-retry` plugin through its DLS by specifying several properties. For disabling it, one should set `maxRetries` to `0`. However, it would be nice to have it enabled by
default (say with `maxRetries = 2`), but only disable it occasionally and when appropriate. For example, when developing tests.

The `test-retry-disabler` plugin can set `maxRetries` to `0` when tests are run from IDEA, when Gradle or Java system property `disableTestRetry` is specified in the CLI, or when the environment
variable `DISABLE_TEST_RETRY` is set.

## Usage
At the minimum, it is enough just to apply `test-retry-disabler` plugin:

```
plugins {
  id "org.gradle.test-retry" version "1.2.1"
  id "org.klokwrk.gradle.test-retry-disabler"
}
```

The configuration above will disable `test-retry` plugin when:
- Gradle build is run from IDEA
- Gradle build is run from CLI with specified `disableTestRetry` Gradle property (`-PdisableTestRetry`)

      ./gradlew check -PdisableTestRetry

- Gradle build is run from CLI with specified `disableTestRetry` Java system property (`-DdisableTestRetry`)

      ./gradlew check -DdisableTestRetry

- Gradle build is run from CLI with specified `DISABLE_TEST_RETRY` environment variable set

## Configuration
If needed, some aspects of `test-retry-dsiabler` can be configured:

```
plugins {
  id "java-library"
  id "org.gradle.test-retry" version "1.2.1"
  id "org.klokwrk.gradle.test-retry-disabler"
}

test {
  retry {
    maxRetries = 2
  }
}

testRetryDisabler {
  enabled = true
  enableIdeaCheck = true
  enableGradlePropertyCheck = true
  enableJavaSystemPropertyCheck = true
  enableEnvironmentVariableCheck = true
  enableWarningsLogging = true
}
```

In the example above, `test-retry-disabler` is configured with all default values. Although the result is the same as nothing was configured, the example demonstrates what can be configured and with
which values:
- `enabled` - when `false`, plugin is disabled as it was never applied
- `enableIdeaCheck` - when `false`, plugin does not check if it is run from IDEA
- `enableGradlePropertyCheck` - when `false`, plugin does not check if `disableTestRetry` Gradle property is specified
- `enableJavaSystemPropertyCheck` - when `false`, plugin does not check if `disableTestRetry` Java system property is specified
- `enableEnvironmentVariableCheck` - when `false`, plugin does not check if `DISABLE_TEST_RETRY` environment variable is set
- `enableWarningsLogging` - when `false`, plugin does not output any warning
