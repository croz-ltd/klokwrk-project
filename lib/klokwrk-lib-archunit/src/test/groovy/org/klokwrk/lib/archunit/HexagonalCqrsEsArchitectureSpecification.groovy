/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.archunit

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.Dependency
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.EvaluationResult
import groovy.util.logging.Slf4j
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.adapter.projection.AdapterProjectionClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.model.event.DomainEventClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.model.value.DomainModelValueClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.in.AdapterInViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.out.AdapterOutViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.projection.AdapterProjectionViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.port.in.ApplicationPortInViolationInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.port.out.ApplicationPortOutViolationInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.service.ApplicationServiceViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.aggregate.DomainAggregateViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.command.DomainCommandViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.event.DomainEventViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.value.DomainModelValueViolationClass
import spock.lang.Shared
import spock.lang.Specification

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackages
import static org.klokwrk.lib.archunit.HexagonalCqrsEsArchitecture.ADAPTER_INBOUND_LAYER
import static org.klokwrk.lib.archunit.HexagonalCqrsEsArchitecture.ADAPTER_OUTBOUND_LAYER
import static org.klokwrk.lib.archunit.HexagonalCqrsEsArchitecture.ADAPTER_PROJECTION_LAYER
import static org.klokwrk.lib.archunit.HexagonalCqrsEsArchitecture.APPLICATION_INBOUND_PORT_LAYER
import static org.klokwrk.lib.archunit.HexagonalCqrsEsArchitecture.APPLICATION_OUTBOUND_PORT_LAYER
import static org.klokwrk.lib.archunit.HexagonalCqrsEsArchitecture.APPLICATION_SERVICE_LAYER
import static org.klokwrk.lib.archunit.HexagonalCqrsEsArchitecture.DOMAIN_AGGREGATE_LAYER
import static org.klokwrk.lib.archunit.HexagonalCqrsEsArchitecture.DOMAIN_COMMAND_LAYER
import static org.klokwrk.lib.archunit.HexagonalCqrsEsArchitecture.DOMAIN_EVENT_LAYER
import static org.klokwrk.lib.archunit.HexagonalCqrsEsArchitecture.DOMAIN_VALUE_LAYER

@Slf4j
class HexagonalCqrsEsArchitectureSpecification extends Specification {

  @Shared
  JavaClasses importedValidClasses

  @Shared
  JavaClasses importedViolationClasses

  @Shared
  List<Class<?>> generalArchitectureAllViolatingSources = [
      DomainModelValueViolationClass, DomainEventViolationClass, DomainCommandViolationClass, DomainAggregateViolationClass, ApplicationPortInViolationInterface, ApplicationPortOutViolationInterface,
      ApplicationServiceViolationClass, AdapterInViolationClass, AdapterOutViolationClass, AdapterProjectionViolationClass
  ]

  @Shared
  List<Class<?>> projectionArchitectureAllViolatingSources = [DomainModelValueViolationClass, DomainEventViolationClass, AdapterProjectionViolationClass]

  void setupSpec() {
    importedValidClasses = ArchUnitUtils.importJavaClassesFromPackages(["org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid"], [], [])
    importedViolationClasses = ArchUnitUtils.importJavaClassesFromPackages(["org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation"], [], [])
  }

