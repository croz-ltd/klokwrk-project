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

import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityFixtureBuilder
import spock.lang.Specification

class CommodityEventDataSpecification extends Specification {
  void "fromCommodity() should work as expected"() {
    given:
    Commodity commodity = commodityParam

    when:
    CommodityEventData commodityEventData = CommodityEventData.fromCommodity(commodity)

    then:
    commodityEventData == commodityEventDataExpectedParam

    where:
    commodityParam                                        | commodityEventDataExpectedParam
    CommodityFixtureBuilder.commodity_dry().build()       | CommodityEventDataFixtureBuilder.commodity_dry().build()
    CommodityFixtureBuilder.commodity_airCooled().build() | CommodityEventDataFixtureBuilder.commodity_airCooled().build()
    CommodityFixtureBuilder.commodity_chilled().build()   | CommodityEventDataFixtureBuilder.commodity_chilled().build()
    CommodityFixtureBuilder.commodity_frozen().build()    | CommodityEventDataFixtureBuilder.commodity_frozen().build()
  }

  void "fromCommodityCollection() should work as expected"() {
    given:
    Collection<Commodity> commodityCollection = [CommodityFixtureBuilder.commodity_dry().build(), CommodityFixtureBuilder.commodity_airCooled().build()]

    when:
    Collection<CommodityEventData> commodityEventDataCollection = CommodityEventData.fromCommodityCollection(commodityCollection)

    then:
    commodityEventDataCollection.containsAll([CommodityEventDataFixtureBuilder.commodity_dry().build(), CommodityEventDataFixtureBuilder.commodity_airCooled().build()])
  }

  void "toCommodity() should work as expected"() {
    given:
    CommodityEventData commodityEventData = commodityEventDataParam

    when:
    Commodity commodity = commodityEventData.toCommodity()

    then:
    commodity == commodityExpectedParam

    where:
    commodityEventDataParam                                        | commodityExpectedParam
    CommodityEventDataFixtureBuilder.commodity_dry().build()       | CommodityFixtureBuilder.commodity_dry().build()
    CommodityEventDataFixtureBuilder.commodity_airCooled().build() | CommodityFixtureBuilder.commodity_airCooled().build()
    CommodityEventDataFixtureBuilder.commodity_chilled().build()   | CommodityFixtureBuilder.commodity_chilled().build()
    CommodityEventDataFixtureBuilder.commodity_frozen().build()    | CommodityFixtureBuilder.commodity_frozen().build()
  }

  void "toCommodityCollection() should work as expected"() {
    given:
    Collection<CommodityEventData> commodityEventDataCollection = [CommodityEventDataFixtureBuilder.commodity_dry().build(), CommodityEventDataFixtureBuilder.commodity_airCooled().build()]

    when:
    Collection<Commodity> commodityCollection = CommodityEventData.toCommodityCollection(commodityEventDataCollection)

    then:
    commodityCollection.containsAll([CommodityFixtureBuilder.commodity_dry().build(), CommodityFixtureBuilder.commodity_airCooled().build()])
  }
}
