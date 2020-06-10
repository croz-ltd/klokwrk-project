package org.klokwrk.cargotracker.booking.commandside.cargobook.domain.facade

import groovy.transform.CompileStatic
import groovy.transform.NullCheck
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.CargoBookCommand
import org.klokwrk.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.klokwrk.cargotracker.booking.commandside.domain.repository.LocationRegistryRepositoryService
import org.klokwrk.cargotracker.booking.domain.model.Location
import org.springframework.stereotype.Service

/**
 * Convenient factory intended to be used by {@link CargoBookFacadeService} for encapsulating and simplifying creation of objects required in {@link CargoBookFacadeService} implementation.
 */
@Service
@CompileStatic
@NullCheck
class CargoBookFactoryService {
  private final LocationRegistryRepositoryService locationRegistryRepositoryService

  CargoBookFactoryService(LocationRegistryRepositoryService locationRegistryRepositoryService) {
    this.locationRegistryRepositoryService = locationRegistryRepositoryService
  }

  /**
   * Creates {@link CargoBookCommand} from supplied {@link CargoBookRequest} instance.
   */
  CargoBookCommand createCargoBookCommand(CargoBookRequest request) {
    String aggregateIdentifier = request.aggregateIdentifier ?: UUID.randomUUID().toString()
    assert UUID.fromString(aggregateIdentifier)

    // NOTE: While creating a command, we also need to resolve all required data from external services if needed. In this example, we are resolving registry data (a.k.a. master data) from the
    //       external registry service.
    //       Data and objects comprising a command should be in their fully valid state and it is the best when involved data pieces are immutable objects.
    Location originLocation = locationRegistryRepositoryService.findByUnLoCode(request.originLocation)
    // TODO dmurat: validation - replace with second level validation producing an exception that can be translated into meaningful response for the user.
    assert originLocation != Location.UNKNOWN_LOCATION

    Location destinationLocation = locationRegistryRepositoryService.findByUnLoCode(request.destinationLocation)
    // TODO dmurat: validation - replace with second level validation producing an exception that can be translated into meaningful response for the user.
    assert destinationLocation != Location.UNKNOWN_LOCATION

    CargoBookCommand cargoBookCommand = new CargoBookCommand(aggregateIdentifier: aggregateIdentifier, originLocation: originLocation, destinationLocation: destinationLocation)
    return cargoBookCommand
  }

  /**
   * Creates {@link CargoBookResponse} from supplied {@link CargoAggregate} instance.
   */
  CargoBookResponse createCargoBookResponse(CargoAggregate cargoAggregate) {
    Map<String, ?> originLocationMap = createMapFromLocation(cargoAggregate.originLocation)
    Map<String, ?> destinationLocationMap = createMapFromLocation(cargoAggregate.destinationLocation)

    CargoBookResponse cargoBookResponse = new CargoBookResponse(aggregateIdentifier: cargoAggregate.aggregateIdentifier, originLocation: originLocationMap, destinationLocation: destinationLocationMap)
    return cargoBookResponse
  }

  /**
   * Creates, or "render" a map from {@link Location} instance.
   */
  Map<String, ?> createMapFromLocation(Location location) {
    Map<String, ?> renderedMap = [
        name: location.name.name,
        nameInternationalized: location.name.nameInternationalized,
        country: [
            name: location.countryName.name,
            nameInternationalized: location.countryName.nameInternationalized
        ],
        unLoCode: [
            code: location.unLoCode.code,
            countryCode: location.unLoCode.countryCode,
            locationCode: location.unLoCode.locationCode
        ]
    ]

    return renderedMap
  }
}
