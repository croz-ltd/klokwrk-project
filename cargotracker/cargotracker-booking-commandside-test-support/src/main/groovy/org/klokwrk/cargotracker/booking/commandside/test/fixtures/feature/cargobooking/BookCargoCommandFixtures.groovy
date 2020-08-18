package org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.cargobooking

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.command.BookCargoCommand

import static org.klokwrk.cargotracker.booking.domain.modelsample.LocationSample.LOCATION_SAMPLE_MAP

/**
 * Contains test data fixtures for {@link BookCargoCommand}.
 */
@CompileStatic
class BookCargoCommandFixtures {
  /**
   * Creates invalid command where origin and destination location are same.
   */
  static BookCargoCommand commandInvalidWithSameOriginAndLocation(String aggregateIdentifier = UUID.randomUUID()) {
    BookCargoCommand bookCargoCommand = new BookCargoCommand(aggregateIdentifier: aggregateIdentifier, originLocation: LOCATION_SAMPLE_MAP.HRRJK, destinationLocation: LOCATION_SAMPLE_MAP.HRRJK)
    return bookCargoCommand
  }

  /**
   * Creates invalid command where origin and destination location are not connected.
   */
  static BookCargoCommand commandInvalidWithNotConnectedLocations(String aggregateIdentifier = UUID.randomUUID()) {
    BookCargoCommand bookCargoCommand = new BookCargoCommand(aggregateIdentifier: aggregateIdentifier, originLocation: LOCATION_SAMPLE_MAP.HRZAG, destinationLocation: LOCATION_SAMPLE_MAP.HRKRK)
    return bookCargoCommand
  }

  /**
   * Creates valid command where origin and destination locations are connected via rail.
   */
  static BookCargoCommand commandValidConnectedViaRail(String aggregateIdentifier = UUID.randomUUID()) {
    BookCargoCommand bookCargoCommand = new BookCargoCommand(aggregateIdentifier: aggregateIdentifier, originLocation: LOCATION_SAMPLE_MAP.HRRJK, destinationLocation: LOCATION_SAMPLE_MAP.HRZAG)
    return bookCargoCommand
  }
}
