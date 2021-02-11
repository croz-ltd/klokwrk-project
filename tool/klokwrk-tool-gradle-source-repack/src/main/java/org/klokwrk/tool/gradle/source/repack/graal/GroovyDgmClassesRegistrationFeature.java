/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.graalvm.nativeimage.hosted.Feature;

import java.util.Properties;

/**
 * Programmatically registers default Groovy methods (accessed by reflection) and {@code groovy.lang.Closure} extending classes from {@code org.codehaus.groovy.runtime} package with Graal native
 * image compiler.
 *
 * Programmatically registers default Groovy methods classes from {@code org.codehaus.groovy.runtime} package with Graal native image compiler.
 * <p/>
 * Default groovy method classes are all {@code org.codehaus.groovy.runtime.dgm$number.class} classes.
 * <p/>
 * In addition, all {@code groovy.lang.Closure} extending classes from {@code org.codehaus.groovy.runtime} are also registered as some of them are required and accessed from
 * {@code org.codehaus.groovy.runtime.DefaultGroovyMethods} class and other similar classes (see {@code org.codehaus.groovy.runtime.DefaultGroovyMethods.DGM_LIKE_CLASSES} constant).
 * <p/>
 * This class is used during compilation of GraalVM native image. It is auto-discovered by native image compiler. Needs to be written in Java.
 */
@AutomaticFeature
public class GroovyDgmClassesRegistrationFeature implements Feature {
  @Override
  public void beforeAnalysis(BeforeAnalysisAccess beforeAnalysisAccess) {
    GroovyDgmClassesRegistrationFeatureConfiguration configuration = calculateConfiguration(beforeAnalysisAccess.getApplicationClassLoader());
    if (!configuration.isEnabled()) {
      return;
    }

    String groovyRuntimePackage = "org.codehaus.groovy.runtime";

    ClassGraph groovyRuntimeClassGraph = new ClassGraph()
        .enableClassInfo()
        .acceptPackages(groovyRuntimePackage);

    if (configuration.isScanVerboseClassGraph()) {
      groovyRuntimeClassGraph.verbose();
    }

    try (ScanResult scanResult = groovyRuntimeClassGraph.scan()) {
      registerDefaultGroovyMethods(scanResult, configuration.isScanVerboseFeature());
      registerClosureExtendingClasses(scanResult, configuration.isScanVerboseFeature());
    }
  }

  private GroovyDgmClassesRegistrationFeatureConfiguration calculateConfiguration(ClassLoader classLoader) {
    boolean isEnabled = true;

    String scanVerbose;
    boolean isScanVerboseClassGraph = false;
    boolean isScanVerboseFeature = false;

    Properties kwrkGraalConfig = RegistrationFeatureUtils.loadKwrkGraalProperties(classLoader);
    if (kwrkGraalConfig != null) {
      isEnabled = Boolean.parseBoolean(kwrkGraalConfig.getProperty("kwrk-graal.registration-feature.dgm-classes.enabled", "true").toLowerCase());

      scanVerbose = kwrkGraalConfig.getProperty("kwrk-graal.registration-feature.dgm-classes.scan.verbose", "none").trim().toLowerCase();
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
    }

    return new GroovyDgmClassesRegistrationFeatureConfiguration(isEnabled, isScanVerboseClassGraph, isScanVerboseFeature);
  }

  protected void registerDefaultGroovyMethods(ScanResult scanResult, boolean isScanVerboseFeature) {
    ClassInfoList defaultGroovyMethodClassInfoCandidateList = scanResult.getSubclasses("org.codehaus.groovy.reflection.GeneratedMetaMethod");
    ClassInfoList defaultGroovyMethodClassInfoFilteredList =
        defaultGroovyMethodClassInfoCandidateList
            .filter((ClassInfo defaultGroovyMethodClassInfoCandidate) -> defaultGroovyMethodClassInfoCandidate.getName().matches("^org.codehaus.groovy.runtime.dgm\\$[0-9]+$"));

    if (isScanVerboseFeature) {
      RegistrationFeatureUtils.printClassInfoList("dgm-registerDefaultGroovyMethods", defaultGroovyMethodClassInfoFilteredList);
    }
    RegistrationFeatureUtils.registerClasses(defaultGroovyMethodClassInfoFilteredList);
  }

  protected void registerClosureExtendingClasses(ScanResult scanResult, boolean isScanVerboseFeature) {
    ClassInfoList groovyRuntimeClosureExtendingClassList = scanResult.getSubclasses("groovy.lang.Closure");

    if (isScanVerboseFeature) {
      RegistrationFeatureUtils.printClassInfoList("dgm-registerClosureExtendingClasses", groovyRuntimeClosureExtendingClassList);
    }
    RegistrationFeatureUtils.registerClasses(groovyRuntimeClosureExtendingClassList);
  }
}
