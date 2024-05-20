/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracking.test.support.fixture.util

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.test.support.fixture.base.JsonFixtureBuilder

import javax.measure.Quantity

@CompileStatic
class JsonFixtureUtils {
  static String stringToJsonString(String stringToRender) {
    String stringToReturn = stringToRender == null ? "null" : /"$stringToRender"/
    return stringToReturn
  }

  static List<Map<String, ?>> jsonFixtureBuilderListToJsonList(List<? extends JsonFixtureBuilder> jsonFixtureBuilderList) {
    List<Map<String, ?>> listToUse = []
    jsonFixtureBuilderList.each {
      listToUse.add(it.buildAsMap())
    }

    return listToUse
  }

  static String jsonFixtureBuilderListToJsonListString(List<? extends JsonFixtureBuilder> jsonFixtureBuilderList) {
    String listStringContent = jsonFixtureBuilderList.collect({ it.buildAsJsonString() }).join(",")
    return "[$listStringContent]"
  }

  @SuppressWarnings("CodeNarc.ReturnsNullInsteadOfEmptyCollection")
  static Map<String, ?> quantityToJsonMap(Quantity quantity) {
    if (quantity == null) {
      return null
    }

    return [
        value: quantity.value,
        unitSymbol: quantity.unit.toString()
    ]
  }

  static String quantityToJsonString(Quantity quantity) {
    if (quantity == null) {
      return "null"
    }

    return """
        {
            "value": $quantity.value,
            "unitSymbol": "$quantity.unit"
        }
    """
  }
}
