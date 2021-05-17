package org.klokwrk.gradle.testretrydisabler

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.environment.RestoreSystemProperties

class TestRetryDisablerPluginSpecification extends Specification {
  static final String LINE_SEPARATOR = System.getProperty("line.separator")
  static final Boolean DEBUG_ENABLED = true
  static final String TEST_PROJECT_NAME = "testing-test-retry-disabler"
  static final String TEST_RETRY_PLUGIN_VERSION = "1.2.1"

  @TempDir
  File testProjectDir

  File settingsFile
  File buildFile

  void setup() {
    settingsFile = new File(testProjectDir, "settings.gradle")
    buildFile = new File(testProjectDir, "build.gradle")

    settingsFile << "rootProject.name = '$TEST_PROJECT_NAME'$LINE_SEPARATOR"
  }

  void "should not run when disabled"() {
    given:
    buildFile << """
      plugins {
        id "java-library"
        id "org.gradle.test-retry" version "$TEST_RETRY_PLUGIN_VERSION"
        id "org.klokwrk.gradle.test-retry-disabler"
      }

      test {
        retry {
          maxRetries = 2
        }
      }

      testRetryDisabler {
        enabled = false
      }
    """

    when:
    BuildResult result = GradleRunner
        .create()
        .withDebug(DEBUG_ENABLED)
        .withProjectDir(testProjectDir)
        .withArguments("--info", "help")
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("[$TestRetryDisablerPlugin.PLUGIN_ID_SHORT:$TEST_PROJECT_NAME] INFO - Plugin is disabled. Won't do anything.")
    result.task(":help").outcome == TaskOutcome.SUCCESS
  }

  void "should warn when applied on a project with child projects"() {
    given:
    File childProjectDir = new File(testProjectDir.absolutePath, "child")
    childProjectDir.mkdir()

    File childBuildFile = new File(childProjectDir, "build.gradle")
    childBuildFile << ""

    settingsFile << "include 'child'"
    buildFile << """
      plugins {
        id "org.klokwrk.gradle.test-retry-disabler"
      }
    """

    when:
    BuildResult result = GradleRunner
        .create()
        .withDebug(DEBUG_ENABLED)
        .withProjectDir(testProjectDir)
        .withArguments("help")
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("[$TestRetryDisablerPlugin.PLUGIN_ID_SHORT:$TEST_PROJECT_NAME] WARNING - Skipping '$TEST_PROJECT_NAME' project as it has child projects.")
    result.task(":help").outcome == TaskOutcome.SUCCESS
  }

  void "should warn when applied on a project without test-retry plugin"() {
    given:
    buildFile << """
      plugins {
        id "org.klokwrk.gradle.test-retry-disabler"
      }
    """

    when:
    BuildResult result = GradleRunner
        .create()
        .withDebug(DEBUG_ENABLED)
        .withProjectDir(testProjectDir)
        .withArguments("help")
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("[$TestRetryDisablerPlugin.PLUGIN_ID_SHORT:$TEST_PROJECT_NAME] WARNING - 'org.gradle.test-retry' plugin is not found in project '$TEST_PROJECT_NAME'.")
    result.task(":help").outcome == TaskOutcome.SUCCESS
  }

  void "should inform when 'test-retry' plugin is already disabled (maxRetries == 0)"() {
    given:
    buildFile << """
      plugins {
        id "java-library"
        id "org.gradle.test-retry" version "$TEST_RETRY_PLUGIN_VERSION"
        id "org.klokwrk.gradle.test-retry-disabler"
      }
    """

    when:
    BuildResult result = GradleRunner
        .create()
        .withDebug(DEBUG_ENABLED)
        .withProjectDir(testProjectDir)
        .withArguments("--info", "help")
        .withPluginClasspath()
        .build()

    then:
    result.output.contains(
        "[$TestRetryDisablerPlugin.PLUGIN_ID_SHORT:$TEST_PROJECT_NAME] INFO - (task: 'test') Detected maxRetries is equal to 0, meaning 'org.gradle.test-retry' plugin is already disabled."
    )
    result.task(":help").outcome == TaskOutcome.SUCCESS
  }

