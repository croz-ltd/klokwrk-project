package org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.cargobooking

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.command.BookCargoCommand
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.event.CargoBookedEvent

/**
 * Contains test data fixtures for {@link CargoBookedEvent}.
 */
@CompileStatic
class CargoBookedEventFixtures {
  /**
   * Creates valid event where origin and destination locations are connected via rail.
   */
  static CargoBookedEvent eventValidConnectedViaRail(String aggregateIdentifier = UUID.randomUUID()) {
    BookCargoCommand bookCargoCommand = BookCargoCommandFixtures.commandValidConnectedViaRail(aggregateIdentifier)
    CargoBookedEvent cargoBookedEvent = eventValidForCommand(bookCargoCommand)
    return cargoBookedEvent
  }

  /**
   * Creates event from given command.
   * <p/>
   * Used implementation is the same as when aggregate creates event from command.
   */
  static CargoBookedEvent eventValidForCommand(BookCargoCommand bookCargoCommand) {
    CargoBookedEvent cargoBookedEvent = new CargoBookedEvent(bookCargoCommand.properties)
    return cargoBookedEvent
  }
}
