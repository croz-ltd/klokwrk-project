package org.klokwrk.gradle.testretrydisabler

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.testing.Test

@SuppressWarnings('unused')
@CompileStatic
class TestRetryDisablerPlugin implements Plugin<Project> {
  static final String PLUGIN_ID = "org.klokwrk.gradle.test-retry-disabler"
  static final String PLUGIN_ID_SHORT = "test-retry-disabler"
  static final String EXTENSION_NAME = "testRetryDisabler"
  static final String PROPERTY_NAME = "disableTestRetry"
  static final String ENVIRONMENT_VARIABLE_NAME = "DISABLE_TEST_RETRY"

  @Override
  void apply(Project project) {
    project.getExtensions().create(EXTENSION_NAME, TestRetryDisablerPluginExtension)

    project.afterEvaluate { Project myProject ->
      TestRetryDisablerPluginExtension extension = project.getExtensions().getByType(TestRetryDisablerPluginExtension)

      if (!extension.enabled.get()) {
        Logger logger = project.getLogger()
        logger.info("[$PLUGIN_ID_SHORT:$project.name] INFO - Plugin is disabled. Won't do anything.")
        return
      }

      if (!project.childProjects.isEmpty()) {
        if (extension.enableWarningsLogging.get()) {
          Logger logger = project.getLogger()
          logger.warn(
              "[$PLUGIN_ID_SHORT:$project.name] WARNING - Skipping '$project.name' project as it has child projects. " +
              "Plugin '$PLUGIN_ID_SHORT' can only be applied on leaf projects and should be removed from '$project.name'."
          )
        }

        return
      }

      if (!checkIfTestRetryPluginIsPresent(myProject)) {
        return
      }

      TaskCollection<Test> testTaskWithRetryCollection = findTestRetryTasksWithMaxRetriesConfigured(project)
      reconfigureTestRetryTasks(project, testTaskWithRetryCollection)
    }
  }

  private Boolean checkIfTestRetryPluginIsPresent(Project project) {
    Plugin testRetryPlugin = project.plugins.findPlugin("org.gradle.test-retry")

    if (!testRetryPlugin) {
      TestRetryDisablerPluginExtension extension = project.getExtensions().getByType(TestRetryDisablerPluginExtension)
      if (extension.enableWarningsLogging.get()) {
        Logger logger = project.getLogger()
        logger.warn(
            "[$PLUGIN_ID_SHORT:$project.name] WARNING - 'org.gradle.test-retry' plugin is not found in project '$project.name'. " +
            "Therefore, '$PLUGIN_ID' can be removed from the project as it does not have any effect."
        )
      }

      return false
    }

    return true
  }

  private TaskCollection<Test> findTestRetryTasksWithMaxRetriesConfigured(Project project) {
    TaskCollection<Test> testTaskWithRetryCollection = project
        .getTasks().withType(Test)
        .matching { Test testTask ->
          def taskRetryExtension = testTask.extensions.findByName("retry")
          if (taskRetryExtension) {
            Integer maxRetries = (taskRetryExtension.getProperties()["maxRetries"] as Property).get() as Integer
            if (maxRetries > 0) {
              return true
            }
            else {
              Logger logger = project.getLogger()
              logger.info(
                  "[$PLUGIN_ID_SHORT:$project.name] INFO - (task: '$testTask.name') " +
                  "Detected maxRetries is equal to 0, meaning 'org.gradle.test-retry' plugin is already disabled. There is nothing more for me to do."
              )
            }
          }

          return false
        }

    return testTaskWithRetryCollection
  }

