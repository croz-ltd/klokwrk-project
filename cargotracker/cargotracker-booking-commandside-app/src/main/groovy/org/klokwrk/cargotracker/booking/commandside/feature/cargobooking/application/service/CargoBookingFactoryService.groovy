package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.service

import groovy.transform.CompileStatic
import groovy.transform.NullCheck
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.command.BookCargoCommand
import org.klokwrk.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoRequest
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoResponse
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.out.FindLocationPortOut
import org.klokwrk.cargotracker.booking.domain.model.Location
import org.springframework.stereotype.Service

/**
 * Convenient factory intended to be used by {@link CargoBookingApplicationService} for encapsulating and simplifying creation of objects required in {@link CargoBookingApplicationService}
 * implementation.
 */
@Service
@CompileStatic
@NullCheck
class CargoBookingFactoryService {
  private final FindLocationPortOut findLocationPortOut

  CargoBookingFactoryService(FindLocationPortOut findLocationPortOut) {
    this.findLocationPortOut = findLocationPortOut
  }

  /**
   * Creates {@link BookCargoCommand} from supplied {@link org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoRequest} instance.
   */
  BookCargoCommand createBookCargoCommand(BookCargoRequest bookCargoRequest) {
    String aggregateIdentifier = bookCargoRequest.aggregateIdentifier ?: UUID.randomUUID().toString()
    assert UUID.fromString(aggregateIdentifier)

    // NOTE: While creating a command, we also need to resolve all required data from external services if needed. In this example, we are resolving registry data (a.k.a. master data) from the
    //       outbound adapter.
    //       Data and objects comprising a command should be in their fully valid state and it is the best when involved data pieces are immutable objects.
    Location originLocation = findLocationPortOut.findByUnLoCode(bookCargoRequest.originLocation)
    // TODO dmurat: validation - replace with second level validation producing an exception that can be translated into meaningful response for the user.
    assert originLocation != Location.UNKNOWN_LOCATION

    Location destinationLocation = findLocationPortOut.findByUnLoCode(bookCargoRequest.destinationLocation)
    // TODO dmurat: validation - replace with second level validation producing an exception that can be translated into meaningful response for the user.
    assert destinationLocation != Location.UNKNOWN_LOCATION

    BookCargoCommand bookCargoCommand = new BookCargoCommand(aggregateIdentifier: aggregateIdentifier, originLocation: originLocation, destinationLocation: destinationLocation)
    return bookCargoCommand
  }

  /**
   * Creates {@link org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoResponse} from supplied {@link CargoAggregate} instance.
   */
  BookCargoResponse createBookCargoResponse(CargoAggregate cargoAggregate) {
    Map<String, ?> originLocationMap = createMapFromLocation(cargoAggregate.originLocation)
    Map<String, ?> destinationLocationMap = createMapFromLocation(cargoAggregate.destinationLocation)

    BookCargoResponse bookCargoResponse = new BookCargoResponse(aggregateIdentifier: cargoAggregate.aggregateIdentifier, originLocation: originLocationMap, destinationLocation: destinationLocationMap)
    return bookCargoResponse
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
