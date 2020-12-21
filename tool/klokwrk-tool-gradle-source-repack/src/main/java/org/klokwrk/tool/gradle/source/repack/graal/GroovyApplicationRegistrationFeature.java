/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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

import com.oracle.svm.core.annotate.AutomaticFeature;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.graalvm.nativeimage.hosted.Feature;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Programmatically registers application's reflective Groovy classes with Graal native image compiler.
 * <p/>
 * Behavior can be configured via {@code kwrk-graal.properties} file in classpath:
 * <ul>
 *   <li>{@code kwrk-graal.classgraph-app-scan.packages}: comma separated list of root packages which will be considered by ClassGraph.</li>
 *   <li>{@code kwrk-graal.classgraph-app-scan.verbose}: boolean flag for turning on/off ClassGraph verbose output</li>
 *   <li>{@code kwrk-graal.classgraph-app-scan.ignored-jars-additional}: comma separated list of additional jars that will bi ignored by ClassGraph.</li>
 *   <li>
 *     {@code kwrk-graal.classgraph-app-scan.ignored-jars-default}: comma separated list of default jars that will bi ignored by ClassGraph. It corresponds to the value of
 *     {@link #CLASS_GRAPH_APP_SCAN_IGNORED_JARS_DEFAULT} constant. Usually not configured, but can be useful if one wants to reset default completely.
 *   </li>
 * <p/>
 * This class is used during compilation of GraalVM native image. It is auto-discovered by native image compiler. Needs to be written in Java.
 */
@SuppressWarnings("unused")
@AutomaticFeature
public class GroovyApplicationRegistrationFeature implements Feature {
  public static final String[] CLASS_GRAPH_APP_SCAN_IGNORED_JARS_DEFAULT = new String[] {
      "classgraph*.jar",
      "groovy*.jar",
      "jackson*.jar",
      "javax*.jar",
      "jsr*.jar",
      "logback*.jar",
      "*llvm*.jar",
      "micronaut*.jar",
      "netty*.jar",
      "picocli*.jar",
      "reactive*.jar",
      "rxjava*.jar",
      "slf4j*.jar",
      "snakeyaml*.jar",
      "spotbugs*.jar",
      "validation*.jar",

      // graal related jars
      "library-support.jar",
      "javacpp-shadowed.jar",
      "pointsto.jar",
      "objectfile.jar",
      "svm*.jar"
  };

  /**
   * Registers generated Groovy closure classes with Graal native image compiler.
   * <p/>
   * For some well known Groovy methods that take closures as parameters (i.e. each), Groovy generates helper classes in the fly next to the class that uses these methods with closure parameters.
   * For closures calls to work correctly, Groovy generated helper classes needs to be registered with GraalVM native image compiler.
   */
  public static void registerGeneratedClosureClasses(ScanResult scanResult) {
    ClassInfoList generatedGroovyClosureClassInfoList = scanResult.getClassesImplementing("org.codehaus.groovy.runtime.GeneratedClosure");
    RegistrationFeatureUtils.registerClasses(generatedGroovyClosureClassInfoList);
  }

  /**
   * Registers all classes that Groovy enhances with generated methods.
   */
  public static void registerClassesWithGeneratedMethods(ScanResult scanResult) {
    ClassInfoList generatedGroovyClosureClassInfoList = scanResult.getClassesWithMethodAnnotation("groovy.transform.Generated");
    RegistrationFeatureUtils.registerClasses(generatedGroovyClosureClassInfoList);
  }

  @Override
  public void beforeAnalysis(BeforeAnalysisAccess beforeAnalysisAccess) {
    ClassGraphAppScanConfiguration classGraphAppScanConfiguration = calculateClassGraphAppScanConfiguration(beforeAnalysisAccess.getApplicationClassLoader());

    ClassGraph gradleSourceRepackClassGraph = new ClassGraph()
        .enableClassInfo()
        .enableMethodInfo()
        .enableAnnotationInfo()
        .acceptPackages(classGraphAppScanConfiguration.getClassGraphAppScanPackages())
        .rejectJars(classGraphAppScanConfiguration.getClassGraphAppScanIgnoredJars());

    if (classGraphAppScanConfiguration.isClassGraphScanVerbose()) {
      gradleSourceRepackClassGraph.verbose();
    }

    try (ScanResult scanResult = gradleSourceRepackClassGraph.scan()) {
      registerGeneratedClosureClasses(scanResult);
      registerClassesWithGeneratedMethods(scanResult);
    }
  }

  private ClassGraphAppScanConfiguration calculateClassGraphAppScanConfiguration(ClassLoader classLoader) {
    boolean isClassGraphScanVerbose = false;
    String[] classGraphAppScanPackages = new String[] {};
    String[] classGraphAppScanIgnoredJarsDefault = CLASS_GRAPH_APP_SCAN_IGNORED_JARS_DEFAULT;
    String[] classGraphAppScanIgnoredJarsAdditional = new String[0];

    URL kwrkConfigUrl = classLoader.getResource("kwrk-graal.properties");
    if (kwrkConfigUrl != null) {
      Properties kwrkConfig = new Properties();
      try (InputStream inputStream = kwrkConfigUrl.openStream()) {
        kwrkConfig.load(inputStream);
      }
      catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }

      isClassGraphScanVerbose = Boolean.parseBoolean(kwrkConfig.getProperty("kwrk-graal.classgraph-app-scan.verbose", "false").toLowerCase());
      classGraphAppScanPackages = kwrkConfig.getProperty("kwrk-graal.classgraph-app-scan.packages", "").split(",");
      if ("".equals(classGraphAppScanPackages[0].trim())) {
        classGraphAppScanPackages = new String[0];
      }

      classGraphAppScanIgnoredJarsDefault = kwrkConfig.getProperty("kwrk-graal.classgraph-app-scan.ignored-jars-default", String.join(",", classGraphAppScanIgnoredJarsDefault)).split(",");
      if ("".equals(classGraphAppScanIgnoredJarsDefault[0].trim())) {
        classGraphAppScanIgnoredJarsDefault = new String[0];
      }

      classGraphAppScanIgnoredJarsAdditional = kwrkConfig.getProperty("kwrk-graal.classgraph-app-scan.ignored-jars-additional", "").split(",");
      if ("".equals(classGraphAppScanIgnoredJarsAdditional[0].trim())) {
        classGraphAppScanIgnoredJarsAdditional = new String[0];
      }
    }

    String[] classGraphAppScanIgnoredJarsFinal = new String[classGraphAppScanIgnoredJarsDefault.length + classGraphAppScanIgnoredJarsAdditional.length];
    System.arraycopy(classGraphAppScanIgnoredJarsDefault, 0, classGraphAppScanIgnoredJarsFinal, 0, classGraphAppScanIgnoredJarsDefault.length);
    System.arraycopy(classGraphAppScanIgnoredJarsAdditional, 0, classGraphAppScanIgnoredJarsFinal, classGraphAppScanIgnoredJarsDefault.length, classGraphAppScanIgnoredJarsAdditional.length);

    return new ClassGraphAppScanConfiguration(isClassGraphScanVerbose, classGraphAppScanPackages, classGraphAppScanIgnoredJarsFinal);
  }
}
