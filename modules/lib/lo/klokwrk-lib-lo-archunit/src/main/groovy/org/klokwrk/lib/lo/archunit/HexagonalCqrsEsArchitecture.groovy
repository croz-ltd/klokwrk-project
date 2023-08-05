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
package org.klokwrk.lib.lo.archunit

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.EvaluationResult
import com.tngtech.archunit.library.Architectures
import com.tngtech.archunit.library.Architectures.LayeredArchitecture
import groovy.transform.CompileStatic

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name
import static java.lang.System.lineSeparator
import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.not

/**
 * Implementation of ArchUnit's <code>ArchRule</code> for asserting CQRS/ES flavored hexagonal architecture used in <code>klokwrk</code>.
 * <p/>
 * It supports 4 variants of CQRS/ES flavored hexagonal architecture: general (without specific architecture subtype), commandSide, querySide and projection. It is inspired by ArchUnit's
 * implementation for asserting onion architecture and follows very similar usages patterns.
 * <p/>
 * Usage example for asserting commandSide subtype of CQRS/ES flavored hexagonal architecture (from klokwrk's code):
 * <p/>
 * <pre>
 * ArchRule rule = HexagonalCqrsEsArchitecture
 *     .architecture(HexagonalCqrsEsArchitecture.ArchitectureSubType.COMMANDSIDE)
 *     .domainValues("..cargotracking.domain.model.value..")
 *     .domainEvents("..cargotracking.domain.model.event..")
 *     .domainCommands("..cargotracking.domain.model.command..")
 *     .domainAggregates("..cargotracking.domain.model.aggregate..")
 *
 *     .applicationInboundPorts("..cargotracking.booking.app.commandside.feature.*.application.port.in..")
 *     .applicationOutboundPorts("..cargotracking.booking.app.commandside.feature.*.application.port.out..")
 *     .applicationServices("..cargotracking.booking.app.commandside.feature.*.application.service..")
 *
 *     .adapterInbound("in.web", "..cargotracking.booking.app.commandside.feature.*.adapter.in.web..")
 *     .adapterOutbound("out.remoting", "..cargotracking.booking.app.commandside.feature.*.adapter.out.remoting..")
 *
 *     .withOptionalLayers(false)
 *
 * rule.check(importedClasses)
 * </pre>
 * <p/>
 * Usage example for asserting projection subtype of CQRS/ES flavored hexagonal architecture (from klokwrk's code):
 * <p/>
 * <pre>
 * ArchRule rule = HexagonalCqrsEsArchitecture
 *     .architecture(HexagonalCqrsEsArchitecture.ArchitectureSubType.PROJECTION)
 *     .domainModelValues("..cargotracking.domain.model.value..")
 *     .domainEvents("..cargotracker.booking.axon.api.feature.*.event..")
 *
 *     .adapterProjection("out.persistence", "..cargotracking.booking.app.queryside.projection.rdbms.feature.*.adapter.out..")
 *
 *     // We are ignoring dependencies originating from command classes. Command classes should not be used in projections. Only events can be used. Since command and events are not split it their
 *     // own modules, we need to ignore commands here. Illegal access to commands is verified in other test.
 *     .ignoreDependency(JavaClass.Predicates.resideInAPackage("org.klokwrk.cargotracker.booking.axon.api.feature.*.command.."), JavaClass.Predicates.resideInAPackage("org.klokwrk.cargotracker.."))
 *     .withOptionalLayers(false)
 *
 * rule.check(importedClasses)
 * </pre>
 * <p/>
 * Usage example for asserting querySide subtype of CQRS/ES flavored hexagonal architecture (from klokwrk's code):
 * <p/>
 * <pre>
 * ArchRule rule = HexagonalCqrsEsArchitecture
 *     .architecture(HexagonalCqrsEsArchitecture.ArchitectureSubType.QUERYSIDE)
 *     .domainModelValues("..cargotracking.domain.model.value..")
 *
 *     .applicationInboundPorts("..cargotracker.booking.queryside.feature.*.application.port.in..")
 *     .applicationOutboundPorts("..cargotracker.booking.queryside.feature.*.application.port.out..")
 *     .applicationServices("..cargotracker.booking.queryside.feature.*.application.service..")
 *
 *     .adapterInbound("in.web", "..cargotracker.booking.queryside.feature.*.adapter.in.web..")
 *     .adapterOutbound("out.persistence", "..cargotracker.booking.queryside.feature.*.adapter.out.persistence..")
 *
 *     .withOptionalLayers(false)
 *
 * rule.check(importedClasses)
 * </pre>
 */
