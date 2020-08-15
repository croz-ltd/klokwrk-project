package org.klokwrk.cargotracker.booking.commandside.domain.aggregate

import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.ResultValidator
import org.axonframework.test.aggregate.TestExecutor
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.CargoBookCommand
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.cargobook.CargoBookCommandFixtures
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.cargobook.CargoBookedEventFixtures
import org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler.MessageHandlerTrait
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
    CargoBookCommand cargoBookCommand = CargoBookCommandFixtures.commandInvalidWithSameOriginAndLocation()
    TestExecutor<CargoAggregate> cargoAggregateTestExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<CargoAggregate> cargoAggregateResultValidator = cargoAggregateTestExecutor.when(cargoBookCommand)

    CommandExecutionException actualException = cargoAggregateResultValidator.actualException
    CommandException detailsException = actualException.details.get() as CommandException

    then:
    verifyAll {
      cargoAggregateResultValidator.expectException(CommandExecutionException)
      actualException.cause instanceof MessageHandlerTrait.ThrowAwayRuntimeException

      detailsException.violationInfo.violationCode.code == ViolationCode.BAD_REQUEST.code
      detailsException.violationInfo.violationCode.codeAsText == CargoAggregate.VIOLATION_DESTINATION_LOCATION_CANNOT_ACCEPT_CARGO
    }
  }

  void "should fail when origin and destination locations can not connect via rail or water"() {
    given:
    CargoBookCommand cargoBookCommand = CargoBookCommandFixtures.commandInvalidWithNotConnectedLocations()
    TestExecutor<CargoAggregate> cargoAggregateTestExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<CargoAggregate> cargoAggregateResultValidator = cargoAggregateTestExecutor.when(cargoBookCommand)

    CommandExecutionException actualException = cargoAggregateResultValidator.actualException
    CommandException detailsException = actualException.details.get() as CommandException

    then:
    verifyAll {
      cargoAggregateResultValidator.expectException(CommandExecutionException)
      actualException.cause instanceof MessageHandlerTrait.ThrowAwayRuntimeException

      detailsException.violationInfo.violationCode.code == ViolationCode.BAD_REQUEST.code
      detailsException.violationInfo.violationCode.codeAsText == CargoAggregate.VIOLATION_DESTINATION_LOCATION_CANNOT_ACCEPT_CARGO
    }
  }

  void "should work when origin and destination locations can connect via rail or water"() {
    given:
    CargoBookCommand cargoBookCommand = CargoBookCommandFixtures.commandValidConnectedViaRail()
    TestExecutor<CargoAggregate> cargoAggregateTestExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<CargoAggregate> cargoAggregateResultValidator = cargoAggregateTestExecutor.when(cargoBookCommand)

    then:
    cargoAggregateResultValidator
        .expectSuccessfulHandlerExecution()
        .expectEvents(CargoBookedEventFixtures.eventValidForCommand(cargoBookCommand))
  }
}
