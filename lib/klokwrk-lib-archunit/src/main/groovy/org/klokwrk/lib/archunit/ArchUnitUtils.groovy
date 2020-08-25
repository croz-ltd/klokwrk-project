package org.klokwrk.lib.archunit

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.core.importer.Location
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.util.regex.Pattern

@Slf4j
@CompileStatic
class ArchUnitUtils {
  static JavaClasses importJavaClassesFromPackages(
      Collection<String> packagesToImport,
      Collection<String> packagesToExclude = [],
      Collection<ImportOption> importOptions = [new ImportOption.DoNotIncludeTests()] as Collection<ImportOption>)
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