@SuppressWarnings("CodeNarc.MethodCount")
@CompileStatic
class HexagonalCqrsEsArchitecture implements ArchRule {

  static final String DOMAIN_VALUE_LAYER = "domain value"
  static final String DOMAIN_EVENT_LAYER = "domain event"
  static final String DOMAIN_COMMAND_LAYER = "domain command"
  static final String DOMAIN_SERVICE_LAYER = "domain service"
  static final String DOMAIN_AGGREGATE_LAYER = "domain aggregate"

  static final String APPLICATION_SERVICE_LAYER = "application service"
  static final String APPLICATION_INBOUND_PORT_LAYER = "application inbound port"
  static final String APPLICATION_OUTBOUND_PORT_LAYER = "application outbound port"

  static final String ADAPTER_INBOUND_LAYER = "inbound adapter"
  static final String ADAPTER_OUTBOUND_LAYER = "outbound adapter"
  static final String ADAPTER_PROJECTION_LAYER = "projection adapter"

  private static final String QUOTED_SPACED_COMMA_SEPARATOR = "', '"

  private final ArchitectureSubType architectureSubType

  private String[] domainValuePackageIdentifiers = []
  private String[] domainEventPackageIdentifiers = []
  private String[] domainCommandPackageIdentifiers = []
  private String[] domainServicePackageIdentifiers = []
  private String[] domainAggregatePackageIdentifiers = []

  private String[] applicationInboundPortPackageIdentifiers = []
  private String[] applicationOutboundPortPackageIdentifiers = []
  private String[] applicationServicePackageIdentifiers = []

  private final Map<String, String[]> adapterInboundPackageIdentifiers
  private final Map<String, String[]> adapterOutboundPackageIdentifiers
  private final Map<String, String[]> adapterProjectionPackageIdentifiers

  private final String overriddenDescription

  private boolean optionalLayers = false
  private final List<IgnoredDependency> ignoredDependencies

  static HexagonalCqrsEsArchitecture architecture(ArchitectureSubType architectureSubType = ArchitectureSubType.NONE) {
    return new HexagonalCqrsEsArchitecture(architectureSubType)
  }

  private HexagonalCqrsEsArchitecture(ArchitectureSubType architectureSubType, String overriddenDescription = null) {
    this.adapterInboundPackageIdentifiers = [:]
    this.adapterOutboundPackageIdentifiers = [:]
    this.adapterProjectionPackageIdentifiers = [:]

    this.ignoredDependencies = []
    this.overriddenDescription = overriddenDescription
    this.architectureSubType = architectureSubType
  }

  private HexagonalCqrsEsArchitecture copyWith(String overriddenDescription) {
    HexagonalCqrsEsArchitecture newHexagonalCqrsEsArchitecture = new HexagonalCqrsEsArchitecture(architectureSubType, overriddenDescription)
        .domainValues(domainValuePackageIdentifiers)
        .domainEvents(domainEventPackageIdentifiers)
        .domainCommands(domainCommandPackageIdentifiers)
        .domainServices(domainServicePackageIdentifiers)
        .domainAggregates(domainAggregatePackageIdentifiers)

        .applicationInboundPorts(applicationInboundPortPackageIdentifiers)
        .applicationOutboundPorts(applicationOutboundPortPackageIdentifiers)
        .applicationServices(applicationServicePackageIdentifiers)

    adapterInboundPackageIdentifiers.each { Map.Entry<String, String[]> mapEntry -> newHexagonalCqrsEsArchitecture.adapterInbound(mapEntry.key, mapEntry.value) }
    adapterOutboundPackageIdentifiers.each { Map.Entry<String, String[]> mapEntry -> newHexagonalCqrsEsArchitecture.adapterInbound(mapEntry.key, mapEntry.value) }
    adapterProjectionPackageIdentifiers.each { Map.Entry<String, String[]> mapEntry -> newHexagonalCqrsEsArchitecture.adapterInbound(mapEntry.key, mapEntry.value) }

    return newHexagonalCqrsEsArchitecture
  }

