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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerDimensionType

import javax.measure.Quantity
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature

import static org.klokwrk.lang.groovy.misc.JsonUtils.stringToJsonString

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CargoRequestDataJsonFixtureBuilder {
  static CargoRequestDataJsonFixtureBuilder cargoRequestData_base() {
    return new CargoRequestDataJsonFixtureBuilder()
        .commodityWeight(1000.kg)
        .containerDimensionType(ContainerDimensionType.DIMENSION_ISO_22.name())
  }

  static CargoRequestDataJsonFixtureBuilder cargoRequestData_dry() {
    return new CargoRequestDataJsonFixtureBuilder()
        .commodityType(CommodityType.DRY.name())
        .commodityWeight(1000.kg)
        .containerDimensionType(ContainerDimensionType.DIMENSION_ISO_22.name())
  }

  static CargoRequestDataJsonFixtureBuilder cargoRequestData_chilled() {
    return new CargoRequestDataJsonFixtureBuilder()
        .commodityType(CommodityType.CHILLED.name())
        .commodityWeight(1000.kg)
        .containerDimensionType(ContainerDimensionType.DIMENSION_ISO_22.name())
  }

  static CargoRequestDataJsonFixtureBuilder cargoRequestData_airCooled() {
    return new CargoRequestDataJsonFixtureBuilder()
        .commodityType(CommodityType.AIR_COOLED.name())
        .commodityWeight(1000.kg)
        .containerDimensionType(ContainerDimensionType.DIMENSION_ISO_22.name())
  }

  static CargoRequestDataJsonFixtureBuilder cargoRequestData_frozen() {
    return new CargoRequestDataJsonFixtureBuilder()
        .commodityType(CommodityType.FROZEN.name())
        .commodityWeight(1000.kg)
        .containerDimensionType(ContainerDimensionType.DIMENSION_ISO_22.name())
  }

  String commodityType
  Quantity<Mass> commodityWeight
  Quantity<Temperature> commodityRequestedStorageTemperature
  String containerDimensionType

  Map<String, ?> buildAsMap() {
    Map<String, ?> mapToReturn = [
        commodityType: commodityType,
        commodityWeight: quantityToJsonMap(commodityWeight),
        commodityRequestedStorageTemperature: quantityToJsonMap(commodityRequestedStorageTemperature),
        containerDimensionType: containerDimensionType
    ]

    return mapToReturn
  }

  String buildAsJsonString() {
    String stringToReturn = """
        {
            "commodityType": ${ stringToJsonString(commodityType) },
            "commodityWeight": ${ quantityToJsonString(commodityWeight) },
            "commodityRequestedStorageTemperature": ${ quantityToJsonString(commodityRequestedStorageTemperature) },
            "containerDimensionType": ${ stringToJsonString(containerDimensionType) }
        }
        """

    return stringToReturn
  }

  @SuppressWarnings("CodeNarc.ReturnsNullInsteadOfEmptyCollection")
  protected Map<String, ?> quantityToJsonMap(Quantity quantity) {
    if (quantity == null) {
      return null
    }

    return [
        value: quantity.value,
        unitSymbol: quantity.unit.toString()
    ]
  }

  protected String quantityToJsonString(Quantity quantity) {
    if (quantity == null) {
      return null
    }

    return """
        {
            "value": $quantity.value,
            "unitSymbol": "$quantity.unit"
        }
    """
  }
}
