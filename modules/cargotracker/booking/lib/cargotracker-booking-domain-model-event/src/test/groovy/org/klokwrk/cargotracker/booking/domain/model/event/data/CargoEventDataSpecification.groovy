/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.domain.model.event.data

import org.klokwrk.cargotracker.booking.domain.model.value.Cargo
import org.klokwrk.cargotracker.booking.domain.model.value.CargoFixtureBuilder
import spock.lang.Specification

class CargoEventDataSpecification extends Specification {
  void 'fromCargo() should work as expected'() {
    given:
    Cargo cargo = cargoParam

    when:
    CargoEventData cargoEventData = CargoEventData.fromCargo(cargo)

    then:
    cargoEventData == cargoEventDataExpectedParam

    where:
    cargoParam                                    | cargoEventDataExpectedParam
    CargoFixtureBuilder.cargo_dry().build()       | CargoEventDataFixtureBuilder.cargo_dry().build()
    CargoFixtureBuilder.cargo_airCooled().build() | CargoEventDataFixtureBuilder.cargo_airCooled().build()
    CargoFixtureBuilder.cargo_chilled().build()   | CargoEventDataFixtureBuilder.cargo_chilled().build()
    CargoFixtureBuilder.cargo_frozen().build()    | CargoEventDataFixtureBuilder.cargo_frozen().build()
  }

  void 'fromCargoCollection() should work as expected'() {
    given:
    Collection<Cargo> cargoCollection = [CargoFixtureBuilder.cargo_dry().build(), CargoFixtureBuilder.cargo_airCooled().build()]

    when:
    Collection<CargoEventData> cargoEventDataCollection = CargoEventData.fromCargoCollection(cargoCollection)

    then:
    cargoEventDataCollection.containsAll([CargoEventDataFixtureBuilder.cargo_dry().build(), CargoEventDataFixtureBuilder.cargo_airCooled().build()])
  }

  void 'toCargo() should work as expected'() {
    given:
    CargoEventData cargoEventData = cargoEventDataParam

    when:
    Cargo cargo = cargoEventData.toCargo()

    then:
    cargo == cargoExpectedParam

    where:
    cargoEventDataParam                                    | cargoExpectedParam
    CargoEventDataFixtureBuilder.cargo_dry().build()       | CargoFixtureBuilder.cargo_dry().build()
    CargoEventDataFixtureBuilder.cargo_airCooled().build() | CargoFixtureBuilder.cargo_airCooled().build()
    CargoEventDataFixtureBuilder.cargo_chilled().build()   | CargoFixtureBuilder.cargo_chilled().build()
    CargoEventDataFixtureBuilder.cargo_frozen().build()    | CargoFixtureBuilder.cargo_frozen().build()
  }

  void 'toCargoCollection() should work as expected'() {
    given:
    Collection<CargoEventData> cargoEventDataCollection = [CargoEventDataFixtureBuilder.cargo_dry().build(), CargoEventDataFixtureBuilder.cargo_airCooled().build()]

    when:
    Collection<Cargo> cargoCollection = CargoEventData.toCargoCollection(cargoEventDataCollection)

    then:
    cargoCollection.containsAll([CargoFixtureBuilder.cargo_dry().build(), CargoFixtureBuilder.cargo_airCooled().build()])
  }
}
