/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.klokwrk.cargotracker.booking.domain.model.value

import spock.lang.Specification

class UnLoCodeCoordinatesSpecification extends Specification {
  void "should skip some validity checks while constructing the instance of UNKNOWN_UN_LO_CODE_COORDINATES constant"() {
    expect:
    UnLoCodeCoordinates.UNKNOWN_UN_LO_CODE_COORDINATES.coordinatesEncoded == "9999N 99999W"
  }

  void "should not skip validity checks when trying to construct UNKNOWN_UN_LO_CODE_COORDINATES equivalent"() {
    when:
    new UnLoCodeCoordinates(coordinatesEncoded: "9999N 99999W")

    then:
    AssertionError exception = thrown(AssertionError)
    exception.message
  }

  void "should fail for invalid format of UnLoCode coordinates"() {
    when:
    new UnLoCodeCoordinates(coordinatesEncoded: coordinatesEncodedParam)

    then:
    AssertionError exception = thrown(AssertionError)
    exception.message.contains(messageFragmentParam)

    where:
    coordinatesEncodedParam | messageFragmentParam
    null                    | "not(blankOrNullString())"
    ""                      | "not(blankOrNullString())"
    "  "                    | "not(blankOrNullString())"

    "1"                     | "hasLength(COORDINATES_ENCODED_STRING_LENGTH)"
    "1234567890123"         | "hasLength(COORDINATES_ENCODED_STRING_LENGTH)"

    "AB3456789012"          | "matchesPattern(COORDINATES_PATTERN)"
    "9999N99999W "          | "matchesPattern(COORDINATES_PATTERN)"
    "9999N-99999W"          | "matchesPattern(COORDINATES_PATTERN)"
    "9999N:99999W"          | "matchesPattern(COORDINATES_PATTERN)"
    "9999W 99999N"          | "matchesPattern(COORDINATES_PATTERN)"
    "9999A 99999W"          | "matchesPattern(COORDINATES_PATTERN)"
    "9999A 99999E"          | "matchesPattern(COORDINATES_PATTERN)"
    "9999N 99999A"          | "matchesPattern(COORDINATES_PATTERN)"
    "9999S 99999A"          | "matchesPattern(COORDINATES_PATTERN)"
  }

  void "should fail for invalid latitude values"() {
    when:
    new UnLoCodeCoordinates(coordinatesEncoded: coordinatesEncodedParam)

    then:
    AssertionError exception = thrown(AssertionError)
    exception.message.contains(messageFragmentParam)

    where:
    coordinatesEncodedParam | messageFragmentParam
    "9999N 99999W"          | "Latitude minutes cannot be greater than or equal to 60"

    "9959N 99999W"          | "Latitude degrees cannot be greater than 90"
    "9059N 99999W"          | "Latitude degrees cannot be greater than 90"
    "9001N 99999W"          | "Latitude degrees cannot be greater than 90"
  }

  void "should fail for invalid longitude values"() {
    when:
    new UnLoCodeCoordinates(coordinatesEncoded: coordinatesEncodedParam)

    then:
    AssertionError exception = thrown(AssertionError)
    exception.message.contains(messageFragmentParam)

    where:
    coordinatesEncodedParam | messageFragmentParam
    "9000N 99999W"          | "Longitude minutes cannot be greater than or equal to 60"

    "9000N 99959W"          | "Longitude degrees cannot be greater than 180"
    "9000N 18059W"          | "Longitude degrees cannot be greater than 180"
    "9000N 18001W"          | "Longitude degrees cannot be greater than 180"
  }

  void "should succeed for valid coordinates"() {
    when:
    new UnLoCodeCoordinates(coordinatesEncoded: coordinatesEncodedParam)

    then:
    true

    where:
    coordinatesEncodedParam | _
    "9000N 18000W"          | _
    "3059N 18000W"          | _
    "0001N 18000W"          | _
    "0000N 18000W"          | _

    "9000S 18000W"          | _
    "3059S 18000W"          | _
    "0001S 18000W"          | _
    "0000S 18000W"          | _

    "9000N 18000W"          | _
    "9000N 09059W"          | _
    "9000N 00001W"          | _
    "9000N 00000W"          | _

    "9000N 18000E"          | _
    "9000N 09059E"          | _
    "9000N 00001E"          | _
    "9000N 00000E"          | _
  }

  void "getLatitudeInDegrees - should return null for UNKNOWN_UN_LO_CODE_COORDINATES"() {
    expect:
    UnLoCodeCoordinates.UNKNOWN_UN_LO_CODE_COORDINATES.latitudeInDegrees == null
  }

  void "getLatitudeInDegrees - should return cached value on second invocation"() {
    when:
    UnLoCodeCoordinates unLoCodeCoordinates = new UnLoCodeCoordinates(coordinatesEncoded: "5231N 01323E")
    BigDecimal latitude = unLoCodeCoordinates.latitudeInDegrees

    then:
    unLoCodeCoordinates.latitudeInDegrees === latitude
  }

  void "getLatitudeInDegrees - should work as expected"() {
    when:
    UnLoCodeCoordinates unLoCodeCoordinates = new UnLoCodeCoordinates(coordinatesEncoded: coordinatesEncodedParam)

    then:
    unLoCodeCoordinates.latitudeInDegrees == latitudeInDegreesParam

    where:
    coordinatesEncodedParam | latitudeInDegreesParam | locationNameParamUnused
    "5231N 01323E"          | 52.52                  | "Berlin"
    "3210N 09540W"          | 32.17                  | "New York"
    "2253S 04314W"          | -22.88                 | "Rio de Janeiro"
    "3749S 14458E"          | -37.82                 | "Melbourne"
  }

  void "getLongitudeInDegrees - should return null for UNKNOWN_UN_LO_CODE_COORDINATES"() {
    expect:
    UnLoCodeCoordinates.UNKNOWN_UN_LO_CODE_COORDINATES.longitudeInDegrees == null
  }

  void "getLongitudeInDegrees - should return cached value on second invocation"() {
    when:
    UnLoCodeCoordinates unLoCodeCoordinates = new UnLoCodeCoordinates(coordinatesEncoded: "5231N 01323E")
    BigDecimal longitude = unLoCodeCoordinates.longitudeInDegrees

    then:
    unLoCodeCoordinates.longitudeInDegrees === longitude
  }

  void "getLongitudeInDegrees - should work as expected"() {
    when:
    UnLoCodeCoordinates unLoCodeCoordinates = new UnLoCodeCoordinates(coordinatesEncoded: coordinatesEncodedParam)

    then:
    unLoCodeCoordinates.longitudeInDegrees == longitudeInDegreesParam

    where:
    coordinatesEncodedParam | longitudeInDegreesParam | locationNameParamUnused
    "5231N 01323E"          | 13.38                   | "Berlin"
    "3210N 09540W"          | -95.67                  | "New York"
    "2253S 04314W"          | -43.23                  | "Rio de Janeiro"
    "3749S 14458E"          | 144.97                  | "Melbourne"
  }
}
