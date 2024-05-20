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

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import groovy.util.logging.Slf4j
import org.klokwrk.lib.lo.archunit.ArchUnitUtils
import org.klokwrk.lib.lo.archunit.HexagonalCqrsEsArchitecture
import spock.lang.Shared
import spock.lang.Specification

@Slf4j
class BookingQuerySideProjectionRdbmsAppArchitectureSpecification extends Specification {
  @Shared
  JavaClasses importedClasses

  void setupSpec() {
    importedClasses = ArchUnitUtils.importJavaClassesFromPackages(
        ["org.klokwrk.cargotracking.booking.app.queryside.projection.rdbms", "org.klokwrk.cargotracking.domain.model.value", "org.klokwrk.cargotracking.domain.model.event"]
    )
  }

  void "projection adapters should not use command classes"() {
    given:
    // @formatter:off
    ArchRule inboundAdapterDoesNotAccessApplicationServiceDirectlyRule = ArchRuleDefinition
        .noClasses().that()
            .resideInAPackage("org.klokwrk.cargotracking.booking.app.queryside.projection.rdbms..")
        .should()
            .dependOnClassesThat().resideInAPackage("org.klokwrk.cargotracking.domain.model.command..")
    // @formatter:on

    expect:
    inboundAdapterDoesNotAccessApplicationServiceDirectlyRule.check(importedClasses)
  }

  /**
   * Verify if application compiles to the projection hexagonal CQRS/ES architecture.
   */
  void "should be valid hexagonal projection CQRS/ES architecture"() {
    given:
    // @formatter:off
    ArchRule rule = HexagonalCqrsEsArchitecture
        .architecture(HexagonalCqrsEsArchitecture.ArchitectureSubType.PROJECTION)
        .domainValues("..cargotracking.domain.model.value..")
        .domainEvents("..cargotracking.domain.model.event..")

        .adapterProjection(
            "out.persistence",
            [
                "..cargotracking.booking.app.queryside.projection.rdbms.feature.*.adapter.out..",
                "..cargotracking.booking.lib.queryside.model.rdbms.jpa.."
            ] as String[]
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
