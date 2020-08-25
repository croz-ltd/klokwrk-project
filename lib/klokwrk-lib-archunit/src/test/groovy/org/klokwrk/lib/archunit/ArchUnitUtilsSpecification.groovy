package org.klokwrk.lib.archunit

import com.tngtech.archunit.core.domain.JavaClasses
import org.klokwrk.lib.archunit.testpackages.archunitutils.all.excluded.ExcludedClass
import org.klokwrk.lib.archunit.testpackages.archunitutils.all.included.IncludedClass
import spock.lang.Specification

class ArchUnitUtilsSpecification extends Specification {
  void "should not import test classes by default"() {
    when:
    JavaClasses importedClasses  = ArchUnitUtils.importJavaClassesFromPackages(["org.klokwrk.lib.archunit.testpackages.archunitutils.all"])

    then:
    importedClasses.isEmpty()

    and:
    when:
    importedClasses  = ArchUnitUtils.importJavaClassesFromPackages(["org.klokwrk.lib.archunit.testpackages.archunitutils.all"], [])

    then:
    importedClasses.isEmpty()
  }

  void "should import classes"() {
    when:
    JavaClasses importedClasses  = ArchUnitUtils.importJavaClassesFromPackages(["org.klokwrk.lib.archunit.testpackages.archunitutils.all"], [], [])

    then:
    importedClasses.containPackage("org.klokwrk.lib.archunit.testpackages.archunitutils.all")
    importedClasses.containPackage("org.klokwrk.lib.archunit.testpackages.archunitutils.all.included")
    importedClasses.containPackage("org.klokwrk.lib.archunit.testpackages.archunitutils.all.excluded")
    importedClasses.contain(IncludedClass)
    importedClasses.contain(ExcludedClass)
  }

  @SuppressWarnings('GroovyPointlessBoolean')
  void "should import classes with exclusions"() {
    when:
    JavaClasses importedClasses  =
        ArchUnitUtils.importJavaClassesFromPackages(["org.klokwrk.lib.archunit.testpackages.archunitutils.all"], ["org.klokwrk.lib.archunit.testpackages.archunitutils.all.excluded"], [])

    then:
    importedClasses.containPackage("org.klokwrk.lib.archunit.testpackages.archunitutils.all")
    importedClasses.containPackage("org.klokwrk.lib.archunit.testpackages.archunitutils.all.included")
    importedClasses.containPackage("org.klokwrk.lib.archunit.testpackages.archunitutils.all.excluded") == false
    importedClasses.contain(IncludedClass)
    importedClasses.contain(ExcludedClass) == false
  }
}
