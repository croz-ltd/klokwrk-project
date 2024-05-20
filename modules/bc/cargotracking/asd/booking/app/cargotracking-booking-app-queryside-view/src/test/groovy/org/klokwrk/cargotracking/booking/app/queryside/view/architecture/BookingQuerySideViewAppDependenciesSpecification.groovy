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
package org.klokwrk.cargotracking.booking.app.queryside.view.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.dependencies.SliceRule
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.klokwrk.cargotracking.booking.app.queryside.view.BookingQuerySideViewApplication
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
        ["org.klokwrk.."]
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

    String[] cargotrackingBookingDomainValueAllPackages = ["org.klokwrk.cargotracking.domain.model.value.."]

    String[] cargotrackingBookingQuerySideViewAppAllPackages = ["org.klokwrk.cargotracking.booking.app.queryside.view.feature..", "org.klokwrk.cargotracking.booking.app.queryside.view.infrastructure.."]
    String[] cargotrackingBookingQuerySideModelRdbmsAllPackages = ["org.klokwrk.cargotracking.booking.lib.queryside.model.rdbms.jpa.."]

    String[] cargotrackingBookingStandaloneOutAdapterAllPackages = ["org.klokwrk.cargotracking.booking.lib.out.customer.."]

    String[] cargotrackingLibAxonCqrsAllPackages = ["org.klokwrk.cargotracking.lib.axon.cqrs.."]
    String[] cargotrackingLibAxonLoggingAllPackages = ["org.klokwrk.cargotracking.lib.axon.logging.."]
    String[] cargotrackingLibBoundaryApiAllPackages = ["org.klokwrk.cargotracking.lib.boundary.api.."]
    String[] cargotrackingLibBoundaryQueryApiAllPackages = ["org.klokwrk.cargotracking.lib.boundary.query.api.."]
    String[] cargotrackingLibWebAllPackages = ["org.klokwrk.cargotracking.lib.web.."]

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
                resideInAnyPackage(cargotrackingBookingQuerySideViewAppAllPackages)
                .or(belongToAnyOf(BookingQuerySideViewApplication) as DescribedPredicate<JavaClass>)
            )
            // ignore testFixtures sourceSet
            .and()
            .haveNameNotMatching(/org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.*$/)
            .and()
            .haveNameNotMatching(/org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.assertion.*$/)
        .should().onlyAccessClassesThat(
            resideInAnyPackage(
                cargotrackingBookingDomainValueAllPackages +

                cargotrackingBookingQuerySideViewAppAllPackages +
                cargotrackingBookingQuerySideModelRdbmsAllPackages +

                cargotrackingBookingStandaloneOutAdapterAllPackages +

                cargotrackingLibAxonCqrsAllPackages +
                cargotrackingLibAxonLoggingAllPackages +
                cargotrackingLibBoundaryApiAllPackages +
                cargotrackingLibBoundaryQueryApiAllPackages +
                cargotrackingLibWebAllPackages +

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
