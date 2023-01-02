/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.klokwrk.tool.gradle.source.repack.graal;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.graalvm.nativeimage.hosted.Feature;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Programmatically registers application's reflective Groovy classes with Graal native image compiler.
 * <p/>
 * Behavior can be configured via {@code kwrk-graal.properties} file in classpath:
 * <ul>
 *   <li>{@code kwrk-graal.classgraph-app-scan.packages}: comma separated list of root packages which will be considered by ClassGraph.</li>
 *   <li>{@code kwrk-graal.classgraph-app-scan.verbose}: boolean flag for turning on/off ClassGraph verbose output</li>
 * <p/>
 * This class should be used during compilation of GraalVM native image. It can be configured via {@code --features} option of native image compiler. Needs to be written in Java.
 */
@SuppressWarnings("unused")
public class GroovyApplicationRegistrationFeature implements Feature {

  @Override
  public void beforeAnalysis(BeforeAnalysisAccess beforeAnalysisAccess) {
    GroovyApplicationRegistrationFeatureConfiguration configuration = calculateConfiguration(beforeAnalysisAccess.getApplicationClassLoader());
    if (!configuration.isEnabled()) {
      return;
    }

    ClassGraph gradleSourceRepackClassGraph = new ClassGraph()
        .enableClassInfo()
        .enableMethodInfo()
        .enableAnnotationInfo()
        .acceptPackages(configuration.getClassGraphAppScanPackages());

    if (configuration.isScanVerboseClassGraph()) {
      gradleSourceRepackClassGraph.verbose();
    }

    try (ScanResult scanResult = gradleSourceRepackClassGraph.scan()) {
      registerGeneratedClosureClasses(scanResult, configuration.isScanVerboseFeature());
      registerAllApplicationClasses(scanResult, configuration.isScanVerboseFeature());
    }
  }

  @SuppressWarnings({"ReassignedVariable", "ConstantConditions", "EnhancedSwitchMigration"})
  private GroovyApplicationRegistrationFeatureConfiguration calculateConfiguration(ClassLoader classLoader) {
    boolean isEnabled = true;

    String scanVerbose;
    boolean isScanVerboseClassGraph = false;
    boolean isScanVerboseFeature = false;

    String[] classGraphAppScanPackages = new String[] {};

    Properties kwrkGraalConfig = RegistrationFeatureUtils.loadKwrkGraalProperties(classLoader);
    if (kwrkGraalConfig != null) {
      isEnabled = Boolean.parseBoolean(kwrkGraalConfig.getProperty("kwrk-graal.registration-feature.application.enabled", "true").toLowerCase());

      scanVerbose = kwrkGraalConfig.getProperty("kwrk-graal.registration-feature.application.scan.verbose", "none").trim().toLowerCase();
      //noinspection DuplicatedCode
      switch (scanVerbose) {
        case "all":
          isScanVerboseClassGraph = true;
          isScanVerboseFeature = true;
          break;
        case "feature":
          isScanVerboseClassGraph = false;
          isScanVerboseFeature = true;
          break;
        case "classgraph":
          isScanVerboseClassGraph = true;
          isScanVerboseFeature = false;
          break;
        default:
          isScanVerboseClassGraph = false;
          isScanVerboseFeature = false;
          break;
      }

      classGraphAppScanPackages = kwrkGraalConfig.getProperty("kwrk-graal.registration-feature.application.scan.packages", "").trim().split(",");
      if ("".equals(classGraphAppScanPackages[0].trim())) {
        classGraphAppScanPackages = new String[0];
      }
    }

    return new GroovyApplicationRegistrationFeatureConfiguration(isEnabled, isScanVerboseClassGraph, isScanVerboseFeature, classGraphAppScanPackages);
  }

  /**
   * Registers generated Groovy closure classes with Graal native image compiler.
   * <p/>
   * For some well known Groovy methods that take closures as parameters (i.e. each), Groovy generates helper classes in the fly next to the class using these methods with closure parameters.
   * For closures calls to work correctly, Groovy generated helper classes needs to be registered with GraalVM native image compiler.
   */
  private static void registerGeneratedClosureClasses(ScanResult scanResult, boolean isVerboseOutputEnabled) {
    ClassInfoList generatedGroovyClosureClassInfoList = scanResult.getClassesImplementing("org.codehaus.groovy.runtime.GeneratedClosure");

    if (isVerboseOutputEnabled) {
      RegistrationFeatureUtils.printClassInfoList("application-registerGeneratedClosureClasses", generatedGroovyClosureClassInfoList);
    }
    RegistrationFeatureUtils.registerClasses(generatedGroovyClosureClassInfoList);
  }

  /**
   * Registers all application classes to ensure that callbacks from generated closure classes work as expected.
   * <p/>
   * Generated closure classes are excluded from this registration.
   * <p/>
   * This might be implemented in some other way if we discover how to find application classes that closure generated classes calls back.
   */
  private static void registerAllApplicationClasses(ScanResult scanResult, boolean isVerboseOutputEnabled) {
    ClassInfoList generatedGroovyClosureClassInfoList = scanResult.getClassesImplementing("org.codehaus.groovy.runtime.GeneratedClosure");
    ClassInfoList allApplicationClasses = scanResult.getClassesImplementing("groovy.lang.GroovyObject");

    allApplicationClasses = allApplicationClasses.filter(classInfo -> {
      List<String> excludedClasses = Arrays.asList("groovy.lang.Closure", "groovy.lang.GroovyObjectSupport");
      if (excludedClasses.contains(classInfo.getName())) {
        return false;
      }

      //noinspection RedundantIfStatement
      if (generatedGroovyClosureClassInfoList.contains(classInfo)) {
        return false;
      }

      return true;
    });

    if (isVerboseOutputEnabled) {
      RegistrationFeatureUtils.printClassInfoList("application-registerAllApplicationClasses", allApplicationClasses);
    }
    RegistrationFeatureUtils.registerClasses(allApplicationClasses);
  }
}
