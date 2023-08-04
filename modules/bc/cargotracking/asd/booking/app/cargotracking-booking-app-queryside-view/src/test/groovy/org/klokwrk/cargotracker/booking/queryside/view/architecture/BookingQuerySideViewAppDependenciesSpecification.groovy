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
package org.klokwrk.cargotracker.booking.queryside.view.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.dependencies.SliceRule
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.klokwrk.cargotracker.booking.queryside.view.BookingQuerySideViewApplication
import org.klokwrk.lib.lo.archunit.ArchUnitUtils
import spock.lang.Shared
import spock.lang.Specification

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.belongToAnyOf
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage

class BookingQuerySideViewAppDependenciesSpecification extends Specification {
  @Shared
  JavaClasses allKlokwrkClasses

  void setupSpec() {
    allKlokwrkClasses = ArchUnitUtils.importJavaClassesFromPackages(
        ["org.klokwrk"],
        ["org.klokwrk.cargotracker.booking.commandside.test", "org.klokwrk.cargotracker.booking.queryside.test", "org.klokwrk.lib.lo.archunit"]
    )
  }

  void "there should not be any cycles"() {
    given:
    SliceRule sliceRule = SlicesRuleDefinition.slices().matching("org.klokwrk.(**)").should().beFreeOfCycles()

    expect:
    sliceRule.check(allKlokwrkClasses)
  }

  @SuppressWarnings("CodeNarc.ExplicitCallToOrMethod")
  void "queryside app should only access classes from allowed dependencies"() {
    given:
    String[] thirdPartyDependencyAllPackages = [
        "java..",
        "org.codehaus.groovy..",
        "groovy..",

        "jakarta.validation..",
        "jakarta.persistence..",

        "javax.measure..",

        "com.fasterxml.jackson.databind..",

        "net.croz.nrich.search..",

        "io.opentelemetry..",
        "org.axonframework.messaging..",
        "org.axonframework.spring..",
        "org.axonframework.tracing..",
        "org.hamcrest",
        "org.springframework..",

        "tech.units.indriya.unit.."
    ]

    String[] cargotrackerBookingDomainValueAllPackages = ["org.klokwrk.cargotracker.booking.domain.model.value.."]

    String[] cargotrackerBookingQuerySideViewAppAllPackages = ["org.klokwrk.cargotracker.booking.queryside.view.feature..", "org.klokwrk.cargotracker.booking.queryside.view.infrastructure.."]
    String[] cargotrackerBookingQuerySideModelRdbmsAllPackages = ["org.klokwrk.cargotracker.booking.queryside.model.rdbms.jpa.."]

    String[] cargotrackerBookingStandaloneOutAdapterAllPackages = ["org.klokwrk.cargotracker.booking.out.customer.."]

    String[] cargotrackerLibAxonCqrsAllPackages = ["org.klokwrk.cargotracker.lib.axon.cqrs.."]
    String[] cargotrackerLibAxonLoggingAllPackages = ["org.klokwrk.cargotracker.lib.axon.logging.."]
    String[] cargotrackerLibBoundaryApiAllPackages = ["org.klokwrk.cargotracker.lib.boundary.api.."]
    String[] cargotrackerLibBoundaryQueryApiAllPackages = ["org.klokwrk.cargotracking.lib.boundary.query.api.."]
    String[] cargotrackerLibWebAllPackages = ["org.klokwrk.cargotracking.lib.web.."]

    String[] klokwrkLibDatasourceProxyAllPackages = ["org.klokwrk.lib.hi.datasourceproxy.."]
    String[] klokwrkLibJacksonAllPackages = ["org.klokwrk.lib.hi.jackson.."]
    String[] klokwrkLibUomPackages = ["org.klokwrk.lib.lo.uom.."]
    String[] klokwrkLibValidationPackages = ["org.klokwrk.lib.hi.validation.."]

    String[] klokwrkLangGroovyAllPackages = ["org.klokwrk.lib.xlang.groovy.."]

    // @formatter:off
    //noinspection ChangeToOperator
    ArchRule rule = ArchRuleDefinition
        .classes()
            .that(
                resideInAnyPackage(cargotrackerBookingQuerySideViewAppAllPackages)
                .or(belongToAnyOf(BookingQuerySideViewApplication) as DescribedPredicate<JavaClass>)
            )
            // ignore testFixtures sourceSet
            .and()
            .haveNameNotMatching(/org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.fixture.*$/)
            .and()
            .haveNameNotMatching(/org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.*$/)
        .should().onlyAccessClassesThat(
            resideInAnyPackage(
                cargotrackerBookingDomainValueAllPackages +

                cargotrackerBookingQuerySideViewAppAllPackages +
                cargotrackerBookingQuerySideModelRdbmsAllPackages +

                cargotrackerBookingStandaloneOutAdapterAllPackages +

                cargotrackerLibAxonCqrsAllPackages +
                cargotrackerLibAxonLoggingAllPackages +
                cargotrackerLibBoundaryApiAllPackages +
                cargotrackerLibBoundaryQueryApiAllPackages +
                cargotrackerLibWebAllPackages +

                klokwrkLibDatasourceProxyAllPackages +
                klokwrkLibJacksonAllPackages +
                klokwrkLibUomPackages +
                klokwrkLibValidationPackages +

                klokwrkLangGroovyAllPackages +

                thirdPartyDependencyAllPackages as String[]
            )
            .or(belongToAnyOf(BookingQuerySideViewApplication) as DescribedPredicate<JavaClass>)
       )
    // @formatter:on

    expect:
    rule.check(allKlokwrkClasses)
  }
}
