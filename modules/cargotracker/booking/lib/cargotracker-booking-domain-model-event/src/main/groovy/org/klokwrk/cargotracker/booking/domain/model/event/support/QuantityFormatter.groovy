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
package org.klokwrk.cargotracker.booking.domain.model.event.support

import groovy.transform.CompileStatic
import tech.units.indriya.format.AbstractQuantityFormat
import tech.units.indriya.format.NumberDelimiterQuantityFormat
import tech.units.indriya.format.SimpleUnitFormat
import tech.units.indriya.unit.Units

import javax.measure.MetricPrefix
import java.text.NumberFormat

@CompileStatic
class QuantityFormatter {
  private static final String CELSIUS_SYMBOL = "°C"

  @SuppressWarnings("CodeNarc.Indentation")
  private static final AbstractQuantityFormat FORMATTER = new NumberDelimiterQuantityFormat.Builder()
      .setNumberFormat(NumberFormat.getInstance(Locale.ROOT).tap({ groupingUsed = false }))
      .setUnitFormat(SimpleUnitFormat.getNewInstance(SimpleUnitFormat.Flavor.Default).tap({ SimpleUnitFormat simpleUnitFormat ->
          // Note: The following code is taken and adapted from "tech.units.indriya.format.SimpleUnitFormat"
          simpleUnitFormat.label(Units.CELSIUS, CELSIUS_SYMBOL)
          MetricPrefix.values().each { MetricPrefix metricPrefix ->
              simpleUnitFormat.label(Units.CELSIUS.prefix(metricPrefix), metricPrefix.symbol + CELSIUS_SYMBOL)
          }
      }))
      .build()

  @SuppressWarnings("CodeNarc.GetterMethodCouldBeProperty")
  static AbstractQuantityFormat getInstance() {
    return FORMATTER
  }
}
