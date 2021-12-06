package org.klokwrk.cargotracker.booking.domain.model.command

import org.klokwrk.cargotracker.booking.domain.model.value.CargoId
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.booking.domain.model.value.PortCapabilities
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification
import org.klokwrk.cargotracker.lib.boundary.api.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import org.klokwrk.lang.groovy.misc.UUIDUtils
import spock.lang.Specification

class BookCargoCommandSpecification extends Specification {
  static Map<String, Location> locationSampleMap = [
      "NLRTM": Location.create("NLRTM", "Rotterdam", "Netherlands", "12345---", "5155N 00430E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
      "DEHAM": Location.create("DEHAM", "Hamburg", "Germany", "12345---", "5331N 00956E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
      "HRZAG": Location.create("HRZAG", "Zagreb", "Croatia", "-2345---", "4548N 01600E"),
  ]

  void "map constructor should work for correct input params"() {
    when:
    CargoId cargoId = CargoId.createWithGeneratedIdentifier()
    BookCargoCommand bookCargoCommand = new BookCargoCommand(
        cargoId: cargoId,
        routeSpecification: new RouteSpecification(originLocation: locationSampleMap["NLRTM"], destinationLocation: locationSampleMap["DEHAM"])
    )

    then:
    bookCargoCommand.cargoId
    UUIDUtils.checkIfRandomUuid(bookCargoCommand.cargoId.identifier)

    bookCargoCommand.routeSpecification.originLocation.unLoCode.code == "NLRTM"
    bookCargoCommand.routeSpecification.destinationLocation.unLoCode.code == "DEHAM"
  }

  void "map constructor should fail for invalid input params"() {
    when:
    new BookCargoCommand(cargoId: cargoIdParam, routeSpecification: routeSpecificationParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messagePartParam)

    where:
    cargoIdParam                            | routeSpecificationParam                                                           | messagePartParam
    null                                    | RouteSpecification.create(locationSampleMap["NLRTM"], locationSampleMap["DEHAM"]) | "notNullValue"
    CargoId.createWithGeneratedIdentifier() | null                                                                              | "notNullValue"
  }

  void "map constructor should fail for failing business rules"() {
    when:
    new BookCargoCommand(cargoId: cargoIdParam, routeSpecification: new RouteSpecification(originLocation: originLocationParam, destinationLocation: destinationLocationParam))

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.severity == Severity.WARNING
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeMessage == "Bad Request"
    domainException.violationInfo.violationCode.codeKey == violationCodeKeyParam

    where:
    cargoIdParam                            | originLocationParam        | destinationLocationParam   | violationCodeKeyParam
    CargoId.createWithGeneratedIdentifier() | locationSampleMap["NLRTM"] | locationSampleMap["NLRTM"] | "routeSpecification.originAndDestinationLocationAreEqual"
    CargoId.createWithGeneratedIdentifier() | locationSampleMap["NLRTM"] | locationSampleMap["HRZAG"] | "routeSpecification.cannotRouteCargoFromOriginToDestination"
  }
}
