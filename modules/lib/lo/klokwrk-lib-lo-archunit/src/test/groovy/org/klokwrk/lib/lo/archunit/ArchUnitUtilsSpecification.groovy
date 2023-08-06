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
package org.klokwrk.lib.lo.archunit

import com.tngtech.archunit.core.domain.JavaClasses
import org.klokwrk.lib.lo.archunit.samplepackages.archunitutils.all.excluded.ExcludedClass
import org.klokwrk.lib.lo.archunit.samplepackages.archunitutils.all.included.IncludedClass
import spock.lang.Specification

class ArchUnitUtilsSpecification extends Specification {
  void "should not import test classes by default"() {
    when:
    JavaClasses importedClasses  = ArchUnitUtils.importJavaClassesFromPackages(["org.klokwrk.lib.lo.archunit.samplepackages.archunitutils.all"])

    then:
    importedClasses.isEmpty()

    and:
    when:
    importedClasses  = ArchUnitUtils.importJavaClassesFromPackages(["org.klokwrk.lib.lo.archunit.samplepackages.archunitutils.all"], [])

    then:
    importedClasses.isEmpty()
  }

  void "should import classes"() {
    when:
    JavaClasses importedClasses  = ArchUnitUtils.importJavaClassesFromPackages(["org.klokwrk.lib.lo.archunit.samplepackages.archunitutils.all"], [], [])

    then:
    importedClasses.containPackage("org.klokwrk.lib.lo.archunit.samplepackages.archunitutils.all")
    importedClasses.containPackage("org.klokwrk.lib.lo.archunit.samplepackages.archunitutils.all.included")
    importedClasses.containPackage("org.klokwrk.lib.lo.archunit.samplepackages.archunitutils.all.excluded")
    importedClasses.contain(IncludedClass)
    importedClasses.contain(ExcludedClass)
  }

  @SuppressWarnings('GroovyPointlessBoolean')
  void "should import classes with exclusions"() {
    when:
    JavaClasses importedClasses  =
        ArchUnitUtils.importJavaClassesFromPackages(["org.klokwrk.lib.lo.archunit.samplepackages.archunitutils.all"], ["org.klokwrk.lib.lo.archunit.samplepackages.archunitutils.all.excluded"], [])

    then:
    importedClasses.containPackage("org.klokwrk.lib.lo.archunit.samplepackages.archunitutils.all")
    importedClasses.containPackage("org.klokwrk.lib.lo.archunit.samplepackages.archunitutils.all.included")
    importedClasses.containPackage("org.klokwrk.lib.lo.archunit.samplepackages.archunitutils.all.excluded") == false
    importedClasses.contain(IncludedClass)
    importedClasses.contain(ExcludedClass) == false
  }
}
