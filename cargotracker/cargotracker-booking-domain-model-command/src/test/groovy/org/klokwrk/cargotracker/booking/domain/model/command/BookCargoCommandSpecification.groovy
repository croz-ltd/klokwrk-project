package org.klokwrk.cargotracker.booking.domain.model.command

import org.klokwrk.cargotracker.booking.domain.model.value.CargoId
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.lib.boundary.api.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import org.klokwrk.lang.groovy.misc.UUIDUtils
import spock.lang.Specification

class BookCargoCommandSpecification extends Specification {
  static Map<String, Location> locationSampleMap = [
      "NLRTM": Location.create("NLRTM", "Rotterdam", "Netherlands", "12345---", "5155N 00430E"),
      "DEHAM": Location.create("DEHAM", "Hamburg", "Germany", "12345---", "5331N 00956E")
  ]

  void "map constructor should work for correct input params"() {
    when:
    CargoId cargoId = CargoId.createWithGeneratedIdentifier()
    BookCargoCommand bookCargoCommand = new BookCargoCommand(cargoId: cargoId, originLocation: locationSampleMap["NLRTM"], destinationLocation: locationSampleMap["DEHAM"])

    then:
    bookCargoCommand.cargoId
    UUIDUtils.checkIfRandomUuid(bookCargoCommand.cargoId.identifier)

    bookCargoCommand.originLocation.unLoCode.code == "NLRTM"
    bookCargoCommand.destinationLocation.unLoCode.code == "DEHAM"
  }

  void "map constructor should fail for invalid formats of input params"() {
    when:
    new BookCargoCommand(cargoId: cargoIdParam, originLocation: originLocationParam, destinationLocation: destinationLocationParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messagePartParam)

    where:
    cargoIdParam                            | originLocationParam        | destinationLocationParam   | messagePartParam
    null                                    | locationSampleMap["NLRTM"] | locationSampleMap["DEHAM"] | "notNullValue"
    CargoId.createWithGeneratedIdentifier() | null                       | locationSampleMap["DEHAM"] | "notNullValue"
    CargoId.createWithGeneratedIdentifier() | locationSampleMap["NLRTM"] | null                       | "notNullValue"
  }

  void "map constructor should fail for failing business rules"() {
    when:
    new BookCargoCommand(cargoId: cargoIdParam, originLocation: originLocationParam, destinationLocation: destinationLocationParam)

    then:
    CommandException commandException = thrown()
    commandException.violationInfo.severity == Severity.WARNING
    commandException.violationInfo.violationCode.code == "400"
    commandException.violationInfo.violationCode.codeMessage == "Bad Request"
    commandException.violationInfo.violationCode.codeKey == violationCodeKeyParam

    where:
    cargoIdParam                            | originLocationParam        | destinationLocationParam   | violationCodeKeyParam
    CargoId.createWithGeneratedIdentifier() | Location.UNKNOWN_LOCATION  | locationSampleMap["DEHAM"] | "originLocationUnknown"
    CargoId.createWithGeneratedIdentifier() | locationSampleMap["NLRTM"] | Location.UNKNOWN_LOCATION  | "destinationLocationUnknown"
    CargoId.createWithGeneratedIdentifier() | locationSampleMap["NLRTM"] | locationSampleMap["NLRTM"] | "originLocationEqualToDestinationLocation"
  }
}
