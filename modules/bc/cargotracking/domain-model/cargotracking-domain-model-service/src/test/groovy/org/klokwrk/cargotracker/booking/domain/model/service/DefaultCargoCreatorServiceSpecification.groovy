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
package org.klokwrk.cargotracker.booking.domain.model.service

import org.klokwrk.cargotracking.domain.model.value.Cargo
import org.klokwrk.cargotracking.domain.model.value.Commodity
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.domain.model.value.ContainerDimensionType
import org.klokwrk.cargotracking.domain.model.value.ContainerType
import spock.lang.Specification

class DefaultCargoCreatorServiceSpecification extends Specification {
  void "constructor should fail for null parameter"() {
    when:
    new DefaultCargoCreatorService(null)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("[item: maxAllowedWeightPerContainerPolicy, expected: notNullValue(), actual: null]")
  }

  void "from() method should work as expected"() {
    given:
    MaxAllowedWeightPerContainerPolicy maxAllowedWeightPerContainerPolicy = new PercentBasedMaxAllowedWeightPerContainerPolicy(90)
    DefaultCargoCreatorService defaultCargoCreatorService = new DefaultCargoCreatorService(maxAllowedWeightPerContainerPolicy)
    Commodity commodity = Commodity.make(CommodityType.DRY, 50_000)

    when:
    Cargo cargo = defaultCargoCreatorService.from(ContainerDimensionType.DIMENSION_ISO_22, commodity)

    then:
    verifyAll(cargo, {
      containerType == ContainerType.TYPE_ISO_22G1
      it.commodity == commodity
      maxAllowedWeightPerContainer == 19_530.kg
      maxRecommendedWeightPerContainer == 16_667.kg
      containerCount == 3
      containerTeuCount == 3
    })
  }
}
