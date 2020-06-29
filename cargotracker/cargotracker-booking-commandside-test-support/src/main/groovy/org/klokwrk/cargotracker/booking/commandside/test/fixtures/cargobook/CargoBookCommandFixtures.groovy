package org.klokwrk.cargotracker.booking.commandside.test.fixtures.cargobook

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.CargoBookCommand

import static org.klokwrk.cargotracker.booking.domain.modelsample.LocationSample.LOCATION_SAMPLE_MAP

/**
 * Contains test data fixtures for {@link CargoBookCommand}.
 */
@CompileStatic
class CargoBookCommandFixtures {
  /**
   * Creates invalid command where origin and destination location are same.
   */
  static CargoBookCommand commandInvalidWithSameOriginAndLocation(String aggregateIdentifier = UUID.randomUUID()) {
    CargoBookCommand cargoBookCommand = new CargoBookCommand(aggregateIdentifier: aggregateIdentifier, originLocation: LOCATION_SAMPLE_MAP.HRRJK, destinationLocation: LOCATION_SAMPLE_MAP.HRRJK)
    return cargoBookCommand
  }

  /**
   * Creates invalid command where origin and destination location are not connected.
   */
  static CargoBookCommand commandInvalidWithNotConnectedLocations(String aggregateIdentifier = UUID.randomUUID()) {
    CargoBookCommand cargoBookCommand = new CargoBookCommand(aggregateIdentifier: aggregateIdentifier, originLocation: LOCATION_SAMPLE_MAP.HRZAG, destinationLocation: LOCATION_SAMPLE_MAP.HRKRK)
    return cargoBookCommand
  }

  /**
   * Creates valid command where origin and destination locations are connected via rail.
   */
  static CargoBookCommand commandValidConnectedViaRail(String aggregateIdentifier = UUID.randomUUID()) {
    CargoBookCommand cargoBookCommand = new CargoBookCommand(aggregateIdentifier: aggregateIdentifier, originLocation: LOCATION_SAMPLE_MAP.HRRJK, destinationLocation: LOCATION_SAMPLE_MAP.HRZAG)
    return cargoBookCommand
  }
}
