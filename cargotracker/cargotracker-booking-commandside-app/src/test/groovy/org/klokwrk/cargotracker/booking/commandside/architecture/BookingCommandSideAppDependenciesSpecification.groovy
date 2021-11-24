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
package org.klokwrk.cargotracker.booking.commandside.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.dependencies.SliceRule
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.klokwrk.lib.archunit.ArchUnitUtils
import spock.lang.Shared
import spock.lang.Specification

class BookingCommandSideAppDependenciesSpecification extends Specification {
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

  void "commandside app spring boot application class should only access classes from allowed dependencies"() {
    given:
    String[] thirdPartyDependencyAllPackages = [
        "java..",
        "org.codehaus.groovy..",
        "groovy..",

        "org.springframework.boot"
    ]

    String[] cargotrackerBookingCommandsideAppSpringBootApplicationPackages = ["org.klokwrk.cargotracker.booking.commandside"]

    // @formatter:off
    ArchRule rule = ArchRuleDefinition
        .classes().that().resideInAnyPackage(cargotrackerBookingCommandsideAppSpringBootApplicationPackages)
        .should().onlyAccessClassesThat().resideInAnyPackage(
            cargotrackerBookingCommandsideAppSpringBootApplicationPackages +

            thirdPartyDependencyAllPackages as String[]
        )
    // @formatter:on

    expect:
    rule.check(allKlokwrkClasses)
  }

  void "commandside app domain aggregate classes should only access classes from allowed dependencies"() {
    given:
    String[] thirdPartyDependencyAllPackages = [
        "java..",
        "org.codehaus.groovy..",
        "groovy..",

        "org.axonframework.modelling.command.."
    ]

    String[] cargotrackerBookingCommandsideAppDomainAggregatePackages = ["org.klokwrk.cargotracker.booking.commandside.domain.aggregate.."]

    String[] cargotrackerBookingCommandAllPackages = ["org.klokwrk.cargotracker.booking.domain.model.command.."]
    String[] cargotrackerBookingEventAllPackages = ["org.klokwrk.cargotracker.booking.domain.model.event.."]
    String[] cargotrackerBookingDomainModelValueAllPackages = ["org.klokwrk.cargotracker.booking.domain.model.value.."]

    String[] cargotrackerLibBoundaryApiAllPackages = ["org.klokwrk.cargotracker.lib.boundary.api.."]

    // @formatter:off
    ArchRule rule = ArchRuleDefinition
        .classes().that().resideInAnyPackage(cargotrackerBookingCommandsideAppDomainAggregatePackages)
        .should().onlyAccessClassesThat().resideInAnyPackage(
            cargotrackerBookingCommandsideAppDomainAggregatePackages +

            cargotrackerBookingCommandAllPackages +
            cargotrackerBookingEventAllPackages +
            cargotrackerBookingDomainModelValueAllPackages +

            cargotrackerLibBoundaryApiAllPackages +

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

        "org.hamcrest"
    ]

    String[] cargotrackerBookingCommandsideAppFeaturePackages = ["org.klokwrk.cargotracker.booking.commandside.feature.."]

    String[] cargotrackerBookingCommandAllPackages = ["org.klokwrk.cargotracker.booking.domain.model.command.."]
    String[] cargotrackerBookingEventAllPackages = ["org.klokwrk.cargotracker.booking.domain.model.event.."]
    String[] cargotrackerBookingBoundaryWebAllPackages = ["org.klokwrk.cargotracker.booking.boundary.web.."]
    String[] cargotrackerBookingDomainModelValueAllPackages = ["org.klokwrk.cargotracker.booking.domain.model.value.."]
    String[] cargotrackerBookingCommandsideDomainAggregatePackages = ["org.klokwrk.cargotracker.booking.commandside.domain.aggregate.."]

    String[] cargotrackerLibAxonCqrsCommandPackages = ["org.klokwrk.cargotracker.lib.axon.cqrs.command.."]
    String[] cargotrackerLibBoundaryApiAllPackages = ["org.klokwrk.cargotracker.lib.boundary.api.."]
    String[] cargotrackerLibWebAllPackages = ["org.klokwrk.cargotracker.lib.web.."]

    String[] klokwrkLibValidationPackages = ["org.klokwrk.lib.validation.."]

    String[] klokwrkLangGroovyAllPackages = ["org.klokwrk.lang.groovy.."]

    // @formatter:off
    ArchRule rule = ArchRuleDefinition
        .classes().that().resideInAnyPackage(cargotrackerBookingCommandsideAppFeaturePackages)
        .should().onlyAccessClassesThat().resideInAnyPackage(
            cargotrackerBookingCommandsideAppFeaturePackages +

            cargotrackerBookingCommandAllPackages +
            cargotrackerBookingEventAllPackages +
            cargotrackerBookingBoundaryWebAllPackages +
            cargotrackerBookingDomainModelValueAllPackages +
            cargotrackerBookingCommandsideDomainAggregatePackages +

            cargotrackerLibAxonCqrsCommandPackages +
            cargotrackerLibBoundaryApiAllPackages +
            cargotrackerLibWebAllPackages +

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

        "org.axonframework.commandhandling",
        "org.axonframework.commandhandling.gateway",
        "org.axonframework.messaging",
        "org.axonframework.messaging.annotation",

        "org.axonframework.extensions.tracing",

        "org.springframework.boot.context..",
        "org.springframework.beans..",
        "org.springframework.context.."
    ]

    String[] cargotrackerBookingCommandsideAppInfrastructurePackages = ["org.klokwrk.cargotracker.booking.commandside.infrastructure.."]

    String[] cargotrackerLibAxonCqrsCommandPackages = ["org.klokwrk.cargotracker.lib.axon.cqrs.command.."]
    String[] cargotrackerLibAxonLoggingAllPackages = ["org.klokwrk.cargotracker.lib.axon.logging.."]
    String[] cargotrackerLibWebAllPackages = ["org.klokwrk.cargotracker.lib.web.."]

    String[] klokwrkLibJacksonAllPackages = ["org.klokwrk.lib.jackson.."]
    String[] klokwrkLibValidationSpringBootPackages = ["org.klokwrk.lib.validation.springboot"]

    // @formatter:off
    ArchRule rule = ArchRuleDefinition
        .classes().that().resideInAnyPackage(cargotrackerBookingCommandsideAppInfrastructurePackages)
        .should().onlyAccessClassesThat().resideInAnyPackage(
            cargotrackerBookingCommandsideAppInfrastructurePackages +

            cargotrackerLibAxonCqrsCommandPackages +
            cargotrackerLibAxonLoggingAllPackages +
            cargotrackerLibWebAllPackages +

            klokwrkLibJacksonAllPackages +
            klokwrkLibValidationSpringBootPackages +

            thirdPartyDependencyAllPackages as String[]
        )
    // @formatter:on

    expect:
    rule.check(allKlokwrkClasses)
  }
}
