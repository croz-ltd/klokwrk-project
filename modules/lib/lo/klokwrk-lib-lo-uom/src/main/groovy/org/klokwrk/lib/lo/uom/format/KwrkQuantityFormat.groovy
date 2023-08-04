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
import tech.units.indriya.format.NumberDelimiterQuantityFormat

import javax.measure.format.QuantityFormat
import java.text.NumberFormat

@CompileStatic
class KwrkQuantityFormat {
  @SuppressWarnings("CodeNarc.Indentation")
  private static final QuantityFormat QUANTITY_FORMAT = new NumberDelimiterQuantityFormat.Builder()
      .setNumberFormat(NumberFormat.getInstance(Locale.ROOT).tap({ groupingUsed = false }))
      .setUnitFormat(KwrkSimpleUnitFormat.instance)
      .build()

  @SuppressWarnings("CodeNarc.GetterMethodCouldBeProperty")
  static QuantityFormat getInstance() {
    return QUANTITY_FORMAT
  }
}
