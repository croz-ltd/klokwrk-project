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
package org.klokwrk.cargotracking.booking.app.queryside.projection.rdbms.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.dependencies.SliceRule
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.klokwrk.cargotracking.booking.app.queryside.projection.rdbms.BookingQuerySideProjectionRdbmsApplication
import org.klokwrk.lib.lo.archunit.ArchUnitUtils
import spock.lang.Shared
import spock.lang.Specification

class BookingQuerySideProjectionRdbmsAppDependenciesSpecification extends Specification {
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
  void "rdbms projection app should only access classes from allowed dependencies"() {
    given:
    String[] thirdPartyDependencyAllPackages = [
        "java..",
        "org.codehaus.groovy..",
        "groovy..",

        "io.opentelemetry..",
        "org.axonframework.config..",
        "org.axonframework.eventhandling..",
        "org.axonframework.messaging..",
        "org.axonframework.tracing..",

        "org.springframework..",

        "javax.measure..",
        "tech.units.indriya..",

        "io.hypersistence.utils.spring.repository..",
        "com.fasterxml.jackson.databind..",

        "net.ttddyy.observation.tracing..",
        "io.micrometer.observation.."
    ]

    String[] cargotrackingBookingProjectionRdbmsAppAllPackages = [
        "org.klokwrk.cargotracking.booking.app.queryside.projection.rdbms.feature..", "org.klokwrk.cargotracking.booking.app.queryside.projection.rdbms.infrastructure.."
    ]
    String[] cargotrackingBookingDomainEventAllPackages = ["org.klokwrk.cargotracking.domain.model.event.."]
    String[] cargotrackingBookingDomainValueAllPackages = ["org.klokwrk.cargotracking.domain.model.value.."]
    String[] cargotrackingBookingQuerysideProjectionRdbmsModelAllPackages = [
        "org.klokwrk.cargotracking.booking.lib.queryside.model.rdbms.jpa..",
        "org.klokwrk.lib.hi.spring.data.jpa.repository.hibernate.."
    ]

    String[] cargotrackingLibAxonErrorhandlingAllPackages = ["org.klokwrk.cargotracking.lib.axon.errorhandling.."]
    String[] cargotrackingLibAxonLoggingAllPackages = ["org.klokwrk.cargotracking.lib.axon.logging.."]
    String[] cargotrackingLibBoundaryApiAllPackages = ["org.klokwrk.cargotracking.lib.boundary.api.."]

    String[] klokwrkLibDatasourceProxyAllPackages = ["org.klokwrk.lib.hi.datasourceproxy.."]
    String[] klokwrkLibJacksonAllPackages = ["org.klokwrk.lib.hi.jackson.."]
    String[] klokwrkLibUomAllPackages = ["org.klokwrk.lib.lo.uom.."]

    String[] klokwrkLangGroovy = ["org.klokwrk.lib.xlang.groovy.."]

    // @formatter:off
    //noinspection ChangeToOperator
    ArchRule rule = ArchRuleDefinition
        .classes().that(
            JavaClass.Predicates.resideInAnyPackage(cargotrackingBookingProjectionRdbmsAppAllPackages)
                                .or(JavaClass.Predicates.belongToAnyOf(BookingQuerySideProjectionRdbmsApplication) as DescribedPredicate<JavaClass>)
        )
        .should().onlyAccessClassesThat(JavaClass.Predicates
            .resideInAnyPackage(
                cargotrackingBookingProjectionRdbmsAppAllPackages +
                cargotrackingBookingDomainEventAllPackages +
                cargotrackingBookingDomainValueAllPackages +
                cargotrackingBookingQuerysideProjectionRdbmsModelAllPackages +

                cargotrackingLibAxonErrorhandlingAllPackages +
                cargotrackingLibAxonLoggingAllPackages +
                cargotrackingLibBoundaryApiAllPackages +

                klokwrkLibDatasourceProxyAllPackages +
                klokwrkLibJacksonAllPackages +
                klokwrkLibUomAllPackages +

                klokwrkLangGroovy +

                thirdPartyDependencyAllPackages as String[]
            )
            .or(JavaClass.Predicates.belongToAnyOf(BookingQuerySideProjectionRdbmsApplication) as DescribedPredicate<JavaClass>)
       )
    // @formatter:on

    expect:
    rule.check(allKlokwrkClasses)
  }
}