  @RestoreSystemProperties
  void "should reconfigure maxRetries when run from IDEA"() {
    given:
    System.setProperty("idea.version", "something")

    buildFile << """
      plugins {
        id "java-library"
        id "org.gradle.test-retry" version "$TEST_RETRY_PLUGIN_VERSION"
        id "org.klokwrk.gradle.test-retry-disabler"
      }

      test {
        retry {
          maxRetries = 2
        }
      }
    """

    when:
    BuildResult result = GradleRunner
        .create()
        .withDebug(DEBUG_ENABLED)
        .withProjectDir(testProjectDir)
        .withArguments("--info", "help")
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("[$TestRetryDisablerPlugin.PLUGIN_ID_SHORT:$TEST_PROJECT_NAME] INFO - (task: 'test') Run from IDEA. Detected maxRetries of 2 is set to 0.")
    result.task(":help").outcome == TaskOutcome.SUCCESS
  }

  @RestoreSystemProperties
  void "should NOT reconfigure maxRetries when run from IDEA and enableIdeaCheck is disabled"() {
    given:
    System.setProperty("idea.version", "something")

    buildFile << """
      plugins {
        id "java-library"
        id "org.gradle.test-retry" version "$TEST_RETRY_PLUGIN_VERSION"
        id "org.klokwrk.gradle.test-retry-disabler"
      }

      test {
        retry {
          maxRetries = 2
        }
      }

      testRetryDisabler {
        enableIdeaCheck = false
      }
    """

    when:
    BuildResult result = GradleRunner
        .create()
        .withDebug(DEBUG_ENABLED)
        .withProjectDir(testProjectDir)
        .withArguments("--info", "help")
        .withPluginClasspath()
        .build()

    then:
    result.output.contains(
        "[$TestRetryDisablerPlugin.PLUGIN_ID_SHORT:$TEST_PROJECT_NAME] INFO - (task: 'test') Didn't reconfigure anything. Either parameters are not supplied or all checks are disabled."
    )
    result.task(":help").outcome == TaskOutcome.SUCCESS
  }

  void "should reconfigure maxRetries when run with Gradle property"() {
    given:
    buildFile << """
      plugins {
        id "java-library"
        id "org.gradle.test-retry" version "$TEST_RETRY_PLUGIN_VERSION"
        id "org.klokwrk.gradle.test-retry-disabler"
      }

      test {
        retry {
          maxRetries = 2
        }
      }
    """

    when:
    BuildResult result = GradleRunner
        .create()
        .withDebug(DEBUG_ENABLED)
        .withProjectDir(testProjectDir)
        .withArguments("--info", "-PdisableTestRetry", "help")
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("[$TestRetryDisablerPlugin.PLUGIN_ID_SHORT:$TEST_PROJECT_NAME] INFO - (task: 'test') Gradle property 'disableTestRetry' is present. Detected maxRetries of 2 is set to 0.")
    result.task(":help").outcome == TaskOutcome.SUCCESS
  }

  void "should NOT reconfigure maxRetries when run with Gradle property and enableGradlePropertyCheck is disabled"() {
    given:
    buildFile << """
      plugins {
        id "java-library"
        id "org.gradle.test-retry" version "$TEST_RETRY_PLUGIN_VERSION"
        id "org.klokwrk.gradle.test-retry-disabler"
      }

      test {
        retry {
          maxRetries = 2
        }
      }

      testRetryDisabler {
        enableGradlePropertyCheck = false
      }
    """

    when:
    BuildResult result = GradleRunner
        .create()
        .withDebug(DEBUG_ENABLED)
        .withProjectDir(testProjectDir)
        .withArguments("--info", "-PdisableTestRetry", "help")
        .withPluginClasspath()
        .build()

    then:
    result.output.contains(
        "[$TestRetryDisablerPlugin.PLUGIN_ID_SHORT:$TEST_PROJECT_NAME] INFO - (task: 'test') Didn't reconfigure anything. Either parameters are not supplied or all checks are disabled."
    )
    result.task(":help").outcome == TaskOutcome.SUCCESS
  }

