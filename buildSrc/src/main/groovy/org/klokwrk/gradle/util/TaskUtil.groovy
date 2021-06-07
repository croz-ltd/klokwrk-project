package org.klokwrk.gradle.util

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

@CompileStatic
class TaskUtil {
  /**
   * Converts Gradle project properties (i.e. properties specified via gradle.properties file) with 'DockerImageVersion' suffix into Java system properties.
   * This way all versions with DockerImageVersion suffix can be used by Testcontainers factories that create and start containers.
   */
  static void convertDockerImageVersionsIntoJavaSystemPropertiesForTestTask(Test testTask, Project project) {
    Map<String, String> dockerImageVersions = project.getRootProject().properties.findAll { String key, Object value ->
      (value instanceof String) && key.toLowerCase().endsWith("DockerImageVersion".toLowerCase())
    } as Map<String, String>

    dockerImageVersions.each { String key, String value ->
      testTask.systemProperty(key, value)
    }
  }
}
