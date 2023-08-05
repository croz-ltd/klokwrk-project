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
package org.klokwrk.cargotracking.booking.app.commandside.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.dependencies.SliceRule
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.klokwrk.lib.lo.archunit.ArchUnitUtils
import spock.lang.Shared
import spock.lang.Specification

class BookingCommandSideAppDependenciesSpecification extends Specification {
  @Shared
  JavaClasses allKlokwrkClasses

  void setupSpec() {
    allKlokwrkClasses = ArchUnitUtils.importJavaClassesFromPackages(
        ["org.klokwrk"],
        ["org.klokwrk.cargotracking.booking.app.commandside.test", "org.klokwrk.cargotracker.booking.queryside.test", "org.klokwrk.lib.lo.archunit"]
    )
  }

  void "there should not be any cycles"() {
    given:
    SliceRule sliceRule = SlicesRuleDefinition.slices().matching("org.klokwrk.(**)").should().beFreeOfCycles()

    expect:
    sliceRule.check(allKlokwrkClasses)
  }

  void "commandside app spring boot application class should only access classes from allowed dependencies"() {
    given:
    String[] thirdPartyDependencyAllPackages = [
        "java..",
        "org.codehaus.groovy..",
        "groovy..",

        "org.springframework.boot"
    ]

    String[] allowedApplicationSubPackages = ["org.klokwrk.cargotracking.booking.app.commandside.infrastructure.springbootconfig"]

    String[] cargotrackingBookingCommandsideAppSpringBootApplicationPackages = ["org.klokwrk.cargotracking.booking.app.commandside"]

    // @formatter:off
    ArchRule rule = ArchRuleDefinition
        .classes().that().resideInAnyPackage(cargotrackingBookingCommandsideAppSpringBootApplicationPackages)
        .should().onlyAccessClassesThat().resideInAnyPackage(
            cargotrackingBookingCommandsideAppSpringBootApplicationPackages +
            allowedApplicationSubPackages +

            thirdPartyDependencyAllPackages as String[]
        )
    // @formatter:on

    expect:
    rule.check(allKlokwrkClasses)
  }

  void "domain aggregate and entity classes should only access classes from allowed dependencies"() {
    given:
    String[] thirdPartyDependencyAllPackages = [
        "java..",

        "org.codehaus.groovy..",
        "groovy..",

        "javax.measure..",
        "tech.units.indriya..",

        "org.axonframework.modelling.command..",
        "org.hamcrest"
    ]

    String[] cargotrackingBookingDomainAggregateAllPackages = ["org.klokwrk.cargotracking.domain.model.aggregate.."]

    String[] cargotrackingBookingServiceAllPackages = ["org.klokwrk.cargotracking.domain.model.service.."]
    String[] cargotrackingBookingCommandAllPackages = ["org.klokwrk.cargotracking.domain.model.command.."]
    String[] cargotrackingBookingEventAllPackages = ["org.klokwrk.cargotracking.domain.model.event.."]
    String[] cargotrackingBookingDomainValueAllPackages = ["org.klokwrk.cargotracking.domain.model.value.."]

    String[] cargotrackingLibBoundaryApiAllPackages = ["org.klokwrk.cargotracking.lib.boundary.api.."]

    String[] klokwrkLibUomAllPackages = ["org.klokwrk.lib.lo.uom.."]

    String[] klokwrkLangGroovyAllPackages = ["org.klokwrk.lib.xlang.groovy.."]

    // @formatter:off
    ArchRule rule = ArchRuleDefinition
        .classes().that().resideInAnyPackage(cargotrackingBookingDomainAggregateAllPackages)
        .should().onlyAccessClassesThat().resideInAnyPackage(
            cargotrackingBookingDomainAggregateAllPackages +

            cargotrackingBookingServiceAllPackages +
            cargotrackingBookingCommandAllPackages +
            cargotrackingBookingEventAllPackages +
            cargotrackingBookingDomainValueAllPackages +

            cargotrackingLibBoundaryApiAllPackages +

            klokwrkLibUomAllPackages +

            klokwrkLangGroovyAllPackages +

            thirdPartyDependencyAllPackages as String[]
        )
    // @formatter:on

    expect:
    rule.check(allKlokwrkClasses)
  }

