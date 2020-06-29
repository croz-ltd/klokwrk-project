package org.klokwrk.cargotracker.booking.commandside.test.fixtures.cargobook

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.CargoBookCommand
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.CargoBookedEvent

/**
 * Contains test data fixtures for {@link CargoBookedEvent}.
 */
@CompileStatic
class CargoBookedEventFixtures {
  /**
   * Creates valid event where origin and destination locations are connected via rail.
   */
  static CargoBookedEvent eventValidConnectedViaRail(String aggregateIdentifier = UUID.randomUUID()) {
    CargoBookCommand cargoBookCommand = CargoBookCommandFixtures.commandValidConnectedViaRail(aggregateIdentifier)
    CargoBookedEvent cargoBookedEvent = eventValidForCommand(cargoBookCommand)
    return cargoBookedEvent
  }

  /**
   * Creates event from given command.
   * <p/>
   * Used implementation is the same as when aggregate creates event from command.
   */
  static CargoBookedEvent eventValidForCommand(CargoBookCommand cargoBookCommand) {
    CargoBookedEvent cargoBookedEvent = new CargoBookedEvent(cargoBookCommand.properties)
    return cargoBookedEvent
  }
}
