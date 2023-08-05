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
package org.klokwrk.cargotracker.booking.commandside.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.Architectures
import groovy.util.logging.Slf4j
import org.klokwrk.lib.lo.archunit.ArchUnitUtils
import org.klokwrk.lib.lo.archunit.HexagonalCqrsEsArchitecture
import spock.lang.Shared
import spock.lang.Specification

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage

@Slf4j
class BookingCommandSideAppArchitectureSpecification extends Specification {
  @Shared
  JavaClasses importedClasses

  void setupSpec() {
    importedClasses = ArchUnitUtils.importJavaClassesFromPackages(
        [
            "org.klokwrk.cargotracker.booking.commandside",
            "org.klokwrk.cargotracker.booking.domain.model.aggregate",
            "org.klokwrk.cargotracking.domain.model.service",
            "org.klokwrk.cargotracking.domain.model.value",
            "org.klokwrk.cargotracker.booking.domain.model.command",
            "org.klokwrk.cargotracking.domain.model.event",
            "org.klokwrk.cargotracker.booking.out.customer"
        ],
        ["org.klokwrk.cargotracker.booking.commandside.test"]
    )
  }

  /**
   * Just a sample to exercise ArchUnit API. Real test is in 'should be valid hexagonal commandside CQRS/ES architecture' feature method.
   */
  void "domain entities should only be accessed by application services"() {
    given:
    // @formatter:off
    ArchRule rule = ArchRuleDefinition
        .classes().that()
          .resideInAnyPackage("..domain.model.aggregate..")
        .should()
          .onlyBeAccessed().byAnyPackage("..application.service..", "..domain.model.aggregate..")
    // @formatter:on

    expect:
    rule.check(importedClasses)
  }

  /**
   * Just a sample to exercise ArchUnit API. Real test is in 'should be valid hexagonal commandside CQRS/ES architecture' feature method.
   */
  void "inbound adapters should access application services only via inbound ports"() {
    given:
    // @formatter:off
    ArchRule inboundAdapterDoesNotAccessApplicationServiceDirectlyRule = ArchRuleDefinition
        .noClasses().that()
            .resideInAPackage("..adapter.in..")
        .should()
            .dependOnClassesThat().resideInAPackage("..application.service..")
    // @formatter:on

    expect:
    inboundAdapterDoesNotAccessApplicationServiceDirectlyRule.check(importedClasses)
  }

  /**
   * Verify if application compiles to the onion architecture.
   * <p/>
   * Normally, we do not need this test. Only the test for commandside hexagonal CQRS/ES architecture is needed. Nevertheless, we are leaving it here as an example.
   */
  void "should be valid onion architecture"() {
    given:
    // @formatter:off
    ArchRule rule = Architectures
        .onionArchitecture()
        .domainModels(
            "..cargotracker.booking.domain.model.aggregate..", // domainEntities
            "..cargotracking.domain.model.service..",   // domainServices
            "..cargotracker.booking.domain.model.command..",   // domainCommands
            "..cargotracking.domain.model.event..",     // domainEvents
            "..cargotracking.domain.model.value.."      // domainValueObjects
        )
        .applicationServices(
            "..cargotracker.booking.commandside.feature.*.application.service..", // applicationServices
            "..cargotracker.booking.commandside.feature.*.application.port.in..", // portInbound
            "..cargotracker.booking.commandside.feature.*.application.port.out..", // portOutbound
            "..cargotracker.booking.out.customer.port.." // portOutbound
        )
        .adapter("in.web", "..cargotracker.booking.commandside.feature.*.adapter.in.web..") // adapterInbound
        .adapter("out.inline.remoting", "..cargotracker.booking.commandside.feature.*.adapter.out.remoting..") // adapterOutbound
        .adapter("out.standalone.customer", "..cargotracker.booking.out.customer.adapter..") // adapterOutbound

        .ignoreDependency( // dependency injection can access and instantiate domain services
            resideInAnyPackage("..cargotracker.booking.commandside.infrastructure.."),
            resideInAnyPackage("..cargotracking.domain.model.service..")
        )
        .ignoreDependency( // dependency injection can access and instantiate outbound adapters
            resideInAnyPackage("..cargotracker.booking.commandside.infrastructure.."),
            resideInAnyPackage("..cargotracker.booking.out.customer.adapter..")
        )

        .withOptionalLayers(true)
    // @formatter:on

    expect:
    rule.check(importedClasses)
  }