  @SuppressWarnings("DuplicatedCode")
  private void reconfigureTestRetryTasks(Project project, TaskCollection<Test> testTaskWithRetryCollection) {
    Logger logger = project.getLogger()

    testTaskWithRetryCollection.each { Test testTask ->
      TestRetryDisablerPluginExtension disablerExtension = project.getExtensions().findByType(TestRetryDisablerPluginExtension)

      Integer maxRetriesDetected = (testTask.extensions.getByName("retry").getProperties()["maxRetries"] as Property).get() as Integer
      Integer maxRetriesReconfigured = 0

      if (disablerExtension.enableIdeaCheck.get()) {
        if (System.getProperty('idea.version')) {
          (testTask.extensions.getByName("retry").getProperties()["maxRetries"] as Property).set(maxRetriesReconfigured)
          logger.info("[$PLUGIN_ID_SHORT:$project.name] INFO - (task: '$testTask.name') Run from IDEA. Detected maxRetries of $maxRetriesDetected is set to $maxRetriesReconfigured.")
          return
        }
      }

      if (disablerExtension.enableGradlePropertyCheck.get()) {
        if (project.hasProperty(PROPERTY_NAME)) {
          String disableTestRetryGradleProperty = project.getProperties()[PROPERTY_NAME]
          if ("false".equalsIgnoreCase(disableTestRetryGradleProperty)) {
            logger.info(
                "[$PLUGIN_ID_SHORT:$project.name] INFO - (task: '$testTask.name') " +
                "Gradle property '$PROPERTY_NAME' is present but it is set to 'false'. Detected maxRetries of $maxRetriesDetected is not changed."
            )
          }
          else {
            (testTask.extensions.getByName("retry").getProperties()["maxRetries"] as Property).set(maxRetriesReconfigured)
            logger.info(
                "[$PLUGIN_ID_SHORT:$project.name] INFO - (task: '$testTask.name') " +
                "Gradle property '$PROPERTY_NAME' is present. Detected maxRetries of $maxRetriesDetected is set to $maxRetriesReconfigured."
            )
          }

          return
        }
      }

      if (disablerExtension.enableJavaSystemPropertyCheck.get()) {
        if (System.getProperty(PROPERTY_NAME) != null) {
          String disableTestRetryJavaSystemProperty = System.getProperty(PROPERTY_NAME)
          if ("false".equalsIgnoreCase(disableTestRetryJavaSystemProperty)) {
            logger.info(
                "[$PLUGIN_ID_SHORT:$project.name] INFO - (task: '$testTask.name') " +
                "Java system property '$PROPERTY_NAME' is present but it is set to 'false'. Detected maxRetries of $maxRetriesDetected is not changed."
            )
          }
          else {
            (testTask.extensions.getByName("retry").getProperties()["maxRetries"] as Property).set(maxRetriesReconfigured)
            logger.info(
                "[$PLUGIN_ID_SHORT:$project.name] INFO - (task: '$testTask.name') " +
                "Java system property '$PROPERTY_NAME' is present. Detected maxRetries of $maxRetriesDetected is set to $maxRetriesReconfigured."
            )
          }

          return
        }
      }

      if (disablerExtension.enableEnvironmentVariableCheck.get()) {
        if (System.getenv(ENVIRONMENT_VARIABLE_NAME) != null) {
          String disableTestRetryEnvironmentVariable = System.getenv(ENVIRONMENT_VARIABLE_NAME)
          if ("false".equalsIgnoreCase(disableTestRetryEnvironmentVariable)) {
            logger.info(
                "[$PLUGIN_ID_SHORT:$project.name] INFO - (task: '$testTask.name') " +
                "Environment varable '$ENVIRONMENT_VARIABLE_NAME' is present but it is set to 'false'. Detected maxRetries of $maxRetriesDetected is not changed."
            )
          }
          else {
            (testTask.extensions.getByName("retry").getProperties()["maxRetries"] as Property).set(maxRetriesReconfigured)
            logger.info(
                "[$PLUGIN_ID_SHORT:$project.name] INFO - (task: '$testTask.name') " +
                "Environment variable '$ENVIRONMENT_VARIABLE_NAME' is present. Detected maxRetries of $maxRetriesDetected is set to $maxRetriesReconfigured."
            )
          }

          return
        }
      }

      logger.info("[$PLUGIN_ID_SHORT:$project.name] INFO - (task: '$testTask.name') Didn't reconfigure anything. Either parameters are not supplied or all checks are disabled.")
      return
    }

    return
  }
}
