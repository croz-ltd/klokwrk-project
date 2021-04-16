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
package org.klokwrk.lib.archunit

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.core.importer.Location
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.util.regex.Pattern

/**
 * Collection of utilities that simplify some aspects of working with ArchUnit.
 */
@Slf4j
@CompileStatic
class ArchUnitUtils {

  @SuppressWarnings("SpaceInsideParentheses")
  /**
   * Imports ArchUnit's <code>JavaClasses</code> while allowing specifying packages that should be excluded from the import.
   * <p/>
   * Usage example (from klokwrk's code):
   * <p/>
   * <pre>
   * JavaClasses importedClasses= ArchUnitUtils.importJavaClassesFromPackages(
   *     ["org.klokwrk.cargotracker.booking.commandside", "org.klokwrk.cargotracker.booking.domain.model", "org.klokwrk.cargotracker.booking.axon.api.feature"],
   *     ["org.klokwrk.cargotracker.booking.commandside.test"]
   * )
   * </pre>
   *
   * @param packagesToImport Collection of package names to be imported. Each collection's string is exact package name without any special characters like <code>..</code> or <code>*</code>.
   * @param packagesToExclude Collection of package names to be excluded from the import. Each collection's string is exact package name without any special characters like <code>..</code> or
   *        <code>*</code>.
   * @param importOptions Collection of additional ArchUnit's <code>ImportOption</code> options. By default it contains an instance of <code>ImportOption.DoNotIncludeTests</code>.
   * @return Imported <code>JavaClasses</code>.
   */
  @SuppressWarnings("BracesForMethod")
  static JavaClasses importJavaClassesFromPackages(
      Collection<String> packagesToImport, Collection<String> packagesToExclude = [], Collection<ImportOption> importOptions = [new ImportOption.DoNotIncludeTests()] as Collection<ImportOption>)
  {
    ClassFileImporter forImportClassFileImporter = new ClassFileImporter().withImportOption(new ExcludePackagesImportOption(packagesToExclude))
    importOptions.each { ImportOption importOption -> forImportClassFileImporter = forImportClassFileImporter.withImportOption(importOption) }
    JavaClasses classesToImport = forImportClassFileImporter.importPackages(packagesToImport)

    log.debug "---------- Following classes are imported:"
    classesToImport*.name.sort(false).each { String className -> log.debug(className) }
    log.debug "----------"

    return classesToImport
  }

  private static class ExcludePackagesImportOption implements ImportOption {
    Collection<String> packageToExcludeCollection
    Collection<Pattern> patternToExcludeCollection

    ExcludePackagesImportOption(Collection<String> packageToExcludeCollection) {
      this.packageToExcludeCollection = packageToExcludeCollection
      this.patternToExcludeCollection = []

      this.packageToExcludeCollection.each { String packageToExclude ->
        patternToExcludeCollection << Pattern.compile(/^.*${ packageToExclude.replace(".", "/") }.*\.class$/)
      }
    }

    @Override
    boolean includes(Location location) {
      Boolean shouldExclude = patternToExcludeCollection.any { Pattern pattern -> location.matches(pattern) }
      return !shouldExclude
    }
  }
}
