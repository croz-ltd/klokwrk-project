package org.klokwrk.cargotracker.booking.queryside.architecture

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.dependencies.SliceRule
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.klokwrk.cargotracker.booking.queryside.BookingQuerySideApplication
import org.klokwrk.lib.archunit.ArchUnitUtils
import spock.lang.Shared
import spock.lang.Specification

class BookingQuerySideAppDependenciesSpecification extends Specification {
  @Shared
  JavaClasses allKlokwrkClasses

  void setupSpec() {
    allKlokwrkClasses = ArchUnitUtils.importJavaClassesFromPackages(
        ["org.klokwrk"],
        ["org.klokwrk.cargotracker.booking.commandside.test", "org.klokwrk.cargotracker.booking.queryside.test", "org.klokwrk.lib.archunit"]
    )
  }

  void "there should not be any cycles"() {
    given:
    SliceRule sliceRule = SlicesRuleDefinition.slices().matching("org.klokwrk.(**)").should().beFreeOfCycles()

    expect:
    sliceRule.check(allKlokwrkClasses)
  }

  @SuppressWarnings("ExplicitCallToOrMethod")
  void "queryside app should only access classes from allowed dependencies"() {
    given:
    String[] thirdPartyDependencyAllPackages = [
        "java..",
        "org.codehaus.groovy..",
        "groovy..",

        "org.axonframework.spring..",
        "org.springframework.."
    ]

    String[] cargotrackerBookingQuerysideAppAllPackages = ["org.klokwrk.cargotracker.booking.queryside.feature..", "org.klokwrk.cargotracker.booking.queryside.infrastructure.."]
    String[] cargotrackerBookingQuerysideRdbmsProjectionModelAllPackages = ["org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model.."]

    String[] cargotrackerLibAxonCqrsAllPackages = ["org.klokwrk.cargotracker.lib.axon.cqrs.."]
    String[] cargotrackerLibAxonLoggingAllPackages = ["org.klokwrk.cargotracker.lib.axon.logging.."]
    String[] cargotrackerLibBoundaryApiAllPackages = ["org.klokwrk.cargotracker.lib.boundary.api.."]
    String[] cargotrackerLibWebAllPackages = ["org.klokwrk.cargotracker.lib.web.."]

    String[] klokwrkLibDatasourceProxyAllPackages = ["org.klokwrk.lib.datasourceproxy.."]
    String[] klokwrkLibJacksonAllPackages = ["org.klokwrk.lib.jackson.."]

    // @formatter:off
    //noinspection ChangeToOperator
    ArchRule rule = ArchRuleDefinition
        .classes().that(
            JavaClass.Predicates.resideInAnyPackage(cargotrackerBookingQuerysideAppAllPackages)
                                .or(JavaClass.Predicates.belongToAnyOf(BookingQuerySideApplication))
        )
        .should().onlyAccessClassesThat(JavaClass.Predicates
            .resideInAnyPackage(
                cargotrackerBookingQuerysideAppAllPackages +
                cargotrackerBookingQuerysideRdbmsProjectionModelAllPackages +

                cargotrackerLibAxonCqrsAllPackages +
                cargotrackerLibAxonLoggingAllPackages +
                cargotrackerLibBoundaryApiAllPackages +
                cargotrackerLibWebAllPackages +

                klokwrkLibDatasourceProxyAllPackages +
                klokwrkLibJacksonAllPackages +

                thirdPartyDependencyAllPackages as String[]
            )
            .or(JavaClass.Predicates.belongToAnyOf(BookingQuerySideApplication))
       )
    // @formatter:on

    expect:
    rule.check(allKlokwrkClasses)
  }
}