  void "should not take into account ignored dependencies"() {
    given:
    HexagonalCqrsEsArchitecture hexagonalCqrsEsArchitectureRule = HexagonalCqrsEsArchitecture.architecture(HexagonalCqrsEsArchitecture.ArchitectureSubType.COMMANDSIDE)
    hexagonalCqrsEsArchitectureRule
        .domainValues("..domain.model.value..")
        .domainEvents("..domain.model.event..")
        .domainCommands("..domain.model.command..")
        .domainAggregates("..domain.model.aggregate..")

        .applicationInboundPorts("..application.port.in..")
        .applicationOutboundPorts("..application.port.out..")
        .applicationServices("..application.service..")

        .adapterInbound("in", "..adapter.in..")
        .adapterOutbound("out", "..adapter.out..")

        .withOptionalLayers(false)

    hexagonalCqrsEsArchitectureRule
        .ignoreDependency(
            resideInAnyPackage(["..adapter.projection.."] as String[]), resideInAnyPackage(["..domain.model.command..", "domain.model.aggregate", "..application..", "..adapter.."] as String[])
        )
        .ignoreDependency(AdapterProjectionClass, DomainModelValueClass)
        .ignoreDependency(AdapterProjectionClass.name, DomainEventClass.name)

    when:
    EvaluationResult evaluationResult = hexagonalCqrsEsArchitectureRule.evaluate(importedValidClasses)

    then:
    //noinspection GroovyPointlessBoolean
    evaluationResult.hasViolation() == false
  }

  void "should be able to override description"() {
    given:
    HexagonalCqrsEsArchitecture emptyHexagonalCqrsEsArchitecture = HexagonalCqrsEsArchitecture.architecture().as("Overridden description.")

    HexagonalCqrsEsArchitecture configuredHexagonalCqrsEsArchitecture = fetchArchitectureRule() as HexagonalCqrsEsArchitecture
    configuredHexagonalCqrsEsArchitecture = configuredHexagonalCqrsEsArchitecture.as("Another overridden description")

    expect:
    emptyHexagonalCqrsEsArchitecture.description == "Overridden description."
    configuredHexagonalCqrsEsArchitecture.description == "Another overridden description"
  }

  void "should fail overriding description with invalid argument"() {
    when:
    HexagonalCqrsEsArchitecture.architecture().as(overridenDescription)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.startsWith("Require violation detected - matcher does not match - [item: newDescription, expected: not(blankOrNullString())")

    where:
    overridenDescription | _
    null                 | _
    ""                   | _
    "  "                 | _
  }

  void "should be able to specify architectural reason"() {
    given:
    HexagonalCqrsEsArchitecture hexagonalCqrsEsArchitecture = HexagonalCqrsEsArchitecture.architecture().because("We like it!")

    expect:
    hexagonalCqrsEsArchitecture.description.endsWith("We like it!")
  }

  private ArchRule fetchArchitectureRule(HexagonalCqrsEsArchitecture.ArchitectureSubType architectureSubType = HexagonalCqrsEsArchitecture.ArchitectureSubType.NONE) {
    HexagonalCqrsEsArchitecture hexagonalCqrsEsArchitectureRule = HexagonalCqrsEsArchitecture.architecture(architectureSubType)

    switch (architectureSubType) {
      case HexagonalCqrsEsArchitecture.ArchitectureSubType.COMMANDSIDE:
        hexagonalCqrsEsArchitectureRule
            .domainValues("..domain.model.value..")
            .domainEvents("..domain.model.event..")
            .domainCommands("..domain.model.command..")
            .domainAggregates("..domain.model.aggregate..")

            .applicationInboundPorts("..application.port.in..")
            .applicationOutboundPorts("..application.port.out..")
            .applicationServices("..application.service..")

            .adapterInbound("in", "..adapter.in..")
            .adapterOutbound("out", "..adapter.out..")

            .withOptionalLayers(false)
            .ignoreDependency(resideInAnyPackage(["..adapter.projection.."] as String[]), DescribedPredicate.alwaysTrue())
        break

      case HexagonalCqrsEsArchitecture.ArchitectureSubType.PROJECTION:
        hexagonalCqrsEsArchitectureRule
            .domainValues("..domain.model.value..")
            .domainEvents("..domain.model.event..")

            .adapterProjection("projection", "..adapter.projection..")

            .withOptionalLayers(false)
            .ignoreDependency(resideOutsideOfPackages(["..domain.model.value..", "..domain.model.event..", "..adapter.projection.."] as String[]), DescribedPredicate.alwaysTrue())
        break

      case HexagonalCqrsEsArchitecture.ArchitectureSubType.QUERYSIDE:
        hexagonalCqrsEsArchitectureRule
            .domainValues("..domain.model.value..")

            .applicationInboundPorts("..application.port.in..")
            .applicationOutboundPorts("..application.port.out..")
            .applicationServices("..application.service..")

            .adapterInbound("in", "..adapter.in..")
            .adapterOutbound("out", "..adapter.out..")

            .withOptionalLayers(false)
            .ignoreDependency(
                resideInAnyPackage(["..domain.model.event..", "..domain.model.command..", "..domain.model.aggregate..", "..adapter.projection.."] as String[]), DescribedPredicate.alwaysTrue()
            )
        break

      default:
        hexagonalCqrsEsArchitectureRule
            .domainValues("..domain.model.value..")
            .domainEvents("..domain.model.event..")
            .domainCommands("..domain.model.command..")
            .domainAggregates("..domain.model.aggregate..")

            .applicationInboundPorts("..application.port.in..")
            .applicationOutboundPorts("..application.port.out..")
            .applicationServices("..application.service..")

            .adapterInbound("in", "..adapter.in..")
            .adapterOutbound("out", "..adapter.out..")
            .adapterProjection("projection", "..adapter.projection..")

            .withOptionalLayers(true)
        break
    }

    log.debug "----- Architecture description"
    hexagonalCqrsEsArchitectureRule.description.eachLine { String line -> log.debug line }
    log.debug "------------------------------"

    return hexagonalCqrsEsArchitectureRule
  }