  HexagonalCqrsEsArchitecture domainValues(String... packageIdentifiers) {
    domainValuePackageIdentifiers = packageIdentifiers
    return this
  }

  HexagonalCqrsEsArchitecture domainEvents(String... packageIdentifiers) {
    domainEventPackageIdentifiers = packageIdentifiers
    return this
  }

  HexagonalCqrsEsArchitecture domainCommands(String... packageIdentifiers) {
    domainCommandPackageIdentifiers = packageIdentifiers
    return this
  }

  HexagonalCqrsEsArchitecture domainServices(String... packageIdentifiers) {
    domainServicePackageIdentifiers = packageIdentifiers
    return this
  }

  HexagonalCqrsEsArchitecture domainAggregates(String... packageIdentifiers) {
    domainAggregatePackageIdentifiers = packageIdentifiers
    return this
  }

  HexagonalCqrsEsArchitecture applicationInboundPorts(String... packageIdentifiers) {
    applicationInboundPortPackageIdentifiers = packageIdentifiers
    return this
  }

  HexagonalCqrsEsArchitecture applicationOutboundPorts(String... packageIdentifiers) {
    applicationOutboundPortPackageIdentifiers = packageIdentifiers
    return this
  }

  HexagonalCqrsEsArchitecture applicationServices(String... packageIdentifiers) {
    applicationServicePackageIdentifiers = packageIdentifiers
    return this
  }

  HexagonalCqrsEsArchitecture adapterInbound(String name, String... packageIdentifiers) {
    adapterInboundPackageIdentifiers[name] = packageIdentifiers
    return this
  }

  HexagonalCqrsEsArchitecture adapterOutbound(String name, String... packageIdentifiers) {
    adapterOutboundPackageIdentifiers[name] = packageIdentifiers
    return this
  }

  HexagonalCqrsEsArchitecture adapterProjection(String name, String... packageIdentifiers) {
    adapterProjectionPackageIdentifiers[name] = packageIdentifiers
    return this
  }

  HexagonalCqrsEsArchitecture withOptionalLayers(boolean optionalLayers) {
    this.optionalLayers = optionalLayers
    return this
  }

  HexagonalCqrsEsArchitecture ignoreDependency(Class<?> origin, Class<?> target) {
    return ignoreDependency(equivalentTo(origin), equivalentTo(target))
  }

  HexagonalCqrsEsArchitecture ignoreDependency(String origin, String target) {
    return ignoreDependency(name(origin), name(target))
  }

  HexagonalCqrsEsArchitecture ignoreDependency(DescribedPredicate<? super JavaClass> origin, DescribedPredicate<? super JavaClass> target) {
    this.ignoredDependencies << new IgnoredDependency(origin, target)
    return this
  }

  private LayeredArchitecture layeredArchitectureDelegate() {
    LayeredArchitecture layeredArchitectureDelegate = Architectures.layeredArchitecture().consideringAllDependencies()

    switch (architectureSubType) {
      case ArchitectureSubType.COMMANDSIDE:
        specifyArchitectureCommandSide(layeredArchitectureDelegate)
        break
      case ArchitectureSubType.PROJECTION:
        specifyArchitectureProjection(layeredArchitectureDelegate)
        break
      case ArchitectureSubType.QUERYSIDE:
        specifyArchitectureQuerySide(layeredArchitectureDelegate)
        break
      default:
        specifyArchitectureDefault(layeredArchitectureDelegate)
        break
    }

    ignoredDependencies.each { IgnoredDependency ignoredDependency ->
      layeredArchitectureDelegate = ignoredDependency.ignoreFor(layeredArchitectureDelegate)
    }

    layeredArchitectureDelegate = layeredArchitectureDelegate.withOptionalLayers(optionalLayers)
    layeredArchitectureDelegate = layeredArchitectureDelegate.as(description)

    return layeredArchitectureDelegate
  }

