package net.croz.cargotracker.booking.domain.model

import spock.lang.Specification
import spock.lang.Unroll

class LocationSpecification extends Specification {

  def "map constructor should work for correct input params"() {
    when:
    Location location = new Location(unLoCode: new UnLoCode(code: "HRRJK"), name: new InternationalizedName(name: "Rijeka"), countryName: new InternationalizedName(name: "Hrvatska"))

    then:
    location.unLoCode == new UnLoCode(code: "HRRJK")
    location.name == new InternationalizedName(name: "Rijeka")
    location.countryName == new InternationalizedName(name: "Hrvatska")
  }

  @Unroll
  def "map constructor should fail for invalid input params: [code: #codeParameter, name: #nameParameter, countryName: #countryNameParameter]"() {
    when:
    new Location(code: new UnLoCode(code: codeParameter), name: new InternationalizedName(name: nameParameter), countryName: new InternationalizedName(name: countryNameParameter))

    then:
    thrown(IllegalArgumentException)

    where:
    codeParameter | nameParameter | countryNameParameter
    null          | "someName"    | "someCountry"
    "HRRJK"       | null          | "someCountry"
    "HRRJK"       | "someName"    | null
  }
}
