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
package org.klokwrk.cargotracking.domain.model.value

import spock.lang.Specification

import static CommodityType.AIR_COOLED
import static CommodityType.CHILLED
import static CommodityType.DRY
import static CommodityType.FROZEN

class CommodityTypeSpecification extends Specification {
  void "should have expected enum size"() {
    // Failure of this test is a signal that we should check places where enumeration is used and update tests and switch/if/else statements
    expect:
    CommodityType.values().size() == 4
  }

  void "isStorageTemperatureAllowed method should work as expected"() {
    given:
    CommodityType commodityType = commodityTypeParam

    when:
    Boolean isTemperatureAllowed = commodityType.isStorageTemperatureAllowed(storageTemperatureParam)

    then:
    isTemperatureAllowed == isTemperatureAllowedParam

    where:
    commodityTypeParam | storageTemperatureParam | isTemperatureAllowedParam
    DRY                | 0.degC                  | true
    DRY                | -100.degC               | true
    DRY                | 100.degC                | true

    AIR_COOLED         | 1.degC                  | false
    AIR_COOLED         | 2.degC                  | true
    AIR_COOLED         | 10.degC                 | true
    AIR_COOLED         | 12.degC                 | true
    AIR_COOLED         | 13.degC                 | false

    CHILLED            | -3.degC                 | false
    CHILLED            | -2.degC                 | true
    CHILLED            | 5.degC                  | true
    CHILLED            | 6.degC                  | true
    CHILLED            | 7.degC                  | false

    FROZEN             | -21.degC                | false
    FROZEN             | -20.degC                | true
    FROZEN             | -10.degC                | true
    FROZEN             | -8.degC                 | true
    FROZEN             | -7.degC                 | false
  }

  @SuppressWarnings('GroovyPointlessBoolean')
  void "isStorageTemperatureAllowed method should work for null param as expected"() {
    given:
    CommodityType commodityType = commodityTypeParam as CommodityType

    when:
    commodityType.isStorageTemperatureAllowed(null)

    then:
    thrown(AssertionError)

    where:
    commodityTypeParam << CommodityType.values()
  }

  void "isStorageTemperatureLimited method should work as expected"() {
    given:
    CommodityType commodityType = commodityTypeParam as CommodityType

    when:
    Boolean result = commodityType.isStorageTemperatureLimited()

    then:
    result == resultParam

    where:
    commodityTypeParam | resultParam
    DRY                | false
    AIR_COOLED         | true
    CHILLED            | true
    FROZEN             | true
  }
}
