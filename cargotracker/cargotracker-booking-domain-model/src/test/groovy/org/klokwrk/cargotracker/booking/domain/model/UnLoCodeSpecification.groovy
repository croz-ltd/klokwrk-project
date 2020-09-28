package org.klokwrk.cargotracker.booking.domain.model

import spock.lang.Specification

class UnLoCodeSpecification extends Specification {

  void "map constructor should work for correct input params"() {
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

  void "map constructor should fail for invalid input params"() {
    when:
    new UnLoCode(code: codeParameter)

    then:
    thrown(AssertionError)

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

  void "getCountryCode() should return expected value"() {
    when:
    UnLoCode unLoCode = new UnLoCode(code: "HRRJK")

    then:
    unLoCode.countryCode == "HR"
  }

  void "getLocationCode() should return expected value"() {
    when:
    UnLoCode unLoCode = new UnLoCode(code: "HRRJK")

    then:
    unLoCode.locationCode == "RJK"
  }
}
