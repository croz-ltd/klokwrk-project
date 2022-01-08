/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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

class PortCapabilitiesSpecification extends Specification {

  @SuppressWarnings("GroovyPointlessBoolean")
  void "constants should be valid"() {
    expect:
    PortCapabilities.NO_PORT_CAPABILITIES.capabilities.size() == 1
    PortCapabilities.NO_PORT_CAPABILITIES.isPort() == false

    PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES.capabilities.size() == 2
    PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES.isPort() == true
    PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES.isSeaPort() == true
    PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES.isContainerPort() == true
    PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES.isSeaContainerPort() == true
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "map constructor should work for correct input params"() {
    when:
    PortCapabilities portCapabilities = new PortCapabilities(capabilities: [PortCapabilityType.SEA_PORT])

    then:
    portCapabilities.isPort() == true
    portCapabilities.isSeaPort() == true
  }

  void "map constructor should fail for invalid input params"() {
    when:
    new PortCapabilities(capabilities: portCapabilitiesParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messagePartParam)

    where:
    portCapabilitiesParam                      | messagePartParam
    null                                       | "notNullValue()"
    [] as Set                                  | "not(empty())"
    ["123"] as Set                             | "everyItem(instanceOf(PortCapabilityType))"
    [PortCapabilityType.SEA_PORT, null] as Set | "everyItem(instanceOf(PortCapabilityType))"
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "isPort() - negation should work correctly"() {
    when:
    PortCapabilities portCapabilities = new PortCapabilities(capabilities: [PortCapabilityType.NO_PORT, PortCapabilityType.SEA_PORT])

    then:
    portCapabilities.isPort() == false
    portCapabilities.isSeaPort() == false
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "isPort() - affirmation should work correctly"() {
    when:
    PortCapabilities portCapabilities = new PortCapabilities(capabilities: [PortCapabilityType.CONTAINER_PORT])

    then:
    portCapabilities.isPort() == true
    portCapabilities.isContainerPort() == true
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "isSeaPort() - negation should work correctly"() {
    when:
    PortCapabilities portCapabilities = new PortCapabilities(capabilities: capabilitiesParam)

    then:
    portCapabilities.isSeaPort() == false

    where:
    capabilitiesParam                                           | _
    [PortCapabilityType.NO_PORT]                                | _
    [PortCapabilityType.NO_PORT, PortCapabilityType.SEA_PORT]   | _
    [PortCapabilityType.NO_PORT, PortCapabilityType.RIVER_PORT] | _
    [PortCapabilityType.RIVER_PORT]                             | _
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "isSeaPort() - affirmation should work correctly"() {
    when:
    PortCapabilities portCapabilities = new PortCapabilities(capabilities: [PortCapabilityType.SEA_PORT, PortCapabilityType.CONTAINER_PORT, PortCapabilityType.BULK_CARGO_PORT])

    then:
    portCapabilities.isSeaPort() == true
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "isRiverPort() - negation should work correctly"() {
    when:
    PortCapabilities portCapabilities = new PortCapabilities(capabilities: capabilitiesParam)

    then:
    portCapabilities.isRiverPort() == false

    where:
    capabilitiesParam                                           | _
    [PortCapabilityType.NO_PORT]                                | _
    [PortCapabilityType.NO_PORT, PortCapabilityType.RIVER_PORT] | _
    [PortCapabilityType.NO_PORT, PortCapabilityType.SEA_PORT]   | _
    [PortCapabilityType.SEA_PORT]                               | _
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "isRiverPort() - affirmation should work correctly"() {
    when:
    PortCapabilities portCapabilities = new PortCapabilities(capabilities: [PortCapabilityType.RIVER_PORT, PortCapabilityType.SEA_PORT])

    then:
    portCapabilities.isRiverPort() == true
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "isContainerPort() - negation should work correctly"() {
    when:
    PortCapabilities portCapabilities = new PortCapabilities(capabilities: capabilitiesParam)

    then:
    portCapabilities.isContainerPort() == false

    where:
    capabilitiesParam                                               | _
    [PortCapabilityType.NO_PORT]                                    | _
    [PortCapabilityType.NO_PORT, PortCapabilityType.CONTAINER_PORT] | _
    [PortCapabilityType.NO_PORT, PortCapabilityType.SEA_PORT]       | _
    [PortCapabilityType.SEA_PORT]                                   | _
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "isContainerPort() - affirmation should work correctly"() {
    when:
    PortCapabilities portCapabilities = new PortCapabilities(capabilities: [PortCapabilityType.RIVER_PORT, PortCapabilityType.CONTAINER_PORT])

    then:
    portCapabilities.isContainerPort() == true
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "isSeaContainerPort() - negation should work correctly"() {
    when:
    PortCapabilities portCapabilities = new PortCapabilities(capabilities: capabilitiesParam)

    then:
    portCapabilities.isSeaContainerPort() == false

    where:
    capabilitiesParam                                                                            | _
    [PortCapabilityType.NO_PORT]                                                                 | _
    [PortCapabilityType.NO_PORT, PortCapabilityType.SEA_PORT]                                    | _
    [PortCapabilityType.NO_PORT, PortCapabilityType.SEA_PORT, PortCapabilityType.CONTAINER_PORT] | _
    [PortCapabilityType.SEA_PORT]                                                                | _
    [PortCapabilityType.CONTAINER_PORT]                                                          | _
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "isSeaContainerPort() - affirmation should work correctly"() {
    when:
    PortCapabilities portCapabilities = new PortCapabilities(capabilities: [PortCapabilityType.SEA_PORT, PortCapabilityType.CONTAINER_PORT, PortCapabilityType.BULK_CARGO_PORT])

    then:
    portCapabilities.isSeaContainerPort() == true
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "makeFromStringsIfPossible() - should work for valid input"() {
    when:
    PortCapabilities portCapabilities = PortCapabilities.makeFromStringsIfPossible(["SEA_PORT", "CONTAINER_PORT", "BULK_CARGO_PORT"])

    then:
    portCapabilities.isPort() == true
    portCapabilities.isSeaPort() == true
    portCapabilities.isContainerPort() == true
    portCapabilities.isSeaContainerPort() == true

    where:
    nameListParam                          | _
    ["SEA_PORT", "CONTAINER_PORT"]         | _
    ["sea_PORT", "CONTAINER_port"]         | _
    ["  SEA_PORT  ", "  CONTAINER_PORT  "] | _
    ["  sea_PORT  ", "  CONTAINER_port  "] | _
  }

  void "makeFromStringsIfPossible() - should return null for invalid input"() {
    when:
    PortCapabilities portCapabilities = PortCapabilities.makeFromStringsIfPossible(nameListParam)

    then:
    portCapabilities == null

    where:
    nameListParam                          | _
    null                                   | _
    []                                     | _
    [null, "SEA_PORT", "CONTAINER_PORT"]   | _
    ["SEA_PORT", "CONTAINER_PORT", ""]     | _
    ["SEA_PORT", "CONTAINER_PORT", "    "] | _
    ["SEA_PORT", "CONTAINER_PORT", "bla"]  | _
  }

  void "makeNoPortCapabilities() - should work as expected"() {
    expect:
    PortCapabilities.makeNoPortCapabilities() == PortCapabilities.NO_PORT_CAPABILITIES
    !(PortCapabilities.makeNoPortCapabilities() === PortCapabilities.NO_PORT_CAPABILITIES)
  }

  void "makeSeaPortCapabilities() - should work as expected"() {
    expect:
    PortCapabilities.makeSeaPortCapabilities() == PortCapabilities.SEA_PORT_CAPABILITIES
    !(PortCapabilities.makeSeaPortCapabilities() === PortCapabilities.SEA_PORT_CAPABILITIES)
  }

  void "makeRiverPortCapabilities() - should work as expected"() {
    expect:
    PortCapabilities.makeRiverPortCapabilities() == PortCapabilities.RIVER_PORT_CAPABILITIES
    !(PortCapabilities.makeRiverPortCapabilities() === PortCapabilities.RIVER_PORT_CAPABILITIES)
  }

  void "makeSeaContainerPortCapabilities() - should work as expected"() {
    expect:
    PortCapabilities.makeSeaContainerPortCapabilities() == PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES
    !(PortCapabilities.makeSeaContainerPortCapabilities() === PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES)
  }
}