  /**
   * Verify if application compiles to the general CQRS/ES hexagonal architecture with optional layers allowed.
   * <p/>
   * Normally, we do not need this test. Only the test for commandside hexagonal CQRS/ES architecture is needed. Nevertheless, we are leaving it here as an example.
   */
  void "should be valid hexagonal CQRS/ES architecture"() {
    given:
    // @formatter:off
    ArchRule rule = HexagonalCqrsEsArchitecture
        .architecture()
        .domainValues("..cargotracking.domain.model.value..")
        .domainEvents("..cargotracking.domain.model.event..")
        .domainCommands("..cargotracker.booking.domain.model.command..")
        .domainServices("..cargotracking.domain.model.service..")
        .domainAggregates("..cargotracker.booking.domain.model.aggregate..")

        .applicationInboundPorts("..cargotracker.booking.commandside.feature.*.application.port.in..")
        .applicationOutboundPorts(
            "..cargotracker.booking.commandside.feature.*.application.port.out..",
            "..cargotracker.booking.out.customer.port.."
        )
        .applicationServices("..cargotracker.booking.commandside.feature.*.application.service..")

        .adapterInbound("in.web", "..cargotracker.booking.commandside.feature.*.adapter.in.web..")
        .adapterOutbound("out.inline.remoting", "..cargotracker.booking.commandside.feature.*.adapter.out.remoting..")
        .adapterOutbound("out.standalone.customer", "..cargotracker.booking.out.customer.adapter..")

        .ignoreDependency( // dependency injection can access and instantiate domain services
            resideInAnyPackage("..cargotracker.booking.commandside.infrastructure.."),
            resideInAnyPackage("..cargotracking.domain.model.service..")
        )
        .ignoreDependency( // dependency injection can access and instantiate outbound adapters
            resideInAnyPackage("..cargotracker.booking.commandside.infrastructure.."),
            resideInAnyPackage("..cargotracker.booking.out.customer.adapter..")
        )

        .withOptionalLayers(true)
    // @formatter:on

    log.debug "----- Architecture description"
    rule.description.eachLine { log.debug it }
    log.debug "------------------------------"

    expect:
    rule.check(importedClasses)
  }

  /**
   * Verify if application compiles to the commandside hexagonal CQRS/ES architecture.
   */
  void "should be valid hexagonal commandside CQRS/ES architecture"() {
    given:
    // @formatter:off
    ArchRule rule = HexagonalCqrsEsArchitecture
        .architecture(HexagonalCqrsEsArchitecture.ArchitectureSubType.COMMANDSIDE)
        .domainValues("..cargotracking.domain.model.value..")
        .domainEvents("..cargotracking.domain.model.event..")
        .domainCommands("..cargotracker.booking.domain.model.command..")
        .domainServices("..cargotracking.domain.model.service..")
        .domainAggregates("..cargotracker.booking.domain.model.aggregate..")

        .applicationInboundPorts("..cargotracker.booking.commandside.feature.*.application.port.in..")
        .applicationOutboundPorts(
            "..cargotracker.booking.commandside.feature.*.application.port.out..",
            "..cargotracker.booking.out.customer.port.."
        )
        .applicationServices("..cargotracker.booking.commandside.feature.*.application.service..")

        .adapterInbound("in.web", "..cargotracker.booking.commandside.feature.*.adapter.in.web..")
        .adapterOutbound("out.inline.remoting", "..cargotracker.booking.commandside.feature.*.adapter.out.remoting..")
        .adapterOutbound("out.standalone.customer", "..cargotracker.booking.out.customer.adapter..")

        .ignoreDependency( // dependency injection can access and instantiate domain services
            resideInAnyPackage(["..cargotracker.booking.commandside.infrastructure.."] as String[]),
            resideInAnyPackage(["..cargotracking.domain.model.service.."] as String[])
        )
        .ignoreDependency( // dependency injection can access and instantiate outbound adapters
            resideInAnyPackage("..cargotracker.booking.commandside.infrastructure.."),
            resideInAnyPackage("..cargotracker.booking.out.customer.adapter..")
        )

        .withOptionalLayers(false)
    // @formatter:on

    log.debug "----- Architecture description"
    rule.description.eachLine { log.debug it }
    log.debug "------------------------------"

    expect:
    rule.check(importedClasses)
  }
}
