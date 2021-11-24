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
package org.klokwrk.cargotracker.booking.queryside.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import groovy.util.logging.Slf4j
import org.axonframework.queryhandling.QueryHandler
import org.klokwrk.lib.archunit.ArchUnitUtils
import org.klokwrk.lib.archunit.HexagonalCqrsEsArchitecture
import spock.lang.Shared
import spock.lang.Specification

@Slf4j
class BookingQuerySideAppArchitectureSpecification extends Specification {
  @Shared
  JavaClasses importedClasses

  void setupSpec() {
    importedClasses = ArchUnitUtils.importJavaClassesFromPackages(
        ["org.klokwrk.cargotracker.booking.queryside", "org.klokwrk.cargotracker.booking.domain.model.value"],
        ["org.klokwrk.cargotracker.booking.queryside.rdbms.projection"]
    )
  }

  void "axon query handler adapters should not implement any interface from application outbound ports"() {
    given:
    // @formatter:off
    ArchRule inboundAdapterDoesNotAccessApplicationServiceDirectlyRule = ArchRuleDefinition
        .noClasses().that()
            .resideInAPackage("org.klokwrk.cargotracker.booking.queryside.feature.*.adapter.out..")
            .and().containAnyMethodsThat(
                new DescribedPredicate<JavaMethod>("annotated with org.axonframework.queryhandling.QueryHandler") {
                  @Override
                  boolean apply(JavaMethod javaMethod) { return javaMethod.isAnnotatedWith(QueryHandler) }
                }
            )
        .should()
            .dependOnClassesThat().resideInAPackage("org.klokwrk.cargotracker.booking.queryside.feature.*.application.port.out..")
    // @formatter:on

    expect:
    inboundAdapterDoesNotAccessApplicationServiceDirectlyRule.check(importedClasses)
  }

  /**
   * Verify if application compiles to the queryside hexagonal CQRS/ES architecture.
   */
  void "should be valid hexagonal queryside CQRS/ES architecture"() {
    given:
    // @formatter:off
    ArchRule rule = HexagonalCqrsEsArchitecture
        .architecture(HexagonalCqrsEsArchitecture.ArchitectureSubType.QUERYSIDE)
        .domainModelValues("..cargotracker.booking.domain.model.value..")

        .applicationInboundPorts("..cargotracker.booking.queryside.feature.*.application.port.in..")
        .applicationOutboundPorts("..cargotracker.booking.queryside.feature.*.application.port.out..")
        .applicationServices("..cargotracker.booking.queryside.feature.*.application.service..")

        .adapterInbound("in.web", "..cargotracker.booking.queryside.feature.*.adapter.in.web..")
        .adapterOutbound("out.persistence", "..cargotracker.booking.queryside.feature.*.adapter.out.persistence..")

        .withOptionalLayers(false)
    // @formatter:on

    log.debug "----- Architecture description"
    rule.description.eachLine { log.debug it }
    log.debug "------------------------------"

    expect:
    rule.check(importedClasses)
  }
}
