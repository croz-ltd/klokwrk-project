package net.croz.cargotracker.booking.domain.model

import net.croz.cargotracker.booking.domain.modelsample.LocationSample
import spock.lang.Specification
import spock.lang.Unroll

class LocationSpecification extends Specification {

  def "map constructor should work for correct input params"() {
    when:
    Location location = new Location(
        unLoCode: new UnLoCode(code: "HRRJK"), name: new InternationalizedName(name: "Rijeka"), countryName: new InternationalizedName(name: "Hrvatska"),
        unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "1234----")
    )

    then:
    location.unLoCode == new UnLoCode(code: "HRRJK")
    location.name == new InternationalizedName(name: "Rijeka")
    location.countryName == new InternationalizedName(name: "Hrvatska")
  }

  @Unroll
  def "map constructor should fail for invalid input params: [code: #codeParameter, name: #nameParameter, countryName: #countryNameParameter, function: #functionParameter]"() {
    when:
    new Location(
        code: new UnLoCode(code: codeParameter), name: new InternationalizedName(name: nameParameter), countryName: new InternationalizedName(name: countryNameParameter),
        unLoCodeFunction: new UnLoCodeFunction(functionParameter)
    )

    then:
    thrown(IllegalArgumentException)

    where:
    codeParameter | nameParameter | countryNameParameter | functionParameter
    null          | "someName"    | "someCountry"        | "0------"
    "HRRJK"       | null          | "someCountry"        | "0------"
    "HRRJK"       | "someName"    | null                 | "0------"
    "HRRJK"       | "someName"    | "someCountry"        | null
  }

  def "create() factory method should work for correct input params"() {
    when:
    Location location = Location.create("HRRJK", "Rijeka", "Hrvatska", "1234----")

    then:
    location.unLoCode == new UnLoCode(code: "HRRJK")
    location.name == new InternationalizedName(name: "Rijeka")
    location.countryName == new InternationalizedName(name: "Hrvatska")
  }

  @Unroll
  def "create() factory method should fail for invalid input params: [code: #codeParameter, name: #nameParameter, countryName: #countryNameParameter, function: #functionParameter]"() {
    when:
    Location.create(codeParameter, nameParameter, countryNameParameter, functionParameter)

    then:
    thrown(IllegalArgumentException)

    where:
    codeParameter | nameParameter | countryNameParameter | functionParameter
    null          | "someName"    | "someCountry"        | "0------"
    "HRRJK"       | null          | "someCountry"        | "0------"
    "HRRJK"       | "someName"    | null                 | "0------"
    "HRRJK"       | "someName"    | "someCountry"        | null
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  def "canAcceptCargoFrom() should return false for same locations"() {
    given:
    Location originLocation = LocationSample.findByUnLoCode("HRRJK")
    Location destinationLocation = LocationSample.findByUnLoCode("HRRJK")

    when:
    Boolean canAccept = destinationLocation.canAcceptCargoFrom(originLocation)

    then:
    canAccept == false
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  def "canAcceptCargoFrom() should return false when origin location is null"() {
    given:
    Location destinationLocation = LocationSample.findByUnLoCode("HRZAD")

    when:
    Boolean canAccept = destinationLocation.canAcceptCargoFrom(null)

    then:
    canAccept == false
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  def "canAcceptCargoFrom() should return true for port locations"() {
    given:
    Location originLocation = LocationSample.findByUnLoCode("HRRJK")
    Location destinationLocation = LocationSample.findByUnLoCode("HRZAD")

    when:
    Boolean canAccept = destinationLocation.canAcceptCargoFrom(originLocation)

    then:
    canAccept == true
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  def "canAcceptCargoFrom() should return true for rail terminal locations"() {
    given:
    Location originLocation = LocationSample.findByUnLoCode("HRRJK")
    Location destinationLocation = LocationSample.findByUnLoCode("HRZAG")

    when:
    Boolean canAccept = destinationLocation.canAcceptCargoFrom(originLocation)

    then:
    canAccept == true
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  def "canAcceptCargoFrom() should return false when locations cannot be connected as ports or as rail terminals"() {
    given:
    Location originLocation = LocationSample.findByUnLoCode("HRKRK")
    Location destinationLocation = LocationSample.findByUnLoCode("HRZAG")

    when:
    Boolean canAccept = destinationLocation.canAcceptCargoFrom(originLocation)

    then:
    canAccept == false
  }
}
