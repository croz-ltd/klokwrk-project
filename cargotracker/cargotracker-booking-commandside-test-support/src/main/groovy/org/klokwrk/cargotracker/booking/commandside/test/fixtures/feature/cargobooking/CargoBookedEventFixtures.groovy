/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.cargobooking

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.command.BookCargoCommand
import org.klokwrk.cargotracker.booking.domain.model.event.CargoBookedEvent

/**
 * Contains test data fixtures for {@link CargoBookedEvent}.
 */
@CompileStatic
class CargoBookedEventFixtures {
  /**
   * Creates valid event where origin and destination locations are connected via rail.
   */
  static CargoBookedEvent eventValidConnectedViaRail(String cargoIdentifier = UUID.randomUUID()) {
    BookCargoCommand bookCargoCommand = BookCargoCommandFixtures.commandValidConnectedViaRail(cargoIdentifier)
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
