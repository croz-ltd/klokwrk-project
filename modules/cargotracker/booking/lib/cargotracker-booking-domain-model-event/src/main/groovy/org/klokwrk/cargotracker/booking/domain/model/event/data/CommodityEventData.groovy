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
package org.klokwrk.cargotracker.booking.domain.model.event.data

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import javax.measure.Quantity
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature

import static org.klokwrk.cargotracker.booking.domain.model.event.support.QuantityFormatter.instance as quantityParser
import static org.klokwrk.cargotracker.booking.domain.model.event.support.QuantityFormatter.instance as quantityFormatter

@SuppressWarnings("CodeNarc.DuplicateImport")
@KwrkImmutable
@CompileStatic
class CommodityEventData {
  CommodityType commodityType
  String commodityRequestedStorageTemperature
  String commodityWeight
  Integer containerCount
  BigDecimal containerTeuCount
  ContainerType containerType
  String maxAllowedWeightPerContainer
  String maxRecommendedWeightPerContainer

  static Collection<CommodityEventData> fromCommodityCollection(Collection<Commodity> commodityCollection) {
    return commodityCollection.collect({ Commodity commodity -> fromCommodity(commodity) })
  }

  static CommodityEventData fromCommodity(Commodity commodity) {
    return new CommodityEventData(
        commodityType: commodity.commodityInfo.commodityType,
        commodityRequestedStorageTemperature: commodity.commodityInfo.requestedStorageTemperature == null ? null : quantityFormatter.format(commodity.commodityInfo.requestedStorageTemperature),
        commodityWeight: quantityFormatter.format(commodity.commodityInfo.weight),
        containerCount: commodity.containerCount,
        containerTeuCount: commodity.containerTeuCount,
        containerType: commodity.containerType,
        maxAllowedWeightPerContainer: quantityFormatter.format(commodity.maxAllowedWeightPerContainer),
        maxRecommendedWeightPerContainer: quantityFormatter.format(commodity.maxRecommendedWeightPerContainer)
    )
  }

  Commodity toCommodity() {
    return new Commodity(
        containerType: containerType,
        commodityInfo: CommodityInfo.make(
            commodityType,
            quantityParser.parse(commodityWeight) as Quantity<Mass>,
            commodityRequestedStorageTemperature == null ? null : quantityParser.parse(commodityRequestedStorageTemperature) as Quantity<Temperature>
        ),
        maxAllowedWeightPerContainer: quantityParser.parse(maxAllowedWeightPerContainer) as Quantity<Mass>,
        maxRecommendedWeightPerContainer: quantityParser.parse(maxRecommendedWeightPerContainer) as Quantity<Mass>,
        containerCount: containerCount,
        containerTeuCount: containerTeuCount
    )
  }
}
