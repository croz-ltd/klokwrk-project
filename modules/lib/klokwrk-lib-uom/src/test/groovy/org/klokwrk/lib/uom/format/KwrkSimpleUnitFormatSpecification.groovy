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
package org.klokwrk.lib.uom.format

import spock.lang.Specification
import tech.units.indriya.unit.Units

import javax.measure.Unit

import static javax.measure.MetricPrefix.MICRO
import static javax.measure.MetricPrefix.MILLI

class KwrkSimpleUnitFormatSpecification extends Specification {
  void "should format Celsius as expected"() {
    when:
    String formattedUnit = KwrkSimpleUnitFormat.instance.format(celsiusUnitParam)

    then:
    formattedUnit == celsiusUnitFormattedParam

    where:
    celsiusUnitParam            | celsiusUnitFormattedParam
    Units.CELSIUS               | "°C"
    Units.CELSIUS.prefix(MILLI) | "m°C"
    Units.CELSIUS.prefix(MICRO) | "µ°C"
  }

  void "should parse Celsius as expected"() {
    when:
    Unit unit = KwrkSimpleUnitFormat.instance.parse(celsiusUnitStringParam)

    then:
    unit == celsiusUnitParam

    where:
    celsiusUnitStringParam | celsiusUnitParam
    "°C"                   | Units.CELSIUS
    "m°C"                  | Units.CELSIUS.prefix(MILLI)
    "µ°C"                  | Units.CELSIUS.prefix(MICRO)
  }
}
