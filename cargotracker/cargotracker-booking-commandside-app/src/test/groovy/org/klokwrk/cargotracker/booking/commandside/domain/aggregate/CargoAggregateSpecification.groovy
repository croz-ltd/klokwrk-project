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
package org.klokwrk.cargotracker.booking.commandside.domain.aggregate

import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.ResultValidator
import org.axonframework.test.aggregate.TestExecutor
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.cargobooking.BookCargoCommandFixtures
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.cargobooking.CargoBookedEventFixtures
import org.klokwrk.cargotracker.booking.domain.model.command.BookCargoCommand
import org.klokwrk.cargotracker.lib.boundary.api.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationCode
import spock.lang.Specification

class CargoAggregateSpecification extends Specification {

  AggregateTestFixture aggregateTestFixture

  void setup() {
    aggregateTestFixture = new AggregateTestFixture(CargoAggregate)
  }

  void "should fail for same origin and destination locations"() {
    given:
    BookCargoCommand bookCargoCommand = BookCargoCommandFixtures.commandInvalidWithEqualOriginAndDestinationLocations()
    TestExecutor<CargoAggregate> cargoAggregateTestExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<CargoAggregate> cargoAggregateResultValidator = cargoAggregateTestExecutor.when(bookCargoCommand)
    CommandException actualException = cargoAggregateResultValidator.actualException

    then:
    verifyAll {
      cargoAggregateResultValidator.expectException(CommandException)
      actualException.cause == null

      actualException.violationInfo.violationCode.code == ViolationCode.BAD_REQUEST.code
      actualException.violationInfo.violationCode.codeKey == CargoAggregate.VIOLATION_DESTINATION_LOCATION_CANNOT_ACCEPT_CARGO
    }
  }

  void "should fail when origin and destination locations can not connect via rail or water"() {
    given:
    BookCargoCommand bookCargoCommand = BookCargoCommandFixtures.commandInvalidWithNotConnectedLocations()
    TestExecutor<CargoAggregate> cargoAggregateTestExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<CargoAggregate> cargoAggregateResultValidator = cargoAggregateTestExecutor.when(bookCargoCommand)
    CommandException actualException = cargoAggregateResultValidator.actualException

    then:
    verifyAll {
      cargoAggregateResultValidator.expectException(CommandException)
      actualException.cause == null

      actualException.violationInfo.violationCode.code == ViolationCode.BAD_REQUEST.code
      actualException.violationInfo.violationCode.codeKey == CargoAggregate.VIOLATION_DESTINATION_LOCATION_CANNOT_ACCEPT_CARGO
    }
  }

  void "should work when origin and destination locations can connect via rail or water"() {
    given:
    BookCargoCommand bookCargoCommand = BookCargoCommandFixtures.commandValidConnectedViaRail()
    TestExecutor<CargoAggregate> cargoAggregateTestExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<CargoAggregate> cargoAggregateResultValidator = cargoAggregateTestExecutor.when(bookCargoCommand)

    then:
    cargoAggregateResultValidator
        .expectSuccessfulHandlerExecution()
        .expectEvents(CargoBookedEventFixtures.eventValidForCommand(bookCargoCommand))
  }
}
