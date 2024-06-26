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
package org.klokwrk.cargotracking.booking.app.commandside.architecture

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
            "org.klokwrk.cargotracking.booking.app.commandside",
            "org.klokwrk.cargotracking.domain.model.aggregate",
            "org.klokwrk.cargotracking.domain.model.service",
            "org.klokwrk.cargotracking.domain.model.value",
            "org.klokwrk.cargotracking.domain.model.command",
            "org.klokwrk.cargotracking.domain.model.event",
            "org.klokwrk.cargotracking.booking.lib.out.customer"
        ],
        ["org.klokwrk.cargotracking.booking.app.commandside.test"]
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
            "..cargotracking.domain.model.aggregate..", // domainEntities
            "..cargotracking.domain.model.service..",   // domainServices
            "..cargotracking.domain.model.command..",   // domainCommands
            "..cargotracking.domain.model.event..",     // domainEvents
            "..cargotracking.domain.model.value.."      // domainValueObjects
        )
        .applicationServices(
            "..cargotracking.booking.app.commandside.feature.*.application.service..", // applicationServices
            "..cargotracking.booking.app.commandside.feature.*.application.port.in..", // portInbound
            "..cargotracking.booking.app.commandside.feature.*.application.port.out..", // portOutbound
            "..cargotracking.booking.lib.out.customer.port.." // portOutbound
        )
        .adapter("in.web", "..cargotracking.booking.app.commandside.feature.*.adapter.in.web..") // adapterInbound
        .adapter("out.remoting", "..cargotracking.booking.app.commandside.feature.*.adapter.out.remoting..") // adapterOutbound
        .adapter("out.customer", "..cargotracking.booking.lib.out.customer.adapter..") // adapterOutbound

        .ignoreDependency( // dependency injection can access and instantiate domain services
            resideInAnyPackage("..cargotracking.booking.app.commandside.infrastructure.."),
            resideInAnyPackage("..cargotracking.domain.model.service..")
        )
        .ignoreDependency( // dependency injection can access and instantiate outbound adapters
            resideInAnyPackage("..cargotracking.booking.app.commandside.infrastructure.."),
            resideInAnyPackage("..cargotracking.booking.lib.out.customer.adapter..")
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
        .domainCommands("..cargotracking.domain.model.command..")
        .domainServices("..cargotracking.domain.model.service..")
        .domainAggregates("..cargotracking.domain.model.aggregate..")

        .applicationInboundPorts("..cargotracking.booking.app.commandside.feature.*.application.port.in..")
        .applicationOutboundPorts(
            "..cargotracking.booking.app.commandside.feature.*.application.port.out..",
            "..cargotracking.booking.lib.out.customer.port.."
        )
        .applicationServices("..cargotracking.booking.app.commandside.feature.*.application.service..")

        .adapterInbound("in.web", "..cargotracking.booking.app.commandside.feature.*.adapter.in.web..")
        .adapterOutbound("out.remoting", "..cargotracking.booking.app.commandside.feature.*.adapter.out.remoting..")
        .adapterOutbound("out.customer", "..cargotracking.booking.lib.out.customer.adapter..")

        .ignoreDependency( // dependency injection can access and instantiate domain services
            resideInAnyPackage("..cargotracking.booking.app.commandside.infrastructure.."),
            resideInAnyPackage("..cargotracking.domain.model.service..")
        )
        .ignoreDependency( // dependency injection can access and instantiate outbound adapters
            resideInAnyPackage("..cargotracking.booking.app.commandside.infrastructure.."),
            resideInAnyPackage("..cargotracking.booking.lib.out.customer.adapter..")
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
        .domainCommands("..cargotracking.domain.model.command..")
        .domainServices("..cargotracking.domain.model.service..")
        .domainAggregates("..cargotracking.domain.model.aggregate..")

        .applicationInboundPorts("..cargotracking.booking.app.commandside.feature.*.application.port.in..")
        .applicationOutboundPorts(
            "..cargotracking.booking.app.commandside.feature.*.application.port.out..",
            "..cargotracking.booking.lib.out.customer.port.."
        )
        .applicationServices("..cargotracking.booking.app.commandside.feature.*.application.service..")

        .adapterInbound("in.web", "..cargotracking.booking.app.commandside.feature.*.adapter.in.web..")
        .adapterOutbound("out.remoting", "..cargotracking.booking.app.commandside.feature.*.adapter.out.remoting..")
        .adapterOutbound("out.customer", "..cargotracking.booking.lib.out.customer.adapter..")

        .ignoreDependency( // dependency injection can access and instantiate domain services
            resideInAnyPackage(["..cargotracking.booking.app.commandside.infrastructure.."] as String[]),
            resideInAnyPackage(["..cargotracking.domain.model.service.."] as String[])
        )
        .ignoreDependency( // dependency injection can access and instantiate outbound adapters
            resideInAnyPackage("..cargotracking.booking.app.commandside.infrastructure.."),
            resideInAnyPackage("..cargotracking.booking.lib.out.customer.adapter..")
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
