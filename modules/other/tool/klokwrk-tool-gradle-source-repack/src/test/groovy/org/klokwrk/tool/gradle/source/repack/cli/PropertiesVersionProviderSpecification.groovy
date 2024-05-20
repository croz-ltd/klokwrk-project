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
package org.klokwrk.tool.gradle.source.repack.cli

import spock.lang.Specification

class PropertiesVersionProviderSpecification extends Specification {
  void "should return expected version info"() {
    given:
    PropertiesVersionProvider propertiesVersionProvider = new PropertiesVersionProvider()

    when:
    String versionString = propertiesVersionProvider.version[0]

    then:
    versionString.startsWith("klokwrk-tool-gradle-source-repack")
  }

  void "should return expected message when properties file is missing"() {
    given:
    PropertiesVersionProvider propertiesVersionProvider = new PropertiesVersionProvider()
    PropertiesVersionProvider propertiesVersionProviderSpy = Spy(propertiesVersionProvider)
    propertiesVersionProviderSpy.fetchVersionPropertiesPath() >> "/non-existing-version.properties"

    when:
    String versionString = propertiesVersionProviderSpy.version[0]

    then:
    versionString == "Version info is not available."
  }
}
