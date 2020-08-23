package org.klokwrk.cargotracker.booking.queryside.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import groovy.util.logging.Slf4j
import org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler.QueryHandlerTrait
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
        ["org.klokwrk.cargotracker.booking.queryside", "org.klokwrk.cargotracker.booking.domain.model"],
        ["org.klokwrk.cargotracker.booking.queryside.rdbms.projection"]
    )
  }

  void "axon query handler adapters should not implement any interface from application outbound ports"() {
    given:
    // @formatter:off
    ArchRule inboundAdapterDoesNotAccessApplicationServiceDirectlyRule = ArchRuleDefinition
        .noClasses().that()
            .resideInAPackage("org.klokwrk.cargotracker.booking.queryside.feature.*.adapter.out..")
            .and().implement(QueryHandlerTrait)
        .should()
            .dependOnClassesThat().resideInAPackage("org.klokwrk.cargotracker.booking.queryside.feature.*.application.port.out..")
    // @formatter:on

    expect:
    inboundAdapterDoesNotAccessApplicationServiceDirectlyRule.check(importedClasses)
  }

  /**
   * Verify if application compiles to the queryside hexagonal CQRS/ES architecture.
   */
  void "should be valid hexagonal commandside CQRS/ES architecture"() {
    given:
    // @formatter:off
    ArchRule rule = HexagonalCqrsEsArchitecture
        .architecture(HexagonalCqrsEsArchitecture.ArchitectureSubType.QUERYSIDE)
        .domainModels("..cargotracker.booking.domain.model..")

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