  void "should be valid general hexagonal CQRS/ES architecture"() {
    given:
    ArchRule rule = fetchArchitectureRule()

    expect:
    rule.check(importedValidClasses)
  }

  void "should not allow forbidden access with general hexagonal CQRS/ES architecture - [#description]"() {
    given:
    log.debug "----- description: ${ description }"
    log.debug "----- violatingSources: ${ violatingSources*.simpleName }"
    log.debug "----- disallowedTarget: ${ disallowedTarget.simpleName }"

    ArchRule rule = fetchArchitectureRule()

    when:
    EvaluationResult evaluationResultForDisallowedTargetOnly = evaluateAndDiscardIrrelevantViolations(rule, importedViolationClasses, disallowedTarget)

    then:
    assertIfDependencyExistsBetweenViolatingSourceAndDisallowedTarget(violatingSources as List<Class<?>>, disallowedTarget)
    doesFailureReportContainsInvalidDependency(evaluationResultForDisallowedTargetOnly, violatingSources as List<Class<?>>)

    where:
    allowedSources                                                                              | disallowedTarget                     | description
    [DomainEventViolationClass, DomainCommandViolationClass, DomainAggregateViolationClass,
     ApplicationPortOutViolationInterface, ApplicationServiceViolationClass,
     AdapterOutViolationClass, AdapterProjectionViolationClass]                                 | DomainModelValueViolationClass       | "* -> ${ DOMAIN_VALUE_LAYER }"
    [DomainAggregateViolationClass, AdapterProjectionViolationClass]                            | DomainEventViolationClass            | "* -> ${ DOMAIN_EVENT_LAYER }"
    [DomainAggregateViolationClass, ApplicationServiceViolationClass]                           | DomainCommandViolationClass          | "* -> ${ DOMAIN_COMMAND_LAYER }"
    [ApplicationServiceViolationClass]                                                          | DomainAggregateViolationClass        | "* -> ${ DOMAIN_AGGREGATE_LAYER }"
    [ApplicationServiceViolationClass, AdapterInViolationClass]                                 | ApplicationPortInViolationInterface  | "* -> ${ APPLICATION_INBOUND_PORT_LAYER }"
    [ApplicationServiceViolationClass, DomainAggregateViolationClass, AdapterOutViolationClass] | ApplicationPortOutViolationInterface | "* -> ${ APPLICATION_OUTBOUND_PORT_LAYER }"
    []                                                                                          | ApplicationServiceViolationClass     | "* -> ${ APPLICATION_SERVICE_LAYER }"
    []                                                                                          | AdapterInViolationClass              | "* -> ${ ADAPTER_INBOUND_LAYER }"
    []                                                                                          | AdapterOutViolationClass             | "* -> ${ ADAPTER_OUTBOUND_LAYER }"
    []                                                                                          | AdapterProjectionViolationClass      | "* -> ${ ADAPTER_PROJECTION_LAYER }"

    violatingSources = fetchViolatingSourcesGeneral(disallowedTarget, allowedSources)
  }

