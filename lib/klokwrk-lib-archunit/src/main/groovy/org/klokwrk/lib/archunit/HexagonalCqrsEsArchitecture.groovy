package org.klokwrk.lib.archunit

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.base.Optional
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

// TODO dmurat: document this ArchRule.
@CompileStatic
class HexagonalCqrsEsArchitecture implements ArchRule {

  static final String DOMAIN_MODEL_LAYER = "domain model"
  static final String DOMAIN_EVENT_LAYER = "domain event"
  static final String DOMAIN_COMMAND_LAYER = "domain command"
  static final String DOMAIN_AGGREGATE_LAYER = "domain aggregate"

  static final String APPLICATION_SERVICE_LAYER = "application service"
  static final String APPLICATION_INBOUND_PORT_LAYER = "application inbound port"
  static final String APPLICATION_OUTBOUND_PORT_LAYER = "application outbound port"

  static final String ADAPTER_INBOUND_LAYER = "inbound adapter"
  static final String ADAPTER_OUTBOUND_LAYER = "outbound adapter"
  static final String ADAPTER_PROJECTION_LAYER = "projection adapter"

  private final ArchitectureSubType architectureSubType

  private String[] domainModelPackageIdentifiers = []
  private String[] domainEventPackageIdentifiers = []
  private String[] domainCommandPackageIdentifiers = []
  private String[] domainAggregatePackageIdentifiers = []

  private String[] applicationInboundPortPackageIdentifiers = []
  private String[] applicationOutboundPortPackageIdentifiers = []
  private String[] applicationServicePackageIdentifiers = []

  private final Map<String, String[]> adapterInboundPackageIdentifiers
  private final Map<String, String[]> adapterOutboundPackageIdentifiers
  private final Map<String, String[]> adapterProjectionPackageIdentifiers

  private final Optional<String> overriddenDescription

  private boolean optionalLayers = false
  private final List<IgnoredDependency> ignoredDependencies

  static HexagonalCqrsEsArchitecture architecture(ArchitectureSubType architectureSubType = ArchitectureSubType.NONE) {
    return new HexagonalCqrsEsArchitecture(Optional.absent() as Optional<String>, architectureSubType)
  }

  private HexagonalCqrsEsArchitecture(Optional<String> overriddenDescription, ArchitectureSubType architectureSubType) {
    this.adapterInboundPackageIdentifiers = [:]
    this.adapterOutboundPackageIdentifiers = [:]
    this.adapterProjectionPackageIdentifiers = [:]

    this.ignoredDependencies = []
    this.overriddenDescription = overriddenDescription
    this.architectureSubType = architectureSubType
  }

  private HexagonalCqrsEsArchitecture copyWith(Optional<String> overriddenDescription) {
    HexagonalCqrsEsArchitecture newHexagonalCqrsEsArchitecture = new HexagonalCqrsEsArchitecture(overriddenDescription, architectureSubType)
        .domainModels(domainModelPackageIdentifiers)
        .domainEvents(domainEventPackageIdentifiers)
        .domainCommands(domainCommandPackageIdentifiers)
        .domainAggregates(domainAggregatePackageIdentifiers)

        .applicationInboundPorts(applicationInboundPortPackageIdentifiers)
        .applicationOutboundPorts(applicationOutboundPortPackageIdentifiers)
        .applicationServices(applicationServicePackageIdentifiers)

    adapterInboundPackageIdentifiers.each { Map.Entry<String, String[]> mapEntry -> newHexagonalCqrsEsArchitecture.adapterInbound(mapEntry.key, mapEntry.value) }
    adapterOutboundPackageIdentifiers.each { Map.Entry<String, String[]> mapEntry -> newHexagonalCqrsEsArchitecture.adapterInbound(mapEntry.key, mapEntry.value) }
    adapterProjectionPackageIdentifiers.each { Map.Entry<String, String[]> mapEntry -> newHexagonalCqrsEsArchitecture.adapterInbound(mapEntry.key, mapEntry.value) }

    return newHexagonalCqrsEsArchitecture
  }

