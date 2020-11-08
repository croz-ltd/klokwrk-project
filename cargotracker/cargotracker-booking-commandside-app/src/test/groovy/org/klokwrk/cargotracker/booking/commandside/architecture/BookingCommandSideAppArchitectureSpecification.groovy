/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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
import org.klokwrk.lib.archunit.ArchUnitUtils
import org.klokwrk.lib.archunit.HexagonalCqrsEsArchitecture
import spock.lang.Shared
import spock.lang.Specification

@Slf4j
class BookingCommandSideAppArchitectureSpecification extends Specification {
  @Shared
  JavaClasses importedClasses

  void setupSpec() {
    importedClasses = ArchUnitUtils.importJavaClassesFromPackages(
        ["org.klokwrk.cargotracker.booking.commandside", "org.klokwrk.cargotracker.booking.domain.model", "org.klokwrk.cargotracker.booking.axon.api.feature"],
        ["org.klokwrk.cargotracker.booking.commandside.test"]
    )
  }

  /**
   * Just a sample to exercise ArchUnit API. Real test is in 'should be valid hexagonal commandside CQRS/ES architecture' feature method.
   */
  void "aggregates should only be accessed by application services"() {
    given:
    // @formatter:off
    ArchRule rule = ArchRuleDefinition
        .classes().that()
          .resideInAnyPackage("..domain.aggregate..")
        .should()
          .onlyBeAccessed().byAnyPackage("..application.service..", "..domain.aggregate..")
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
            "..cargotracker.booking.commandside.domain.aggregate..", // domainAggregates
            "..cargotracker.booking.axon.api.feature.*.command..",   // domainCommands
            "..cargotracker.booking.axon.api.feature.*.event..",     // domainEvents
            "..cargotracker.booking.domain.model.."                  // domainModels
        )
        .applicationServices(
            "..cargotracker.booking.commandside.feature.*.application.service..", // applicationServices
            "..cargotracker.booking.commandside.feature.*.application.port.in..", // applicationInboundPorts
            "..cargotracker.booking.commandside.feature.*.application.port.out.." // applicationOutboundPorts
        )
        .adapter("in.web", "..cargotracker.booking.commandside.feature.*.adapter.in.web..") // adapterInbound
        .adapter("out.remoting", "..cargotracker.booking.commandside.feature.*.adapter.out.remoting..") // adapterOutbound
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
        .domainModels("..cargotracker.booking.domain.model..")
        .domainEvents("..cargotracker.booking.axon.api.feature.*.event..")
        .domainCommands("..cargotracker.booking.axon.api.feature.*.command..")
        .domainAggregates("..cargotracker.booking.commandside.domain.aggregate..")

        .applicationInboundPorts("..cargotracker.booking.commandside.feature.*.application.port.in..")
        .applicationOutboundPorts("..cargotracker.booking.commandside.feature.*.application.port.out..")
        .applicationServices("..cargotracker.booking.commandside.feature.*.application.service..")

        .adapterInbound("in.web", "..cargotracker.booking.commandside.feature.*.adapter.in.web..")
        .adapterOutbound("out.remoting", "..cargotracker.booking.commandside.feature.*.adapter.out.remoting..")

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
        .domainModels("..cargotracker.booking.domain.model..")
        .domainEvents("..cargotracker.booking.axon.api.feature.*.event..")
        .domainCommands("..cargotracker.booking.axon.api.feature.*.command..")
        .domainAggregates("..cargotracker.booking.commandside.domain.aggregate..")

        .applicationInboundPorts("..cargotracker.booking.commandside.feature.*.application.port.in..")
        .applicationOutboundPorts("..cargotracker.booking.commandside.feature.*.application.port.out..")
        .applicationServices("..cargotracker.booking.commandside.feature.*.application.service..")

        .adapterInbound("in.web", "..cargotracker.booking.commandside.feature.*.adapter.in.web..")
        .adapterOutbound("out.remoting", "..cargotracker.booking.commandside.feature.*.adapter.out.remoting..")

        .withOptionalLayers(false)
    // @formatter:on

    log.debug "----- Architecture description"
    rule.description.eachLine { log.debug it }
    log.debug "------------------------------"

    expect:
    rule.check(importedClasses)
  }
}
