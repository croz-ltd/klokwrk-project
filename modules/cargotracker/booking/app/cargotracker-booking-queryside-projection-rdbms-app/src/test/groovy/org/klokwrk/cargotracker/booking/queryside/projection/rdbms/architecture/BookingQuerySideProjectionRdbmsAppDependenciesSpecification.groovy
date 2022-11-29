/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.projection.rdbms.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.dependencies.SliceRule
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.klokwrk.cargotracker.booking.queryside.projection.rdbms.BookingQuerySideProjectionRdbmsApplication
import org.klokwrk.lib.archunit.ArchUnitUtils
import spock.lang.Shared
import spock.lang.Specification

class BookingQuerySideProjectionRdbmsAppDependenciesSpecification extends Specification {
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

  @SuppressWarnings("CodeNarc.ExplicitCallToOrMethod")
  void "rdbms projection app should only access classes from allowed dependencies"() {
    given:
    String[] thirdPartyDependencyAllPackages = [
        "java..",
        "org.codehaus.groovy..",
        "groovy..",

        "org.axonframework.config..",
        "org.axonframework.eventhandling..",
        "org.axonframework.messaging..",

        "org.springframework..",

        "javax.measure..",
        "tech.units.indriya..",

        "com.vladmihalcea.spring.repository.."
    ]

    String[] cargotrackerBookingProjectionRdbmsAppAllPackages = [
        "org.klokwrk.cargotracker.booking.queryside.projection.rdbms.feature..", "org.klokwrk.cargotracker.booking.queryside.projection.rdbms.infrastructure.."
    ]
    String[] cargotrackerBookingDomainEventAllPackages = ["org.klokwrk.cargotracker.booking.domain.model.event.."]
    String[] cargotrackerBookingDomainValueAllPackages = ["org.klokwrk.cargotracker.booking.domain.model.value.."]
    String[] cargotrackerBookingQuerysideProjectionRdbmsModelAllPackages = [
        "org.klokwrk.cargotracker.booking.queryside.model.rdbms.jpa..",
        "org.klokwrk.lib.springframework.data.jpa.repository.hibernate.."
    ]

    String[] cargotrackerLibAxonLoggingAllPackages = ["org.klokwrk.cargotracker.lib.axon.logging.."]
    String[] cargotrackerLibBoundaryApiAllPackages = ["org.klokwrk.cargotracker.lib.boundary.api.."]

    String[] klokwrkLibDatasourceProxyAllPackages = ["org.klokwrk.lib.datasourceproxy.."]
    String[] klokwrkLibJacksonAllPackages = ["org.klokwrk.lib.jackson.."]

    String[] klokwrkLangGroovy = ["org.klokwrk.lang.groovy.."]

    // @formatter:off
    //noinspection ChangeToOperator
    ArchRule rule = ArchRuleDefinition
        .classes().that(
            JavaClass.Predicates.resideInAnyPackage(cargotrackerBookingProjectionRdbmsAppAllPackages)
                                .or(JavaClass.Predicates.belongToAnyOf(BookingQuerySideProjectionRdbmsApplication) as DescribedPredicate<JavaClass>)
        )
        .should().onlyAccessClassesThat(JavaClass.Predicates
            .resideInAnyPackage(
                cargotrackerBookingProjectionRdbmsAppAllPackages +
                cargotrackerBookingDomainEventAllPackages +
                cargotrackerBookingDomainValueAllPackages +
                cargotrackerBookingQuerysideProjectionRdbmsModelAllPackages +

                cargotrackerLibAxonLoggingAllPackages +
                cargotrackerLibBoundaryApiAllPackages +

                klokwrkLibDatasourceProxyAllPackages +
                klokwrkLibJacksonAllPackages +

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
