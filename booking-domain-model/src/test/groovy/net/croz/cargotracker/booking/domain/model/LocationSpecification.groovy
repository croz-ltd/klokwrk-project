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

  @Unroll
  def "destinationLocation.canAcceptCargoFrom() should work as expected: [origin: #originDescription, destination: #destinationDescription]"() {
    when:
    Location originLocation = originLocationInstance
    Location destinationLocation = destinationLocationInstance

    then:
    destinationLocation.canAcceptCargoFrom(originLocation) == destinationCanAccept

    where:
    originLocationInstance                 | destinationLocationInstance            | destinationCanAccept | originDescription              | destinationDescription
    LocationSample.findByUnLoCode("HRRJK") | LocationSample.findByUnLoCode("HRRJK") | false                | "any"                          | "same as origin"
    null                                   | LocationSample.findByUnLoCode("HRRJK") | false                | "null"                         | "any"
    LocationSample.findByUnLoCode("HRZAD") | LocationSample.findByUnLoCode("HRRJK") | true                 | "port & rail terminal"         | "port & rail terminal"
    LocationSample.findByUnLoCode("HRZAD") | LocationSample.findByUnLoCode("HRKRK") | true                 | "port & rail terminal"         | "port"
    LocationSample.findByUnLoCode("HRKRK") | LocationSample.findByUnLoCode("HRZAD") | true                 | "port"                         | "port & rail terminal"
    LocationSample.findByUnLoCode("HRZAG") | LocationSample.findByUnLoCode("HRZAD") | true                 | "rail terminal"                | "port & rail terminal"
    LocationSample.findByUnLoCode("HRZAG") | LocationSample.findByUnLoCode("HRVZN") | true                 | "rail terminal"                | "rail terminal"
    LocationSample.findByUnLoCode("HRZAG") | LocationSample.findByUnLoCode("HRKRK") | false                | "rail terminal"                | "port"
    LocationSample.findByUnLoCode("HRKRK") | LocationSample.findByUnLoCode("HRZAG") | false                | "port"                         | "rail terminal"
    LocationSample.findByUnLoCode("HRDKO") | LocationSample.findByUnLoCode("HRZAG") | false                | "not port & not rail terminal" | "rail terminal"
    LocationSample.findByUnLoCode("HRZAG") | LocationSample.findByUnLoCode("HRDKO") | false                | "rail terminal"                | "not port & not rail terminal"
    LocationSample.findByUnLoCode("HRMVN") | LocationSample.findByUnLoCode("HRDKO") | false                | "not port & not rail terminal" | "not port & not rail terminal"
  }
}
