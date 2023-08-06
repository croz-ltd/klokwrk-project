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
package org.klokwrk.lib.lo.uom.format

import groovy.transform.CompileStatic
import si.uom.NonSI
import systems.uom.common.USCustomary
import tech.units.indriya.format.SimpleUnitFormat
import tech.units.indriya.unit.Units

import javax.measure.MetricPrefix
import javax.measure.format.UnitFormat

import static org.klokwrk.lib.lo.uom.constants.UomConstants.CELSIUS_SYMBOL

/**
 * Customization of indriya (JSR-385 reference implementation) {@code SimpleUnitFormat} with default flavor.
 * <p/>
 * At the moment, this customization does the following:
 * <ul>
 * <li>Uses {@code °C} (unicode char 00B0 + character C) for Celsius unit formatting instead of original {@code ℃} (unicode char 2103)</li>
 * </ul>
 */
@CompileStatic
class KwrkSimpleUnitFormat {
  // Ensure loading of NonSI and USCustomary system of units before using KwrkSimpleUnitFormat. This is necessary to ensure proper updates to SimpleUnitFormat by those systems of units. Otherwise
  // SimpleUnitFormat will not be aware of new units.
  // This could be placed somewhere in application class' static initialization code, but I choose to put it here to avoid confusion as much as possible.
  static {
    NonSI.instance.name
    USCustomary.instance.name
  }

  private static final UnitFormat UNIT_FORMATTER = SimpleUnitFormat.instance.tap({ SimpleUnitFormat simpleUnitFormat ->
    // Note: The following code is taken and adapted from "tech.units.indriya.format.SimpleUnitFormat"
    simpleUnitFormat.label(Units.CELSIUS, CELSIUS_SYMBOL)
    MetricPrefix.values().each { MetricPrefix metricPrefix ->
      simpleUnitFormat.label(Units.CELSIUS.prefix(metricPrefix), metricPrefix.symbol + CELSIUS_SYMBOL)
    }
  })

  @SuppressWarnings("CodeNarc.GetterMethodCouldBeProperty")
  static UnitFormat getInstance() {
    return UNIT_FORMATTER
  }
}
