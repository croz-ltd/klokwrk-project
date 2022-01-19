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
package org.klokwrk.cargotracker.booking.domain.model.service

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerDimensionType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType

import javax.measure.Quantity
import javax.measure.quantity.Mass

import static org.hamcrest.Matchers.notNullValue

/**
 * Default implementation of {@link CommodityCreatorService}.
 * <p/>
 * For determining maximum allowed weight per container, it uses provided {@link MaxAllowedWeightPerContainerPolicy} service instance.
 */
@CompileStatic
class DefaultCommodityCreatorService implements CommodityCreatorService {
  MaxAllowedWeightPerContainerPolicy maxAllowedWeightPerContainerPolicy

  /**
   * Constructor.
   * <p/>
   * Provided {@link MaxAllowedWeightPerContainerPolicy} instance must not be {@code null}.
   */
  DefaultCommodityCreatorService(MaxAllowedWeightPerContainerPolicy maxAllowedWeightPerContainerPolicy) {
    requireMatch(maxAllowedWeightPerContainerPolicy, notNullValue())

    this.maxAllowedWeightPerContainerPolicy = maxAllowedWeightPerContainerPolicy
  }

  @Override
  Commodity from(ContainerDimensionType containerDimensionType, CommodityInfo commodityInfo) {
    ContainerType containerType = ContainerType.find(containerDimensionType, commodityInfo.commodityType.containerFeaturesType)
    Quantity<Mass> commodityMaxAllowedWeightPerContainerPerPolicyInKilograms = maxAllowedWeightPerContainerPolicy.maxAllowedWeightPerContainer(containerType)
    Commodity commodity = Commodity.make(containerType, commodityInfo, commodityMaxAllowedWeightPerContainerPerPolicyInKilograms)

    // Potentially we might also want the max container TEU count per commodity type policy
    // This is very similar policy we have in BookingOfferCommodities.canAcceptCommodity(). But there it is cumulative across thw whole BookingOffer.
    // We will not implement this at the moment, and we'll rely on cumulative policy only. Maybe we can introduce this later.
//    if (commodityContainerTeuCount > 5000) {
//      throw new CommandException(ViolationInfo.createForBadRequestWithCustomCodeKey("bookingOfferAggregate.commodityContainerTeuCountTooHigh"))
//    }

    return commodity
  }
}
