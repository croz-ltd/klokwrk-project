package net.croz.cargotracker.booking.commandside.api.model

import spock.lang.Specification
import spock.lang.Unroll

class UnLoCodeSpecification extends Specification {

  @Unroll
  def "map constructor should work for correct input params: [code: #codeParameter]"() {
    when:
    UnLoCode unLoCode = new UnLoCode(code: codeParameter)

    then:
    unLoCode.code == codeParameter

    where:
    codeParameter | _
    "HRRJK"       | _
    "HRRJ2"       | _
    "HRRJ9"       | _
  }

  @Unroll
  def "map constructor should fail for invalid input params: [code: #codeParameter]"() {
    when:
    new UnLoCode(code: codeParameter)

    then:
    thrown(IllegalArgumentException)

    where:
    codeParameter | _
    null          | _
    ""            | _
    "   "         | _
    "a"           | _
    "0"           | _
    "hrrjk"       | _
    "HRR0K"       | _
    "HRRJ0"       | _
  }

  def "getCountryCode() should return expected value"() {
    when:
    UnLoCode unLoCode = new UnLoCode(code: "HRRJK")

    then:
    unLoCode.countryCode == "HR"
  }

  def "getLocationCode() should return expected value"() {
    when:
    UnLoCode unLoCode = new UnLoCode(code: "HRRJK")

    then:
    unLoCode.locationCode == "RJK"
  }
}