  private List<Class<?>> fetchViolatingSourcesGeneral(Class<?> disallowedTarget, List<Class<?>> allowedSources) {
    return fetchViolatingSources(generalArchitectureAllViolatingSources, disallowedTarget, allowedSources)
  }

  /**
   * Little helper that provides more obvious way for specifying violating sources whn looking into the implementation at <code>HexagonalCqrsEsArchitecture</code>.
   */
  private List<Class<?>> fetchViolatingSources(List<Class<?>> allViolatingSources, Class<?> disallowedTarget, List<Class<?>> allowedSources) {
    List<Class<?>> violatingSources = allViolatingSources - allowedSources
    violatingSources = violatingSources - disallowedTarget
    return violatingSources
  }

  /**
   * Creates <code>EvaluationResult</code> with discarded all violations that are not related to the given <code>disallowedTarget</code>.
   */
  private EvaluationResult evaluateAndDiscardIrrelevantViolations(ArchRule rule, JavaClasses importedViolationClasses, Class<?> disallowedTarget) {
    EvaluationResult evaluationResultForDisallowedTargetOnly = rule.evaluate(importedViolationClasses).filterDescriptionsMatching({ String violationTextualDescription ->
      violationTextualDescription.contains(" <${ disallowedTarget.name }> in ")
    })

    return evaluationResultForDisallowedTargetOnly
  }

  /**
   * Asserts if a dependency between violatingSources and disallowedTarget actually exists in the source code.
   * <p/>
   * If not, it should be added in violating source for test to have sense (with meany example test classes, it is very easy to miss one).
   */
  private void assertIfDependencyExistsBetweenViolatingSourceAndDisallowedTarget(List<Class<?>> violatingSources, Class<?> disallowedTarget) {
    violatingSources.each { Class<?> violatingSource ->
      JavaClass violatingSourceJavaClass = importedViolationClasses.get(violatingSource)
      JavaClass disallowedTargetJavaClass = importedViolationClasses.get(disallowedTarget)

      Set<Dependency> allDependenciesFromViolatingSource = violatingSourceJavaClass.directDependenciesFromSelf
      Set<Dependency> dependenciesFromViolatingSourceToDisallowedTarget = allDependenciesFromViolatingSource.findAll { Dependency dependency ->
        dependency.targetClass == disallowedTargetJavaClass
      }

      assert dependenciesFromViolatingSourceToDisallowedTarget, "There is no dependency from violatingSource to disallowedTarget: ${ violatingSource.simpleName } -> ${ disallowedTarget.simpleName }"
    }
  }

  /**
   * For a failure report (it contains multiple text lines) make sure there is a line describing dependency between each violating source and disallowed target.
   */
  private Boolean doesFailureReportContainsInvalidDependency(EvaluationResult evaluationResultForDisallowedTargetOnly, List<Class<?>> violatingSources) {
    Boolean result = evaluationResultForDisallowedTargetOnly.failureReport.details.every { String failureMessage ->
      violatingSources.any { Class<?> source ->
        failureMessage.contains("${ source.name }")
      }
    }

    return result
  }

  void "should be valid commandside hexagonal CQRS/ES architecture"() {
    given:
    ArchRule rule = fetchArchitectureRule(HexagonalCqrsEsArchitecture.ArchitectureSubType.COMMANDSIDE)

    expect:
    rule.check(importedValidClasses)
  }

