package org.klokwrk.cargotracker.booking.commandside.domain.commandhandler

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.Repository
import org.klokwrk.cargotracker.booking.axon.api.command.CargoBookCommand
import org.klokwrk.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoAggregateCommandHandlerService {
  private final Repository<CargoAggregate> cargoAggregateRepository

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  CargoAggregateCommandHandlerService(Repository<CargoAggregate> cargoAggregateRepository) {
    this.cargoAggregateRepository = cargoAggregateRepository
  }

  @CommandHandler
  CargoAggregate bookCargo(CargoBookCommand cargoBookCommand, CommandMessage commandMessage) {
    Aggregate<CargoAggregate> createdCargoAggregate = cargoAggregateRepository.newInstance({
      return new CargoAggregate(cargoBookCommand.properties)
    })

    CargoAggregate cargoAggregateInstance = createdCargoAggregate.invoke({ CargoAggregate cargoAggregate ->
      cargoAggregate.bookCargo(cargoBookCommand, commandMessage.metaData)
    }) as CargoAggregate

    return cargoAggregateInstance
  }
}