  HexagonalCqrsEsArchitecture domainModels(String... packageIdentifiers) {
    domainModelPackageIdentifiers = packageIdentifiers
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

  @SuppressWarnings('unused')
  HexagonalCqrsEsArchitecture adapterProjection(String name, String... packageIdentifiers) {
    adapterProjectionPackageIdentifiers[name] = packageIdentifiers
    return this
  }

  HexagonalCqrsEsArchitecture withOptionalLayers(boolean optionalLayers) {
    this.optionalLayers = optionalLayers
    return this
  }

  @SuppressWarnings('unused')
  HexagonalCqrsEsArchitecture ignoreDependency(Class<?> origin, Class<?> target) {
    return ignoreDependency(equivalentTo(origin), equivalentTo(target))
  }

  @SuppressWarnings('unused')
  HexagonalCqrsEsArchitecture ignoreDependency(String origin, String target) {
    return ignoreDependency(name(origin), name(target))
  }

  HexagonalCqrsEsArchitecture ignoreDependency(DescribedPredicate<? super JavaClass> origin, DescribedPredicate<? super JavaClass> target) {
    this.ignoredDependencies << new IgnoredDependency(origin, target)
    return this
  }

  private LayeredArchitecture layeredArchitectureDelegate() {
    LayeredArchitecture layeredArchitectureDelegate = Architectures.layeredArchitecture()

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

  @SuppressWarnings("UseCollectMany")
  private void specifyArchitectureDefault(LayeredArchitecture layeredArchitecture) {
    // @formatter:off
    //noinspection DuplicatedCode
    layeredArchitecture
        .layer(DOMAIN_MODEL_LAYER).definedBy(domainModelPackageIdentifiers)
        .layer(DOMAIN_EVENT_LAYER).definedBy(domainEventPackageIdentifiers)
        .layer(DOMAIN_COMMAND_LAYER).definedBy(domainCommandPackageIdentifiers)
        .layer(DOMAIN_AGGREGATE_LAYER).definedBy(domainAggregatePackageIdentifiers)

        .layer(APPLICATION_INBOUND_PORT_LAYER).definedBy(applicationInboundPortPackageIdentifiers)
        .layer(APPLICATION_OUTBOUND_PORT_LAYER).definedBy(applicationOutboundPortPackageIdentifiers)
        .layer(APPLICATION_SERVICE_LAYER).definedBy(applicationServicePackageIdentifiers)

        .layer(ADAPTER_INBOUND_LAYER).definedBy(adapterInboundPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])
        .layer(ADAPTER_OUTBOUND_LAYER).definedBy(adapterOutboundPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])
        .layer(ADAPTER_PROJECTION_LAYER).definedBy(adapterProjectionPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])

        // For now, access to DOMAIN_MODEL_LAYER is forbidden for APPLICATION_INBOUND_PORT_LAYER and ADAPTER_INBOUND_LAYER. Will see how this goes.
        .whereLayer(DOMAIN_MODEL_LAYER)
            .mayOnlyBeAccessedByLayers(
                DOMAIN_EVENT_LAYER, DOMAIN_COMMAND_LAYER, DOMAIN_AGGREGATE_LAYER, APPLICATION_SERVICE_LAYER, APPLICATION_OUTBOUND_PORT_LAYER,
                ADAPTER_OUTBOUND_LAYER, ADAPTER_PROJECTION_LAYER
            )
        .whereLayer(DOMAIN_EVENT_LAYER).mayOnlyBeAccessedByLayers(DOMAIN_AGGREGATE_LAYER, ADAPTER_PROJECTION_LAYER)
        .whereLayer(DOMAIN_COMMAND_LAYER).mayOnlyBeAccessedByLayers(DOMAIN_AGGREGATE_LAYER, APPLICATION_SERVICE_LAYER)
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