  void "should not allow forbidden access with commandside hexagonal CQRS/ES architecture - [#description]"() {
    given:
    log.debug "----- description: ${ description }"
    log.debug "----- violatingSources: ${ violatingSources*.simpleName }"
    log.debug "----- disallowedTarget: ${ disallowedTarget.simpleName }"

    ArchRule rule = fetchArchitectureRule(HexagonalCqrsEsArchitecture.ArchitectureSubType.COMMANDSIDE)

    when:
    EvaluationResult evaluationResultForDisallowedTargetOnly = evaluateAndDiscardIrrelevantViolations(rule, importedViolationClasses, disallowedTarget)

    then:
    assertIfDependencyExistsBetweenViolatingSourceAndDisallowedTarget(violatingSources as List<Class<?>>, disallowedTarget)
    doesFailureReportContainsInvalidDependency(evaluationResultForDisallowedTargetOnly, violatingSources as List<Class<?>>)

    where:
    allowedSources                                                                                     | disallowedTarget                     | description
    [DomainEventViolationClass, DomainCommandViolationClass, DomainAggregateViolationClass,
     ApplicationServiceViolationClass, ApplicationPortOutViolationInterface, AdapterOutViolationClass] | DomainModelValueViolationClass       | "* -> ${ DOMAIN_VALUE_LAYER }"
    [DomainAggregateViolationClass]                                                                    | DomainEventViolationClass            | "* -> ${ DOMAIN_EVENT_LAYER }"
    [DomainAggregateViolationClass, ApplicationServiceViolationClass]                                  | DomainCommandViolationClass          | "* -> ${ DOMAIN_COMMAND_LAYER }"
    [ApplicationServiceViolationClass]                                                                 | DomainAggregateViolationClass        | "* -> ${ DOMAIN_AGGREGATE_LAYER }"
    [ApplicationServiceViolationClass, AdapterInViolationClass]                                        | ApplicationPortInViolationInterface  | "* -> ${ APPLICATION_INBOUND_PORT_LAYER }"
    [ApplicationServiceViolationClass, DomainAggregateViolationClass, AdapterOutViolationClass]        | ApplicationPortOutViolationInterface | "* -> ${ APPLICATION_OUTBOUND_PORT_LAYER }"
    []                                                                                                 | ApplicationServiceViolationClass     | "* -> ${ APPLICATION_SERVICE_LAYER }"
    []                                                                                                 | AdapterInViolationClass              | "* -> ${ ADAPTER_INBOUND_LAYER }"
    []                                                                                                 | AdapterOutViolationClass             | "* -> ${ ADAPTER_OUTBOUND_LAYER }"

    violatingSources = fetchViolatingSourcesCommandside(disallowedTarget, allowedSources)
  }

  private List<Class<?>> fetchViolatingSourcesCommandside(Class<?> disallowedTarget, List<Class<?>> allowedSources) {
    List<Class<?>> commandSideArchitectureAllViolatingSources = generalArchitectureAllViolatingSources - [AdapterProjectionViolationClass]
    return fetchViolatingSources(commandSideArchitectureAllViolatingSources, disallowedTarget, allowedSources)
  }

  void "should be valid projection hexagonal CQRS/ES architecture"() {
    given:
    ArchRule rule = fetchArchitectureRule(HexagonalCqrsEsArchitecture.ArchitectureSubType.PROJECTION)

    expect:
    rule.check(importedValidClasses)
  }

  void "should not allow forbidden access with projection hexagonal CQRS/ES architecture - [#description]"() {
    given:
    log.debug "----- description: ${ description }"
    log.debug "----- violatingSources: ${ violatingSources*.simpleName }"
    log.debug "----- disallowedTarget: ${ disallowedTarget.simpleName }"

    ArchRule rule = fetchArchitectureRule(HexagonalCqrsEsArchitecture.ArchitectureSubType.PROJECTION)

    when:
    EvaluationResult evaluationResultForDisallowedTargetOnly = evaluateAndDiscardIrrelevantViolations(rule, importedViolationClasses, disallowedTarget)

    then:
    assertIfDependencyExistsBetweenViolatingSourceAndDisallowedTarget(violatingSources as List<Class<?>>, disallowedTarget)
    doesFailureReportContainsInvalidDependency(evaluationResultForDisallowedTargetOnly, violatingSources as List<Class<?>>)

    where:
    allowedSources                                               | disallowedTarget                | description
    [DomainEventViolationClass, AdapterProjectionViolationClass] | DomainModelValueViolationClass  | "* -> ${ DOMAIN_VALUE_LAYER }"
    [AdapterProjectionViolationClass]                            | DomainEventViolationClass       | "* -> ${ DOMAIN_EVENT_LAYER }"
    []                                                           | AdapterOutViolationClass        | "* -> ${ ADAPTER_OUTBOUND_LAYER }"
    []                                                           | AdapterProjectionViolationClass | "* -> ${ ADAPTER_PROJECTION_LAYER }"

    violatingSources = fetchViolatingSourcesProjection(disallowedTarget, allowedSources)
  }

