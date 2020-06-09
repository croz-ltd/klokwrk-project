package org.klokwrk.cargotracker.booking.commandside.domain.aggregate

import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.ResultValidator
import org.axonframework.test.aggregate.TestExecutor
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.CargoBookCommand
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.CargoBookedEvent
import org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler.MessageHandlerTrait
import org.klokwrk.cargotracker.lib.boundary.api.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationCode
import spock.lang.Specification

import static org.klokwrk.cargotracker.booking.domain.modelsample.LocationSample.LOCATION_SAMPLE_MAP

class CargoAggregateSpecification extends Specification {

  AggregateTestFixture aggregateTestFixture

  void setup() {
    aggregateTestFixture = new AggregateTestFixture(CargoAggregate)
    aggregateTestFixture.registerAnnotatedCommandHandler(new CargoAggregateCommandHandlerService(aggregateTestFixture.repository))
  }

  void "should fail for same origin and destination locations"() {
    given:
    String aggregateIdentifier = UUID.randomUUID()
    CargoBookCommand cargoBookCommand = new CargoBookCommand(aggregateIdentifier: aggregateIdentifier, originLocation: LOCATION_SAMPLE_MAP.HRRJK, destinationLocation: LOCATION_SAMPLE_MAP.HRRJK)

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
      detailsException.violationInfo.violationCode.codeAsText == "destinationLocationCannotAcceptCargo"
    }
  }

  void "should fail when origin and destination locations can not connect via rail or water"() {
    given:
    String aggregateIdentifier = UUID.randomUUID()
    CargoBookCommand cargoBookCommand = new CargoBookCommand(aggregateIdentifier: aggregateIdentifier, originLocation: LOCATION_SAMPLE_MAP.HRZAG, destinationLocation: LOCATION_SAMPLE_MAP.HRKRK)

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
      detailsException.violationInfo.violationCode.codeAsText == "destinationLocationCannotAcceptCargo"
    }
  }

  void "should work when origin and destination locations can connect via rail or water"() {
    given:
    String aggregateIdentifier = UUID.randomUUID()
    CargoBookCommand cargoBookCommand = new CargoBookCommand(aggregateIdentifier: aggregateIdentifier, originLocation: LOCATION_SAMPLE_MAP.HRRJK, destinationLocation: LOCATION_SAMPLE_MAP.HRZAG)

    TestExecutor<CargoAggregate> cargoAggregateTestExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<CargoAggregate> cargoAggregateResultValidator = cargoAggregateTestExecutor.when(cargoBookCommand)

    then:
    cargoAggregateResultValidator
        .expectSuccessfulHandlerExecution()
        .expectEvents(new CargoBookedEvent(cargoBookCommand.properties))
  }
}
