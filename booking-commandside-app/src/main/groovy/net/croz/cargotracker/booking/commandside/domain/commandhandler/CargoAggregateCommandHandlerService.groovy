package net.croz.cargotracker.booking.commandside.domain.commandhandler

import groovy.transform.CompileStatic
import net.croz.cargotracker.booking.api.axon.command.CargoBookCommand
import net.croz.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.Repository
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoAggregateCommandHandlerService {
  private Repository<CargoAggregate> cargoAggregateRepository

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  CargoAggregateCommandHandlerService(Repository<CargoAggregate> cargoAggregateRepository) {
    this.cargoAggregateRepository = cargoAggregateRepository
  }

  @CommandHandler
  CargoAggregate bookCargo(CargoBookCommand cargoBookCommand) {
    Aggregate<CargoAggregate> createdCargoAggregate = cargoAggregateRepository.newInstance({
      return new CargoAggregate(cargoBookCommand.properties)
    })

    CargoAggregate cargoAggregateInstance = createdCargoAggregate.invoke({ CargoAggregate cargoAggregate ->
      cargoAggregate.bookCargo(cargoBookCommand)
    })

    return cargoAggregateInstance
  }
}
