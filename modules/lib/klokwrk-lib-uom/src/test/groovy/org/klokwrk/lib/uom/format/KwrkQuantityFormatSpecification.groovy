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
package org.klokwrk.lib.uom.format

import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.format.QuantityFormat

class KwrkQuantityFormatSpecification extends Specification {
  void "should format correctly"() {
    given:
    QuantityFormat formatter = KwrkQuantityFormat.instance

    when:
    String formattedString = formatter.format(Quantities.getQuantity(valueParam, unitParam))

    then:
    formattedString == formattedStringParam

    where:
    valueParam | unitParam      | formattedStringParam
    1          | Units.KILOGRAM | "1 kg"
    1          | Units.CELSIUS  | "1 °C"
  }

  void "should parse correctly"() {
    given:
    QuantityFormat parser = KwrkQuantityFormat.instance

    when:
    Quantity quantity = parser.parse(inputStringParam)

    then:
    quantity.value as String == inputStringParam.tokenize().first()
    quantity.unit == unitParam

    where:
    inputStringParam | unitParam
    "1 kg"           | Units.KILOGRAM
    "1 °C"           | Units.CELSIUS
  }
}
