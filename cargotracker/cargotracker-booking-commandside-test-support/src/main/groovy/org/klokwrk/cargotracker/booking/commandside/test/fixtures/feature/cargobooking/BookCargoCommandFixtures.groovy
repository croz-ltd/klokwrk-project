package org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.cargobooking

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.command.BookCargoCommand
import org.klokwrk.cargotracker.booking.domain.model.Location

/**
 * Contains test data fixtures for {@link BookCargoCommand}.
 */
@CompileStatic
class BookCargoCommandFixtures {
  static final Map<String, Location> LOCATION_SAMPLE_MAP = [
      "HRKRK": Location.create("HRKRK", "Krk", "Hrvatska", "1-3-----"),
      "HRRJK": Location.create("HRRJK", "Rijeka", "Hrvatska", "1234----"),
      "HRZAG": Location.create("HRZAG", "Zagreb", "Hrvatska", "-2345---")
  ]

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
