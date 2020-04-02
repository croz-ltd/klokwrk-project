package net.croz.cargotracker.booking.commandside.domain.commandhandler

import net.croz.cargotracker.booking.commandside.api.command.CargoBookCommand
import net.croz.cargotracker.booking.commandside.domain.model.CargoAggregate
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.Repository
import org.springframework.stereotype.Service

@Service
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
