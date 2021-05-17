package org.klokwrk.gradle.testretrydisabler

import groovy.transform.CompileStatic
import org.gradle.api.provider.Property

@CompileStatic
abstract class TestRetryDisablerPluginExtension {
  abstract Property<Boolean> getEnabled()

  abstract Property<Boolean> getEnableIdeaCheck()

  abstract Property<Boolean> getEnableGradlePropertyCheck()
  abstract Property<Boolean> getEnableJavaSystemPropertyCheck()
  abstract Property<Boolean> getEnableEnvironmentVariableCheck()

  abstract Property<Boolean> getEnableWarningsLogging()

  TestRetryDisablerPluginExtension() {
    enabled.convention(true)

    enableIdeaCheck.convention(true)

    enableGradlePropertyCheck.convention(true)
    enableJavaSystemPropertyCheck.convention(true)
    enableEnvironmentVariableCheck.convention(true)

    enableWarningsLogging.convention(true)
  }
}