  @SuppressWarnings("CodeNarc.UseCollectMany")
  private void specifyArchitectureDefault(LayeredArchitecture layeredArchitecture) {
    // @formatter:off
    //noinspection DuplicatedCode
    layeredArchitecture
        .layer(DOMAIN_VALUE_LAYER).definedBy(domainValuePackageIdentifiers)
        .layer(DOMAIN_EVENT_LAYER).definedBy(domainEventPackageIdentifiers)
        .layer(DOMAIN_COMMAND_LAYER).definedBy(domainCommandPackageIdentifiers)
        .layer(DOMAIN_SERVICE_LAYER).definedBy(domainServicePackageIdentifiers)
        .layer(DOMAIN_AGGREGATE_LAYER).definedBy(domainAggregatePackageIdentifiers)

        .layer(APPLICATION_INBOUND_PORT_LAYER).definedBy(applicationInboundPortPackageIdentifiers)
        .layer(APPLICATION_OUTBOUND_PORT_LAYER).definedBy(applicationOutboundPortPackageIdentifiers)
        .layer(APPLICATION_SERVICE_LAYER).definedBy(applicationServicePackageIdentifiers)

        .layer(ADAPTER_INBOUND_LAYER).definedBy(adapterInboundPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])
        .layer(ADAPTER_OUTBOUND_LAYER).definedBy(adapterOutboundPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])
        .layer(ADAPTER_PROJECTION_LAYER).definedBy(adapterProjectionPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])

        // We are allowing access to DOMAIN_VALUE_LAYER for APPLICATION_INBOUND_PORT_LAYER and ADAPTER_INBOUND_LAYER. This might require creation of domain specific (or at least, closer to domain)
        // JSON deserializers/serializers and validation constraints
        .whereLayer(DOMAIN_VALUE_LAYER)
            .mayOnlyBeAccessedByLayers(
                DOMAIN_EVENT_LAYER, DOMAIN_COMMAND_LAYER, DOMAIN_SERVICE_LAYER, DOMAIN_AGGREGATE_LAYER,
                APPLICATION_INBOUND_PORT_LAYER, APPLICATION_OUTBOUND_PORT_LAYER, APPLICATION_SERVICE_LAYER,
                ADAPTER_INBOUND_LAYER, ADAPTER_OUTBOUND_LAYER, ADAPTER_PROJECTION_LAYER
            )
        .whereLayer(DOMAIN_EVENT_LAYER).mayOnlyBeAccessedByLayers(DOMAIN_AGGREGATE_LAYER, ADAPTER_PROJECTION_LAYER)
        .whereLayer(DOMAIN_COMMAND_LAYER).mayOnlyBeAccessedByLayers(DOMAIN_AGGREGATE_LAYER, APPLICATION_SERVICE_LAYER)
        .whereLayer(DOMAIN_SERVICE_LAYER).mayOnlyBeAccessedByLayers(DOMAIN_AGGREGATE_LAYER)
        .whereLayer(DOMAIN_AGGREGATE_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_SERVICE_LAYER)

        .whereLayer(APPLICATION_INBOUND_PORT_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_SERVICE_LAYER, ADAPTER_INBOUND_LAYER)
        .whereLayer(APPLICATION_OUTBOUND_PORT_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_SERVICE_LAYER, DOMAIN_AGGREGATE_LAYER, ADAPTER_OUTBOUND_LAYER)
        // Everything should go through INBOUND_PORT_LAYER
        .whereLayer(APPLICATION_SERVICE_LAYER).mayNotBeAccessedByAnyLayer()
    // @formatter:on

    adapterMayNotBeAccessedByAnyLayer(layeredArchitecture, adapterInboundPackageIdentifiers, ADAPTER_INBOUND_LAYER)
    adapterMayNotBeAccessedByAnyLayer(layeredArchitecture, adapterOutboundPackageIdentifiers, ADAPTER_OUTBOUND_LAYER)
    adapterMayNotBeAccessedByAnyLayer(layeredArchitecture, adapterProjectionPackageIdentifiers, ADAPTER_PROJECTION_LAYER)
  }

  @SuppressWarnings("CodeNarc.UseCollectMany")
  private void specifyArchitectureCommandSide(LayeredArchitecture layeredArchitecture) {
    // @formatter:off
    //noinspection DuplicatedCode
    layeredArchitecture
        .layer(DOMAIN_VALUE_LAYER).definedBy(domainValuePackageIdentifiers)
        .layer(DOMAIN_EVENT_LAYER).definedBy(domainEventPackageIdentifiers)
        .layer(DOMAIN_COMMAND_LAYER).definedBy(domainCommandPackageIdentifiers)
        .layer(DOMAIN_SERVICE_LAYER).definedBy(domainServicePackageIdentifiers)
        .layer(DOMAIN_AGGREGATE_LAYER).definedBy(domainAggregatePackageIdentifiers)

        .layer(APPLICATION_INBOUND_PORT_LAYER).definedBy(applicationInboundPortPackageIdentifiers)
        .optionalLayer(APPLICATION_OUTBOUND_PORT_LAYER).definedBy(applicationOutboundPortPackageIdentifiers)
        .layer(APPLICATION_SERVICE_LAYER).definedBy(applicationServicePackageIdentifiers)

        .layer(ADAPTER_INBOUND_LAYER).definedBy(adapterInboundPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])
        .optionalLayer(ADAPTER_OUTBOUND_LAYER).definedBy(adapterOutboundPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])

        // We are allowing access to DOMAIN_VALUE_LAYER for APPLICATION_INBOUND_PORT_LAYER and ADAPTER_INBOUND_LAYER. This might require creation of domain specific (or at least, closer to domain)
        // JSON deserializers/serializers and validation constraints
        .whereLayer(DOMAIN_VALUE_LAYER)
            .mayOnlyBeAccessedByLayers(
                DOMAIN_EVENT_LAYER, DOMAIN_COMMAND_LAYER, DOMAIN_SERVICE_LAYER, DOMAIN_AGGREGATE_LAYER,
                APPLICATION_INBOUND_PORT_LAYER, APPLICATION_SERVICE_LAYER, APPLICATION_OUTBOUND_PORT_LAYER,
                ADAPTER_INBOUND_LAYER, ADAPTER_OUTBOUND_LAYER
            )
        .whereLayer(DOMAIN_EVENT_LAYER).mayOnlyBeAccessedByLayers(DOMAIN_AGGREGATE_LAYER)
        .whereLayer(DOMAIN_COMMAND_LAYER).mayOnlyBeAccessedByLayers(DOMAIN_AGGREGATE_LAYER, APPLICATION_SERVICE_LAYER)
        .whereLayer(DOMAIN_SERVICE_LAYER).mayOnlyBeAccessedByLayers(DOMAIN_AGGREGATE_LAYER)
        .whereLayer(DOMAIN_AGGREGATE_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_SERVICE_LAYER)

        .whereLayer(APPLICATION_INBOUND_PORT_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_SERVICE_LAYER, ADAPTER_INBOUND_LAYER)
        .whereLayer(APPLICATION_OUTBOUND_PORT_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_SERVICE_LAYER, DOMAIN_AGGREGATE_LAYER, ADAPTER_OUTBOUND_LAYER)
        // Everything should go through INBOUND_PORT_LAYER
        .whereLayer(APPLICATION_SERVICE_LAYER).mayNotBeAccessedByAnyLayer()
    // @formatter:on

    adapterMayNotBeAccessedByAnyLayer(layeredArchitecture, adapterInboundPackageIdentifiers, ADAPTER_INBOUND_LAYER)
    adapterMayNotBeAccessedByAnyLayer(layeredArchitecture, adapterOutboundPackageIdentifiers, ADAPTER_OUTBOUND_LAYER)
  }

  @SuppressWarnings("CodeNarc.UseCollectMany")
  private void specifyArchitectureProjection(LayeredArchitecture layeredArchitecture) {
    // @formatter:off
    layeredArchitecture
        .optionalLayer(DOMAIN_VALUE_LAYER).definedBy(domainValuePackageIdentifiers)
        .layer(DOMAIN_EVENT_LAYER).definedBy(domainEventPackageIdentifiers)

        .layer(ADAPTER_PROJECTION_LAYER).definedBy(adapterProjectionPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])

        .whereLayer(DOMAIN_VALUE_LAYER).mayOnlyBeAccessedByLayers(DOMAIN_EVENT_LAYER, ADAPTER_PROJECTION_LAYER)
        .whereLayer(DOMAIN_EVENT_LAYER).mayOnlyBeAccessedByLayers(ADAPTER_PROJECTION_LAYER)
    // @formatter:on

    adapterMayNotBeAccessedByAnyLayer(layeredArchitecture, adapterOutboundPackageIdentifiers, ADAPTER_OUTBOUND_LAYER)
    adapterMayNotBeAccessedByAnyLayer(layeredArchitecture, adapterProjectionPackageIdentifiers, ADAPTER_PROJECTION_LAYER)
  }

  @SuppressWarnings("CodeNarc.UseCollectMany")
  private void specifyArchitectureQuerySide(LayeredArchitecture layeredArchitecture) {
    // @formatter:off
    layeredArchitecture
        .optionalLayer(DOMAIN_VALUE_LAYER).definedBy(domainValuePackageIdentifiers)

        .layer(APPLICATION_INBOUND_PORT_LAYER).definedBy(applicationInboundPortPackageIdentifiers)
        .optionalLayer(APPLICATION_OUTBOUND_PORT_LAYER).definedBy(applicationOutboundPortPackageIdentifiers)
        .layer(APPLICATION_SERVICE_LAYER).definedBy(applicationServicePackageIdentifiers)

        .layer(ADAPTER_INBOUND_LAYER).definedBy(adapterInboundPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])
        .layer(ADAPTER_OUTBOUND_LAYER).definedBy(adapterOutboundPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])

        .whereLayer(DOMAIN_VALUE_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_SERVICE_LAYER, APPLICATION_INBOUND_PORT_LAYER, APPLICATION_OUTBOUND_PORT_LAYER, ADAPTER_OUTBOUND_LAYER)

        // Here we are allowing for outbound layer to access types defined in APPLICATION_INBOUND_PORT_LAYER. Reason is, this is queryside where often application services just pass
        // inbound data to the outbound queries. Outbound queries than can use response types that are defined in inbound ports.
        .whereLayer(APPLICATION_INBOUND_PORT_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_SERVICE_LAYER, ADAPTER_INBOUND_LAYER, ADAPTER_OUTBOUND_LAYER)
        .whereLayer(APPLICATION_OUTBOUND_PORT_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_SERVICE_LAYER, ADAPTER_OUTBOUND_LAYER)
        // Everything should go through INBOUND_PORT_LAYER
        .whereLayer(APPLICATION_SERVICE_LAYER).mayNotBeAccessedByAnyLayer()
    // @formatter:on

    adapterMayNotBeAccessedByAnyLayer(layeredArchitecture, adapterInboundPackageIdentifiers, ADAPTER_INBOUND_LAYER)
    adapterMayNotBeAccessedByAnyLayer(layeredArchitecture, adapterOutboundPackageIdentifiers, ADAPTER_OUTBOUND_LAYER)
  }

  private void adapterMayNotBeAccessedByAnyLayer(LayeredArchitecture layeredArchitecture, Map<String, String[]> adapterPackageIdentifiers, String adapterLayerDescription) {
    adapterPackageIdentifiers.each { Map.Entry<String, String[]> adapter ->
      String adapterLayer = "${ adapter.key } ${ adapterLayerDescription }"
      layeredArchitecture
          .layer(adapterLayer).definedBy(adapter.value)
          .whereLayer(adapterLayer).mayNotBeAccessedByAnyLayer()
    }
  }

  @Override
  void check(JavaClasses classes) {
    layeredArchitectureDelegate().check(classes)
  }

  @Override
  HexagonalCqrsEsArchitecture because(String reason) {
    return Factory.withBecause(this, reason) as HexagonalCqrsEsArchitecture
  }

  @Override
  ArchRule allowEmptyShould(boolean allowEmptyShould) {
    return withOptionalLayers(allowEmptyShould)
  }

  @Override
  HexagonalCqrsEsArchitecture "as"(String newDescription) {
    requireMatch(newDescription, not(blankOrNullString()))
    return copyWith(newDescription)
  }

  @Override
  EvaluationResult evaluate(JavaClasses classes) {
    return layeredArchitectureDelegate().evaluate(classes)
  }

  @Override
  String getDescription() {
    if (overriddenDescription) {
      return overriddenDescription
    }

    List<String> lines = ["Hexagonal architecture (CQRS/ES flavor, subtype ${architectureSubType}) consisting of ${ optionalLayers ? " (optional)" : "" }".toString()]

    lines.with {
      addAll(describeStandardPackageIdentifiers(domainValuePackageIdentifiers, "domain value objects"))
      addAll(describeStandardPackageIdentifiers(domainEventPackageIdentifiers, "domain events"))
      addAll(describeStandardPackageIdentifiers(domainCommandPackageIdentifiers, "domain commands"))
      addAll(describeStandardPackageIdentifiers(domainServicePackageIdentifiers, "domain services"))
      addAll(describeStandardPackageIdentifiers(domainAggregatePackageIdentifiers, "domain entities and aggregates"))

      addAll(describeStandardPackageIdentifiers(applicationInboundPortPackageIdentifiers, "application inbound ports"))
      addAll(describeStandardPackageIdentifiers(applicationOutboundPortPackageIdentifiers, "application outbound ports"))
      addAll(describeStandardPackageIdentifiers(applicationServicePackageIdentifiers, "application services"))

      addAll(describeAdapterPackageIdentifiers(adapterInboundPackageIdentifiers, "adapter inbound"))
      addAll(describeAdapterPackageIdentifiers(adapterOutboundPackageIdentifiers, "adapter outbound"))
      addAll(describeAdapterPackageIdentifiers(adapterProjectionPackageIdentifiers, "adapter projection"))
    }

    return lines.join(lineSeparator())
  }

  private List<String> describeStandardPackageIdentifiers(String[] packageIdentifiers, String packageIdentifiersName) {
    List<String> packageIdentifiersDescription = []

    if (packageIdentifiers.size() > 0) {
      packageIdentifiersDescription << "${packageIdentifiersName} ('${ packageIdentifiers.join(QUOTED_SPACED_COMMA_SEPARATOR) }')".toString()
    }

    return packageIdentifiersDescription
  }

  private List<String> describeAdapterPackageIdentifiers(Map<String, String[]> adapterPackageIdentifiers, String adapterPackageIdentifiersName) {
    List<String> adapterPackageIdentifiersLines = []
    adapterPackageIdentifiers.each { Map.Entry<String, String[]> mapEntry ->
      adapterPackageIdentifiersLines << "${adapterPackageIdentifiersName} '${ mapEntry.key }' ('${ mapEntry.value.join(QUOTED_SPACED_COMMA_SEPARATOR) }')".toString()
    }

    return adapterPackageIdentifiersLines
  }

  static enum ArchitectureSubType {
    COMMANDSIDE, PROJECTION, QUERYSIDE, NONE
  }

  private static class IgnoredDependency {
    private final DescribedPredicate<? super JavaClass> origin
    private final DescribedPredicate<? super JavaClass> target

    IgnoredDependency(DescribedPredicate<? super JavaClass> origin, DescribedPredicate<? super JavaClass> target) {
      this.origin = origin
      this.target = target
    }

    LayeredArchitecture ignoreFor(LayeredArchitecture layeredArchitecture) {
      return layeredArchitecture.ignoreDependency(origin, target)
    }
  }
}