  void "commandside app feature classes should only access classes from allowed dependencies"() {
    given:
    String[] thirdPartyDependencyAllPackages = [
        "java..",
        "org.codehaus.groovy..",
        "groovy..",

        "javax.measure..",
        "org.hamcrest",
        "tech.units.indriya.."
    ]

    String[] cargotrackingBookingCommandsideAppFeaturePackages = ["org.klokwrk.cargotracking.booking.app.commandside.feature.."]

    String[] cargotrackingBookingCommandAllPackages = ["org.klokwrk.cargotracking.domain.model.command.."]
    String[] cargotrackingBookingEventAllPackages = ["org.klokwrk.cargotracking.domain.model.event.."]
    String[] cargotrackingBookingBoundaryWebAllPackages = ["org.klokwrk.cargotracking.booking.lib.boundary.web.."]
    String[] cargotrackingBookingDomainValueAllPackages = ["org.klokwrk.cargotracking.domain.model.value.."]
    String[] cargotrackingBookingDomainAggregateAllPackages = ["org.klokwrk.cargotracking.domain.model.aggregate.."]
    String[] cargotrackingBookingStandaloneOutAdapterAllPackages = ["org.klokwrk.cargotracking.booking.lib.out.customer.."]

    String[] cargotrackingLibAxonCqrsCommandPackages = ["org.klokwrk.cargotracking.lib.axon.cqrs.command.."]
    String[] cargotrackingLibBoundaryApiAllPackages = ["org.klokwrk.cargotracking.lib.boundary.api.."]
    String[] cargotrackingLibWebAllPackages = ["org.klokwrk.cargotracking.lib.web.."]

    String[] klokwrkLibValidationPackages = ["org.klokwrk.lib.hi.validation.."]

    String[] klokwrkLangGroovyAllPackages = ["org.klokwrk.lib.xlang.groovy.."]

    // @formatter:off
    ArchRule rule = ArchRuleDefinition
        .classes().that()
            .resideInAnyPackage(cargotrackingBookingCommandsideAppFeaturePackages)
            .and()
            .haveNameNotMatching(/.*JsonFixtureBuilder$/)
        .should().onlyAccessClassesThat().resideInAnyPackage(
            cargotrackingBookingCommandsideAppFeaturePackages +

            cargotrackingBookingCommandAllPackages +
            cargotrackingBookingEventAllPackages +
            cargotrackingBookingBoundaryWebAllPackages +
            cargotrackingBookingDomainValueAllPackages +
            cargotrackingBookingDomainAggregateAllPackages +
            cargotrackingBookingStandaloneOutAdapterAllPackages +

            cargotrackingLibAxonCqrsCommandPackages +
            cargotrackingLibBoundaryApiAllPackages +
            cargotrackingLibWebAllPackages +

            klokwrkLibValidationPackages +

            klokwrkLangGroovyAllPackages +

            thirdPartyDependencyAllPackages as String[]
        )
    // @formatter:on

    expect:
    rule.check(allKlokwrkClasses)
  }

  void "commandside app infrastructure classes should only access classes from allowed dependencies"() {
    given:
    String[] thirdPartyDependencyAllPackages = [
        "java..",
        "org.codehaus.groovy..",
        "groovy..",

        "io.opentelemetry..",
        "org.axonframework.commandhandling",
        "org.axonframework.commandhandling.gateway",
        "org.axonframework.messaging",
        "org.axonframework.messaging.annotation",
        "org.axonframework.tracing..",

        "org.springframework.boot.context..",
        "org.springframework.beans..",
        "org.springframework.context..",
        "org.springframework.core.env..",
    ]

    String[] cargotrackingBookingCommandsideAppInfrastructurePackages = ["org.klokwrk.cargotracking.booking.app.commandside.infrastructure.."]

    String[] cargotrackingBookingServiceAllPackages = ["org.klokwrk.cargotracking.domain.model.service.."]
    String[] cargotrackingBookingStandaloneOutAdapterAllPackages = ["org.klokwrk.cargotracking.booking.lib.out.customer.."]

    String[] cargotrackingLibAxonCqrsCommandPackages = ["org.klokwrk.cargotracking.lib.axon.cqrs.command.."]
    String[] cargotrackingLibAxonLoggingAllPackages = ["org.klokwrk.cargotracking.lib.axon.logging.."]
    String[] cargotrackingLibWebAllPackages = ["org.klokwrk.cargotracking.lib.web.."]

    String[] klokwrkLibJacksonAllPackages = ["org.klokwrk.lib.hi.jackson.."]
    String[] klokwrkLibValidationSpringBootPackages = ["org.klokwrk.lib.hi.validation.springboot"]

    // @formatter:off
    ArchRule rule = ArchRuleDefinition
        .classes().that().resideInAnyPackage(cargotrackingBookingCommandsideAppInfrastructurePackages)
        .should().onlyAccessClassesThat().resideInAnyPackage(
            cargotrackingBookingCommandsideAppInfrastructurePackages +

            cargotrackingBookingServiceAllPackages +
            cargotrackingBookingStandaloneOutAdapterAllPackages +

            cargotrackingLibAxonCqrsCommandPackages +
            cargotrackingLibAxonLoggingAllPackages +
            cargotrackingLibWebAllPackages +

            klokwrkLibJacksonAllPackages +
            klokwrkLibValidationSpringBootPackages +

            thirdPartyDependencyAllPackages as String[]
        )
    // @formatter:on

    expect:
    rule.check(allKlokwrkClasses)
  }
}
