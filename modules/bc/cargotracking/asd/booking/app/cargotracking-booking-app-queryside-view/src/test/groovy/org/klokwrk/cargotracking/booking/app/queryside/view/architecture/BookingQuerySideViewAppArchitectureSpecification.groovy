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
package org.klokwrk.cargotracking.booking.app.queryside.view.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import groovy.util.logging.Slf4j
import org.axonframework.queryhandling.QueryHandler
import org.klokwrk.lib.lo.archunit.ArchUnitUtils
import org.klokwrk.lib.lo.archunit.HexagonalCqrsEsArchitecture
import spock.lang.Shared
import spock.lang.Specification

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage

@Slf4j
class BookingQuerySideViewAppArchitectureSpecification extends Specification {
  @Shared
  JavaClasses importedClasses

  void setupSpec() {
    importedClasses = ArchUnitUtils.importJavaClassesFromPackages(
        [
            "org.klokwrk.cargotracking.booking.app.queryside.view",
            "org.klokwrk.cargotracking.domain.model.value",
            "org.klokwrk.cargotracking.booking.lib.out.customer"
        ],
        ["org.klokwrk.cargotracking.booking.app.queryside.projection.rdbms"]
    )
  }

  void "axon query handler adapters should not implement any interface from application outbound ports"() {
    given:
    // @formatter:off
    ArchRule inboundAdapterDoesNotAccessApplicationServiceDirectlyRule = ArchRuleDefinition
        .noClasses().that()
            .resideInAPackage("org.klokwrk.cargotracking.booking.app.queryside.view.feature.*.adapter.out..")
            .and().containAnyMethodsThat(
                new DescribedPredicate<JavaMethod>("annotated with org.axonframework.queryhandling.QueryHandler") {
                  @Override
                  boolean test(JavaMethod javaMethod) { return javaMethod.isAnnotatedWith(QueryHandler) }
                }
            )
        .should()
            .dependOnClassesThat().resideInAPackage("org.klokwrk.cargotracking.booking.app.queryside.view.feature.*.application.port.out..")
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
        .domainValues("..cargotracking.domain.model.value..")

        .applicationInboundPorts("..cargotracking.booking.app.queryside.view.feature.*.application.port.in..")
        .applicationOutboundPorts(
            "..cargotracking.booking.app.queryside.view.feature.*.application.port.out..",
            "..cargotracking.booking.lib.out.customer.port.."
        )
        .applicationServices("..cargotracking.booking.app.queryside.view.feature.*.application.service..")

        .adapterInbound("in.web", "..cargotracking.booking.app.queryside.view.feature.*.adapter.in.web..")
        .adapterOutbound(
            "out.persistence",
            [
                "..cargotracking.booking.app.queryside.view.feature.*.adapter.out.persistence..",
                "..cargotracking.booking.lib.queryside.model.rdbms.jpa.."
            ] as String[]
        )
        .adapterOutbound("out.standalone.customer", "..cargotracking.booking.lib.out.customer.adapter..")

        .ignoreDependency( // dependency injection can access and instantiate outbound adapters
            resideInAnyPackage("..cargotracking.booking.app.queryside.view.infrastructure.."),
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
