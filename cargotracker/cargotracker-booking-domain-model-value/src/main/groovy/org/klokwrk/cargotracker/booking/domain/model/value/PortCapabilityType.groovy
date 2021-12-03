package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic

/**
 * Enumeration representing a capability (or a feature) of a port.
 */
@CompileStatic
enum PortCapabilityType {
  /**
   * Not a port.
   */
  NO_PORT,

  /**
   * Port at a sea, or closely connected to the sea.
   */
  SEA_PORT,

  /**
   * Port at the river (implies not a sea port).
   */
  RIVER_PORT,

  /**
   * Port with a container terminal.
   */
  CONTAINER_PORT,

  /**
   * Port with a bulk cargo terminal.
   */
  BULK_CARGO_PORT,

  /**
   * Port with an oil terminal.
   */
  OIL_PORT,

  /**
   * Port dealing with human passengers.
   */
  PASSENGER_PORT
}
