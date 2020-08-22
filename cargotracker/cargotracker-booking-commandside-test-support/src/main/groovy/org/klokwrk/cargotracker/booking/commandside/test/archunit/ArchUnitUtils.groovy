package org.klokwrk.cargotracker.booking.commandside.test.archunit

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class ArchUnitUtils {
  static JavaClasses importJavaClassesFromPackages(Collection<String> packagesToImport, Collection<String> packagesToExclude = []) {
    JavaClasses classesToImportUnfiltered = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages(packagesToImport)

    JavaClasses classesToExclude = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages(packagesToExclude)

    JavaClasses classesToImport = classesToImportUnfiltered
        .that(new ExcludingPredicate(classesToExclude))
        .as(classesToImportUnfiltered.description)

    log.debug "---------- Following classes are imported:"
    classesToImport*.name.sort(false).each { String className -> log.debug(className) }
    log.debug "----------"

    return classesToImport
  }

  private static class ExcludingPredicate extends DescribedPredicate<JavaClass> {
    private final JavaClasses classesToExclude

    ExcludingPredicate(JavaClasses classesToExclude) {
      super("excluding", [])
      this.classesToExclude = classesToExclude
    }

    @Override
    boolean apply(JavaClass inputJavaClass) {
      if (classesToExclude.contain(inputJavaClass.name)) {
        return false
      }

      return true
    }
  }
}
