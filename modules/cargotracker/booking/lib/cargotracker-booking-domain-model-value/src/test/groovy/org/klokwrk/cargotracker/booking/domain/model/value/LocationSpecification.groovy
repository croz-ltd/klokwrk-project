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

class LocationSpecification extends Specification {

  @SuppressWarnings("GroovyPointlessBoolean")
  void "map constructor should work for correct input params"() {
    when:
    Location location = new Location(
        unLoCode: new UnLoCode(code: "HRRJK"), name: new InternationalizedName(name: "Rijeka"), countryName: new InternationalizedName(name: "Croatia"),
        unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "1234----"), unLoCodeCoordinates: new UnLoCodeCoordinates(coordinatesEncoded: "4520N 01424E"),
        portCapabilities: PortCapabilities.makeSeaContainerPortCapabilities()
    )

    then:
    location.unLoCode == new UnLoCode(code: "HRRJK")
    location.name == new InternationalizedName(name: "Rijeka")
    location.countryName == new InternationalizedName(name: "Croatia")
    location.unLoCodeFunction.isPort() == true
    UnLoCodeCoordinates.extractUnLoCodeCoordinatesLatitude(location.unLoCodeCoordinates.coordinatesEncoded) == "4520N"
  }

  void "map constructor should fail for null input params"() {
    when:
    new Location(unLoCode: unLoCodeParam, name: nameParam, countryName: countryNameParam, unLoCodeFunction: unLoCodeFunctionParam, unLoCodeCoordinates: unLoCodeCoordinatesParam,
                 portCapabilities: portCapabilitiesParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("notNullValue")

    where:
    [unLoCodeParam, nameParam, countryNameParam, unLoCodeFunctionParam, unLoCodeCoordinatesParam, portCapabilitiesParam] << [
        [
            null, new InternationalizedName(name: "aName"), new InternationalizedName(name: "aCountry"), new UnLoCodeFunction(functionEncoded: "0-------"),
            new UnLoCodeCoordinates(coordinatesEncoded: "4520N 01424E"), PortCapabilities.NO_PORT_CAPABILITIES
        ],
        [
            new UnLoCode(code: "HRRJK"), null, new InternationalizedName(name: "aCountry"), new UnLoCodeFunction(functionEncoded: "0-------"),
            new UnLoCodeCoordinates(coordinatesEncoded: "4520N 01424E"), PortCapabilities.NO_PORT_CAPABILITIES
        ],
        [
            new UnLoCode(code: "HRRJK"), new InternationalizedName(name: "aName"), null, new UnLoCodeFunction(functionEncoded: "0-------"),
            new UnLoCodeCoordinates(coordinatesEncoded: "4520N 01424E"), PortCapabilities.NO_PORT_CAPABILITIES
        ],
        [
            new UnLoCode(code: "HRRJK"), new InternationalizedName(name: "aName"), new InternationalizedName(name: "aCountry"), null,
            new UnLoCodeCoordinates(coordinatesEncoded: "4520N 01424E"), PortCapabilities.NO_PORT_CAPABILITIES
        ],
        [
            new UnLoCode(code: "HRRJK"), new InternationalizedName(name: "aName"), new InternationalizedName(name: "aCountry"), new UnLoCodeFunction(functionEncoded: "0-------"),
            null, PortCapabilities.NO_PORT_CAPABILITIES
        ],
        [
            new UnLoCode(code: "HRRJK"), new InternationalizedName(name: "aName"), new InternationalizedName(name: "aCountry"), new UnLoCodeFunction(functionEncoded: "0-------"),
            new UnLoCodeCoordinates(coordinatesEncoded: "4520N 01424E"), null
        ]
    ]
  }

  void "map constructor should fail for mismatching unLoCodeFunction and portCapabilities"() {
    when:
    new Location(
        unLoCode: new UnLoCode(code: "HRRJK"), name: new InternationalizedName(name: "aName"), countryName: new InternationalizedName(name: "aCountry"),
        unLoCodeFunction: new UnLoCodeFunction(functionEncoded: functionEncodedParam), unLoCodeCoordinates: new UnLoCodeCoordinates(coordinatesEncoded: "4520N 01424E"),
        portCapabilities: portCapabilitiesParam
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("item: unLoCodeFunction.isPort(), expected: is(portCapabilities.isPort())")

    where:
    functionEncodedParam | portCapabilitiesParam
    "0-------"           | PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES
    "1-------"           | PortCapabilities.NO_PORT_CAPABILITIES
  }

  void "map constructor should fail for invalid input params for construction of contained properties"() {
    when:
    new Location(
        unLoCode: new UnLoCode(code: codeParam), name: new InternationalizedName(name: nameParam), countryName: new InternationalizedName(name: countryNameParam),
        unLoCodeFunction: new UnLoCodeFunction(functionEncoded: functionParam), unLoCodeCoordinates: new UnLoCodeCoordinates(coordinatesEncoded: coordinatesParam),
        portCapabilities: portCapabilitiesParam
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(errorMessagePartParam)

    where:
    codeParam | nameParam  | countryNameParam | functionParam | coordinatesParam | portCapabilitiesParam                 | errorMessagePartParam
    null      | "someName" | "someCountry"    | "0-------"    | "4520N 01424E"   | PortCapabilities.NO_PORT_CAPABILITIES | "item: code, expected: not(blankOrNullString())"
    "HRRJK"   | null       | "someCountry"    | "0-------"    | "4520N 01424E"   | PortCapabilities.NO_PORT_CAPABILITIES | "item: name, expected: not(blankOrNullString())"
    "HRRJK"   | "someName" | null             | "0-------"    | "4520N 01424E"   | PortCapabilities.NO_PORT_CAPABILITIES | "item: name, expected: not(blankOrNullString())"
    "HRRJK"   | "someName" | "someCountry"    | null          | "4520N 01424E"   | PortCapabilities.NO_PORT_CAPABILITIES | "item: functionEncoded, expected: not(blankOrNullString())"
    "HRRJK"   | "someName" | "someCountry"    | "0-------"    | null             | PortCapabilities.NO_PORT_CAPABILITIES | "item: coordinatesEncoded, expected: not(blankOrNullString())"
    "HRRJK"   | "someName" | "someCountry"    | "0-------"    | "4520N 01424E"   | null                                  | "item: portCapabilities, expected: notNullValue()"
  }

  void "make() factory method should work for correct input params"() {
    when:
    Location location = Location.make("HRRJK", "Rijeka", "Croatia", "1234----", "4520N 01424E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES)

    then:
    location.unLoCode == new UnLoCode(code: "HRRJK")
    location.name == new InternationalizedName(name: "Rijeka")
    location.countryName == new InternationalizedName(name: "Croatia")
    location.unLoCodeCoordinates.longitudeInDegrees
    location.portCapabilities.isSeaContainerPort()
  }

  void "make() factory method should fail for invalid input params"() {
    when:
    Location.make(codeParam, nameParam, countryNameParam, functionParam, coordinatesParam, portCapabilitiesParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(errorMessagePartParam)

    where:
    codeParam | nameParam  | countryNameParam | functionParam | coordinatesParam | portCapabilitiesParam                 | errorMessagePartParam
    null      | "someName" | "someCountry"    | "0-------"    | "0000N 00000W"   | PortCapabilities.NO_PORT_CAPABILITIES | "not(blankOrNullString())"
    "HRRJK"   | null       | "someCountry"    | "0-------"    | "0000N 00000W"   | PortCapabilities.NO_PORT_CAPABILITIES | "not(blankOrNullString())"
    "HRRJK"   | "someName" | null             | "0-------"    | "0000N 00000W"   | PortCapabilities.NO_PORT_CAPABILITIES | "not(blankOrNullString())"
    "HRRJK"   | "someName" | "someCountry"    | null          | "0000N 00000W"   | PortCapabilities.NO_PORT_CAPABILITIES | "not(blankOrNullString())"
    "HRRJK"   | "someName" | "someCountry"    | "0-------"    | null             | PortCapabilities.NO_PORT_CAPABILITIES | "not(blankOrNullString())"
    "HRRJK"   | "someName" | "someCountry"    | "0-------"    | "0000N 00000W"   | null                                  | "notNullValue()"

    "HRRJK"   | "someName" | "someCountry"    | "1-------"    | "0000N 00000W"   | PortCapabilities.NO_PORT_CAPABILITIES | "unLoCodeFunction.isPort(), expected: is(portCapabilities.isPort())"
    "HRRJK"   | "someName" | "someCountry"    | "1-------"    | "0000N 00000W"   | PortCapabilities.NO_PORT_CAPABILITIES | "unLoCodeFunction.isPort(), expected: is(portCapabilities.isPort())"
  }
}
