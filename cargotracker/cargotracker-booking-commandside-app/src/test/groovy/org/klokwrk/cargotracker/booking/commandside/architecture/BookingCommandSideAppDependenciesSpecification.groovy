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

  void "commandside app should only access classes from allowed dependencies"() {
    given:
    String[] thirdPartyDependencyAllPackages = [
        "java..",
        "org.codehaus.groovy..",
        "groovy..",

        "javax.validation..",
        "javax.servlet",

        "org.axonframework.modelling.command..",
        "org.hamcrest",
        "org.springframework.."
    ]

    String[] cargotrackerBookingCommandsideAppAllPackages = ["org.klokwrk.cargotracker.booking.commandside.."]

    String[] cargotrackerBookingAxonApiAllPackages = ["org.klokwrk.cargotracker.booking.axon.api.."]
    String[] cargotrackerBookingBoundaryWebAllPackages = ["org.klokwrk.cargotracker.booking.boundary.web.."]
    String[] cargotrackerBookingDomainModelAllPackages = ["org.klokwrk.cargotracker.booking.domain.model.."]

    String[] cargotrackerLibAxonCqrsAllPackages = ["org.klokwrk.cargotracker.lib.axon.cqrs.."]
    String[] cargotrackerLibAxonLoggingAllPackages = ["org.klokwrk.cargotracker.lib.axon.logging.."]
    String[] cargotrackerLibBoundaryApiAllPackages = ["org.klokwrk.cargotracker.lib.boundary.api.."]
    String[] cargotrackerLibWebAllPackages = ["org.klokwrk.cargotracker.lib.web.."]

    String[] klokwrkLibJacksonAllPackages = ["org.klokwrk.lib.jackson.."]
    String[] klokwrkLangGroovyAllPackages = ["org.klokwrk.lang.groovy.."]

    // @formatter:off
    ArchRule rule = ArchRuleDefinition
        .classes().that().resideInAnyPackage(cargotrackerBookingCommandsideAppAllPackages)
        .should().onlyAccessClassesThat().resideInAnyPackage(
            cargotrackerBookingCommandsideAppAllPackages +

            cargotrackerBookingAxonApiAllPackages +
            cargotrackerBookingBoundaryWebAllPackages +
            cargotrackerBookingDomainModelAllPackages +

            cargotrackerLibAxonCqrsAllPackages +
            cargotrackerLibAxonLoggingAllPackages +
            cargotrackerLibBoundaryApiAllPackages +
            cargotrackerLibWebAllPackages +

            klokwrkLibJacksonAllPackages +

            klokwrkLangGroovyAllPackages +

            thirdPartyDependencyAllPackages as String[]
        )
    // @formatter:on

    expect:
    rule.check(allKlokwrkClasses)
  }
}