  @RestoreSystemProperties
  void "should reconfigure maxRetries when run with Java system property"() {
    given:
    buildFile << """
      plugins {
        id "java-library"
        id "org.gradle.test-retry" version "$TEST_RETRY_PLUGIN_VERSION"
        id "org.klokwrk.gradle.test-retry-disabler"
      }

      test {
        retry {
          maxRetries = 2
        }
      }
    """

    when:
    BuildResult result = GradleRunner
        .create()
        .withDebug(DEBUG_ENABLED)
        .withProjectDir(testProjectDir)
        .withArguments("--info", "-DdisableTestRetry", "help")
        .withPluginClasspath()
        .build()

    then:
    result.output.contains(
        "[$TestRetryDisablerPlugin.PLUGIN_ID_SHORT:$TEST_PROJECT_NAME] INFO - (task: 'test') Java system property 'disableTestRetry' is present. Detected maxRetries of 2 is set to 0."
    )
    result.task(":help").outcome == TaskOutcome.SUCCESS
  }

  @RestoreSystemProperties
  void "should NOT reconfigure maxRetries when run with Java system property and enableJavaSystemPropertyCheck is disabled"() {
    given:
    buildFile << """
      plugins {
        id "java-library"
        id "org.gradle.test-retry" version "$TEST_RETRY_PLUGIN_VERSION"
        id "org.klokwrk.gradle.test-retry-disabler"
      }

      test {
        retry {
          maxRetries = 2
        }
      }

      testRetryDisabler {
        enableJavaSystemPropertyCheck = false
      }
    """

    when:
    BuildResult result = GradleRunner
        .create()
        .withDebug(DEBUG_ENABLED)
        .withProjectDir(testProjectDir)
        .withArguments("--info", "-DdisableTestRetry", "help")
        .withPluginClasspath()
        .build()

    then:
    result.output.contains(
        "[$TestRetryDisablerPlugin.PLUGIN_ID_SHORT:$TEST_PROJECT_NAME] INFO - (task: 'test') Didn't reconfigure anything. Either parameters are not supplied or all checks are disabled."
    )
    result.task(":help").outcome == TaskOutcome.SUCCESS
  }

  void "should reconfigure maxRetries when run with environment variable"() {
    given:
    buildFile << """
      plugins {
        id "java-library"
        id "org.gradle.test-retry" version "$TEST_RETRY_PLUGIN_VERSION"
        id "org.klokwrk.gradle.test-retry-disabler"
      }

      test {
        retry {
          maxRetries = 2
        }
      }
    """

    when:
    BuildResult result = GradleRunner
        .create()
//        .withDebug(DEBUG_ENABLED)
        .withEnvironment([(TestRetryDisablerPlugin.ENVIRONMENT_VARIABLE_NAME): "something"])
        .withProjectDir(testProjectDir)
        .withArguments("--info", "help")
        .withPluginClasspath()
        .build()

    then:
    result.output.contains(
        "[$TestRetryDisablerPlugin.PLUGIN_ID_SHORT:$TEST_PROJECT_NAME] INFO - (task: 'test') Environment variable '$TestRetryDisablerPlugin.ENVIRONMENT_VARIABLE_NAME' is present. " +
        "Detected maxRetries of 2 is set to 0."
    )
    result.task(":help").outcome == TaskOutcome.SUCCESS
  }

  void "should NOT reconfigure maxRetries when run with environment variable and enableEnvironmentVariableCheck is disabled"() {
    given:
    buildFile << """
      plugins {
        id "java-library"
        id "org.gradle.test-retry" version "$TEST_RETRY_PLUGIN_VERSION"
        id "org.klokwrk.gradle.test-retry-disabler"
      }

      test {
        retry {
          maxRetries = 2
        }
      }

      testRetryDisabler {
        enableEnvironmentVariableCheck = false
      }
    """

    when:
    BuildResult result = GradleRunner
        .create()
//        .withDebug(DEBUG_ENABLED)
        .withEnvironment([(TestRetryDisablerPlugin.ENVIRONMENT_VARIABLE_NAME): "something"])
        .withProjectDir(testProjectDir)
        .withArguments("--info", "help")
        .withPluginClasspath()
        .build()

    then:
    result.output.contains(
        "[$TestRetryDisablerPlugin.PLUGIN_ID_SHORT:$TEST_PROJECT_NAME] INFO - (task: 'test') Didn't reconfigure anything. Either parameters are not supplied or all checks are disabled."
    )
    result.task(":help").outcome == TaskOutcome.SUCCESS
  }
}
