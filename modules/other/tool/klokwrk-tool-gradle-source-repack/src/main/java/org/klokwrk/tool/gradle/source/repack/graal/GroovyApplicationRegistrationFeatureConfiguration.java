/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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

public class GroovyApplicationRegistrationFeatureConfiguration {
  private final boolean isEnabled;

  private final boolean isScanVerboseClassGraph;
  private final boolean isScanVerboseFeature;

  private final String[] classGraphAppScanPackages;

  public GroovyApplicationRegistrationFeatureConfiguration(boolean isEnabled, boolean isScanVerboseClassGraph, boolean isScanVerboseFeature, String[] classGraphAppScanPackages) {
    this.isEnabled = isEnabled;

    this.isScanVerboseClassGraph = isScanVerboseClassGraph;
    this.isScanVerboseFeature = isScanVerboseFeature;

    this.classGraphAppScanPackages = classGraphAppScanPackages;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public boolean isScanVerboseClassGraph() {
    return isScanVerboseClassGraph;
  }

  public boolean isScanVerboseFeature() {
    return isScanVerboseFeature;
  }

  public String[] getClassGraphAppScanPackages() {
    return classGraphAppScanPackages;
  }
}
