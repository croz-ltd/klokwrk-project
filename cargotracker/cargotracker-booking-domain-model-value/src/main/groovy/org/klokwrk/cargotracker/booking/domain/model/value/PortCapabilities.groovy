package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import static org.hamcrest.Matchers.empty
import static org.hamcrest.Matchers.everyItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue

/**
 * Represents a set of port capabilities implemented as a set of {@link PortCapabilityType} values.
 * <p/>
 * The main reason for introducing this class is because {@code UnLoCodeFunction} does not provide this information.
 */
@KwrkImmutable
@CompileStatic
class PortCapabilities implements PostMapConstructorCheckable {
  static final PortCapabilities NO_PORT_CAPABILITIES = new PortCapabilities(capabilities: [PortCapabilityType.NO_PORT] as Set)
  static final PortCapabilities SEA_PORT_CAPABILITIES = new PortCapabilities(capabilities: [PortCapabilityType.SEA_PORT] as Set)
  static final PortCapabilities RIVER_PORT_CAPABILITIES = new PortCapabilities(capabilities: [PortCapabilityType.RIVER_PORT] as Set)
  static final PortCapabilities SEA_CONTAINER_PORT_CAPABILITIES = new PortCapabilities(capabilities: [PortCapabilityType.SEA_PORT, PortCapabilityType.CONTAINER_PORT] as Set)

  Set<PortCapabilityType> capabilities

  /**
   * Creates a new {@code PortCapabilities} corresponding to the location which is not a port.
   */
  static PortCapabilities createNoPortCapabilities() {
    return new PortCapabilities(capabilities: [PortCapabilityType.NO_PORT] as Set)
  }

  /**
   * Creates a new {@code PortCapabilities} instance for sea port.
   */
  static PortCapabilities createSeaPortCapabilities() {
    return new PortCapabilities(capabilities: [PortCapabilityType.SEA_PORT] as Set)
  }

  /**
   * Creates a new {@code PortCapabilities} instance for river port.
   */
  static PortCapabilities createRiverPortCapabilities() {
    return new PortCapabilities(capabilities: [PortCapabilityType.RIVER_PORT] as Set)
  }

  /**
   * Creates a new {@code PortCapabilities} instance for sea port with container terminal.
   */
  static PortCapabilities createSeaContainerPortCapabilities() {
    return new PortCapabilities(capabilities: [PortCapabilityType.SEA_PORT, PortCapabilityType.CONTAINER_PORT] as Set)
  }

  /**
   * Tries to create {@code PortCapabilities} instance from a list of strings corresponding to {@code PortCapabilityType} enum names.
   * <p/>
   * If all supplied strings correspond to valid enum names, valid {@code PortCapabilities} instance will be returned. Otherwise, method returns a {@code null}.
   * <p/>
   * When comparing supplied names with names of an enum, supplied names are trimmed and uppercased before comparison.
   */
  static PortCapabilities createFromStringsIfPossible(List<String> portCapabilitiesStringList) {
    if (portCapabilitiesStringList == null || portCapabilitiesStringList.isEmpty()) {
      return null
    }

    if (portCapabilitiesStringList.any({ String aString -> aString == null || aString.trim().isEmpty() })) {
      return null
    }

    List<String> enumNameList = PortCapabilityType.values().collect { PortCapabilityType portCapabilityType -> portCapabilityType.name() }
    if (!enumNameList.containsAll(portCapabilitiesStringList.each({ String aString -> aString.trim().toUpperCase() }))) {
      return null
    }

    Set<PortCapabilityType> portCapabilityTypes = portCapabilitiesStringList.collect({ String aString -> PortCapabilityType.valueOf(aString) }) as Set
    return new PortCapabilities(capabilities: portCapabilityTypes)
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(capabilities, notNullValue())
    requireMatch(capabilities, not(empty()))
    requireMatch(capabilities, everyItem(instanceOf(PortCapabilityType)))
  }

  Boolean isPort() {
    return !capabilities.contains(PortCapabilityType.NO_PORT)
  }

  Boolean isSeaPort() {
    return isPort() && capabilities.contains(PortCapabilityType.SEA_PORT)
  }

  Boolean isRiverPort() {
    return isPort() && capabilities.contains(PortCapabilityType.RIVER_PORT)
  }

  Boolean isContainerPort() {
    return isPort() && capabilities.contains(PortCapabilityType.CONTAINER_PORT)
  }

  Boolean isSeaContainerPort() {
    return isPort() && isSeaPort() && isContainerPort()
  }
}