  private List<Class<?>> fetchViolatingSourcesProjection(Class<?> disallowedTarget, List<Class<?>> allowedSources) {
    return fetchViolatingSources(projectionArchitectureAllViolatingSources, disallowedTarget, allowedSources)
  }

  void "should be valid queryside hexagonal CQRS/ES architecture"() {
    given:
    ArchRule rule = fetchArchitectureRule(HexagonalCqrsEsArchitecture.ArchitectureSubType.QUERYSIDE)

    expect:
    rule.check(importedValidClasses)
  }

  void "should not allow forbidden access with queryside hexagonal CQRS/ES architecture - [#description]"() {
    given:
    log.debug "----- description: ${ description }"
    log.debug "----- violatingSources: ${ violatingSources*.simpleName }"
    log.debug "----- disallowedTarget: ${ disallowedTarget.simpleName }"

    ArchRule rule = fetchArchitectureRule(HexagonalCqrsEsArchitecture.ArchitectureSubType.QUERYSIDE)

    when:
    EvaluationResult evaluationResultForDisallowedTargetOnly = evaluateAndDiscardIrrelevantViolations(rule, importedViolationClasses, disallowedTarget)

    then:
    assertIfDependencyExistsBetweenViolatingSourceAndDisallowedTarget(violatingSources as List<Class<?>>, disallowedTarget)
    doesFailureReportContainsInvalidDependency(evaluationResultForDisallowedTargetOnly, violatingSources as List<Class<?>>)

    where:
    allowedSources                                                                                     | disallowedTarget                     | description
    [ApplicationServiceViolationClass, ApplicationPortOutViolationInterface, AdapterOutViolationClass] | DomainModelValueViolationClass       | "* -> ${ DOMAIN_VALUE_LAYER }"
    [ApplicationServiceViolationClass, AdapterInViolationClass, AdapterOutViolationClass]              | ApplicationPortInViolationInterface  | "* -> ${ APPLICATION_INBOUND_PORT_LAYER }"
    [ApplicationServiceViolationClass, AdapterOutViolationClass]                                       | ApplicationPortOutViolationInterface | "* -> ${ APPLICATION_OUTBOUND_PORT_LAYER }"
    []                                                                                                 | ApplicationServiceViolationClass     | "* -> ${ APPLICATION_SERVICE_LAYER }"
    []                                                                                                 | AdapterInViolationClass              | "* -> ${ ADAPTER_INBOUND_LAYER }"
    []                                                                                                 | AdapterOutViolationClass             | "* -> ${ ADAPTER_OUTBOUND_LAYER }"

    violatingSources = fetchViolatingSourcesQuerySide(disallowedTarget, allowedSources)
  }

  private List<Class<?>> fetchViolatingSourcesQuerySide(Class<?> disallowedTarget, List<Class<?>> allowedSources) {
    List<Class<?>> querySideArchitectureAllViolatingSources =
        generalArchitectureAllViolatingSources - [DomainEventViolationClass, DomainCommandViolationClass, DomainAggregateViolationClass, AdapterProjectionViolationClass]

    return fetchViolatingSources(querySideArchitectureAllViolatingSources, disallowedTarget, allowedSources)
  }
}
