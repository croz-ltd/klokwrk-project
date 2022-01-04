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
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerFeaturesType.FEATURES_ISO_G1
import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerFeaturesType.FEATURES_ISO_R1_STANDARD_REEFER

class ContainerFeaturesTypeSpecification extends Specification {
  void "should have expected enum size"() {
    expect:
    ContainerFeaturesType.values().size() == 2
  }

  void "should have expected container temperature ranges"() {
    expect:
    FEATURES_ISO_G1.containerTemperatureRange.minimum == null
    FEATURES_ISO_G1.containerTemperatureRange.maximum == null

    FEATURES_ISO_R1_STANDARD_REEFER.containerTemperatureRange.minimum == Quantities.getQuantity(-30, Units.CELSIUS)
    FEATURES_ISO_R1_STANDARD_REEFER.containerTemperatureRange.maximum == Quantities.getQuantity(30, Units.CELSIUS)
  }

  void "isContainerTemperatureControlled() method should work as expected"() {
    given:
    ContainerFeaturesType containerFeaturesType = containerFeturesTypeParam

    when:
    Boolean result = containerFeaturesType.isContainerTemperatureControlled()

    then:
    result == resultParam

    where:
    containerFeturesTypeParam       | resultParam
    FEATURES_ISO_G1                 | false
    FEATURES_ISO_R1_STANDARD_REEFER | true
  }
}