  @SuppressWarnings("UseCollectMany")
  private void specifyArchitectureCommandSide(LayeredArchitecture layeredArchitecture) {
    // @formatter:off
    //noinspection DuplicatedCode
    layeredArchitecture
        .layer(DOMAIN_MODEL_LAYER).definedBy(domainModelPackageIdentifiers)
        .layer(DOMAIN_EVENT_LAYER).definedBy(domainEventPackageIdentifiers)
        .layer(DOMAIN_COMMAND_LAYER).definedBy(domainCommandPackageIdentifiers)
        .layer(DOMAIN_AGGREGATE_LAYER).definedBy(domainAggregatePackageIdentifiers)

        .layer(APPLICATION_INBOUND_PORT_LAYER).definedBy(applicationInboundPortPackageIdentifiers)
        .optionalLayer(APPLICATION_OUTBOUND_PORT_LAYER).definedBy(applicationOutboundPortPackageIdentifiers)
        .layer(APPLICATION_SERVICE_LAYER).definedBy(applicationServicePackageIdentifiers)

        .layer(ADAPTER_INBOUND_LAYER).definedBy(adapterInboundPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])
        .optionalLayer(ADAPTER_OUTBOUND_LAYER).definedBy(adapterOutboundPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])

        // For now, access to DOMAIN_MODEL_LAYER is forbidden for APPLICATION_INBOUND_PORT_LAYER and ADAPTER_INBOUND_LAYER. Will see how this goes.
        .whereLayer(DOMAIN_MODEL_LAYER)
            .mayOnlyBeAccessedByLayers( DOMAIN_EVENT_LAYER, DOMAIN_COMMAND_LAYER, DOMAIN_AGGREGATE_LAYER, APPLICATION_SERVICE_LAYER, APPLICATION_OUTBOUND_PORT_LAYER, ADAPTER_OUTBOUND_LAYER)
        .whereLayer(DOMAIN_EVENT_LAYER).mayOnlyBeAccessedByLayers(DOMAIN_AGGREGATE_LAYER)
        .whereLayer(DOMAIN_COMMAND_LAYER).mayOnlyBeAccessedByLayers(DOMAIN_AGGREGATE_LAYER, APPLICATION_SERVICE_LAYER)
        .whereLayer(DOMAIN_AGGREGATE_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_SERVICE_LAYER)

        .whereLayer(APPLICATION_INBOUND_PORT_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_SERVICE_LAYER, ADAPTER_INBOUND_LAYER)
        .whereLayer(APPLICATION_OUTBOUND_PORT_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_SERVICE_LAYER, DOMAIN_AGGREGATE_LAYER, ADAPTER_OUTBOUND_LAYER)
        // Everything should go through INBOUND_PORT_LAYER
        .whereLayer(APPLICATION_SERVICE_LAYER).mayNotBeAccessedByAnyLayer()
    // @formatter:on

    adapterMayNotBeAccessedByAnyLayer(layeredArchitecture, adapterInboundPackageIdentifiers, ADAPTER_INBOUND_LAYER)
    adapterMayNotBeAccessedByAnyLayer(layeredArchitecture, adapterOutboundPackageIdentifiers, ADAPTER_OUTBOUND_LAYER)
  }

  @SuppressWarnings("UseCollectMany")
  private void specifyArchitectureProjection(LayeredArchitecture layeredArchitecture) {
    // @formatter:off
    layeredArchitecture
        .optionalLayer(DOMAIN_MODEL_LAYER).definedBy(domainModelPackageIdentifiers)
        .layer(DOMAIN_EVENT_LAYER).definedBy(domainEventPackageIdentifiers)

        .layer(ADAPTER_PROJECTION_LAYER).definedBy(adapterProjectionPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])

        .whereLayer(DOMAIN_MODEL_LAYER).mayOnlyBeAccessedByLayers(DOMAIN_EVENT_LAYER, ADAPTER_PROJECTION_LAYER)
        .whereLayer(DOMAIN_EVENT_LAYER).mayOnlyBeAccessedByLayers(ADAPTER_PROJECTION_LAYER)
    // @formatter:on

    adapterMayNotBeAccessedByAnyLayer(layeredArchitecture, adapterOutboundPackageIdentifiers, ADAPTER_OUTBOUND_LAYER)
    adapterMayNotBeAccessedByAnyLayer(layeredArchitecture, adapterProjectionPackageIdentifiers, ADAPTER_PROJECTION_LAYER)
  }

  @SuppressWarnings("UseCollectMany")
  private void specifyArchitectureQuerySide(LayeredArchitecture layeredArchitecture) {
    // @formatter:off
    layeredArchitecture
        .optionalLayer(DOMAIN_MODEL_LAYER).definedBy(domainModelPackageIdentifiers)

        .layer(APPLICATION_INBOUND_PORT_LAYER).definedBy(applicationInboundPortPackageIdentifiers)
        .optionalLayer(APPLICATION_OUTBOUND_PORT_LAYER).definedBy(applicationOutboundPortPackageIdentifiers)
        .layer(APPLICATION_SERVICE_LAYER).definedBy(applicationServicePackageIdentifiers)

        .layer(ADAPTER_INBOUND_LAYER).definedBy(adapterInboundPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])
        .layer(ADAPTER_OUTBOUND_LAYER).definedBy(adapterOutboundPackageIdentifiers.collect({ Map.Entry<String, String[]> mapEntry -> mapEntry.value }).flatten() as String[])

        .whereLayer(DOMAIN_MODEL_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_SERVICE_LAYER, APPLICATION_OUTBOUND_PORT_LAYER, ADAPTER_OUTBOUND_LAYER)

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
  ArchRule because(String reason) {
    return Factory.withBecause(this, reason)
  }

  @Override
  ArchRule "as"(String newDescription) {
    return copyWith(Optional.of(newDescription))
  }

  @Override
  EvaluationResult evaluate(JavaClasses classes) {
    return layeredArchitectureDelegate().evaluate(classes)
  }

  @SuppressWarnings('DuplicatedCode')
  @Override
  String getDescription() {
    if (overriddenDescription.isPresent()) {
      return overriddenDescription.get()
    }

    List<String> lines = ["Hexagonal architecture (CQRS/ES flavor) consisting of ${ optionalLayers ? " (optional)" : "" }".toString()]

    domainModelPackageIdentifiers ? lines << "domain models ('${ domainModelPackageIdentifiers.join("', '") }')".toString() : "nop"
    domainEventPackageIdentifiers ? lines << "domain events ('${ domainModelPackageIdentifiers.join("', '") }')".toString() : "nop"
    domainCommandPackageIdentifiers ? lines << "domain commands ('${ domainCommandPackageIdentifiers.join("', '") }')".toString() : "nop"
    domainAggregatePackageIdentifiers ? lines << "domain aggregates ('${ domainAggregatePackageIdentifiers.join("', '") }')".toString() : "nop"

    applicationInboundPortPackageIdentifiers ? lines << "application inbound ports ('${ applicationInboundPortPackageIdentifiers.join("', '") }')".toString() : "nop"
    applicationOutboundPortPackageIdentifiers ? lines << "application outbound ports ('${ applicationOutboundPortPackageIdentifiers.join("', '") }')".toString() : "nop"
    applicationServicePackageIdentifiers ? lines << "application services ('${ applicationServicePackageIdentifiers.join("', '") }')".toString() : "nop"

    adapterInboundPackageIdentifiers.each { Map.Entry<String, String[]> mapEntry -> lines << "adapter inbound '${ mapEntry.key }' ('${ mapEntry.value.join("', '") }')".toString() }
    adapterOutboundPackageIdentifiers.each { Map.Entry<String, String[]> mapEntry -> lines << "adapter outbound '${ mapEntry.key }' ('${ mapEntry.value.join("', '") }')".toString() }
    adapterProjectionPackageIdentifiers.each { Map.Entry<String, String[]> mapEntry -> lines << "adapter projection '${ mapEntry.key }' ('${ mapEntry.value.join("', '") }')".toString() }

    return lines.join(lineSeparator())
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
