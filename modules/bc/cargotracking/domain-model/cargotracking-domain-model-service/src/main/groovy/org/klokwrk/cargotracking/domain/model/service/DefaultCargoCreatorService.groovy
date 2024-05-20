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
package org.klokwrk.cargotracking.domain.model.service

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.domain.model.value.Cargo
import org.klokwrk.cargotracking.domain.model.value.Commodity
import org.klokwrk.cargotracking.domain.model.value.ContainerDimensionType
import org.klokwrk.cargotracking.domain.model.value.ContainerType

import javax.measure.Quantity
import javax.measure.quantity.Mass

import static org.hamcrest.Matchers.notNullValue

/**
 * Default implementation of {@link CargoCreatorService}.
 * <p/>
 * For determining maximum allowed weight per container, it uses provided {@link MaxAllowedWeightPerContainerPolicy} service instance.
 */
@CompileStatic
class DefaultCargoCreatorService implements CargoCreatorService {
  MaxAllowedWeightPerContainerPolicy maxAllowedWeightPerContainerPolicy

  /**
   * Constructor.
   * <p/>
   * Provided {@link MaxAllowedWeightPerContainerPolicy} instance must not be {@code null}.
   */
  DefaultCargoCreatorService(MaxAllowedWeightPerContainerPolicy maxAllowedWeightPerContainerPolicy) {
    requireMatch(maxAllowedWeightPerContainerPolicy, notNullValue())
    this.maxAllowedWeightPerContainerPolicy = maxAllowedWeightPerContainerPolicy
  }

  @Override
  Cargo from(ContainerDimensionType containerDimensionType, Commodity commodity) {
    ContainerType containerType = ContainerType.find(containerDimensionType, commodity.commodityType.containerFeaturesType)
    Quantity<Mass> maxAllowedWeightPerContainerPerPolicyInKilograms = maxAllowedWeightPerContainerPolicy.maxAllowedWeightPerContainer(containerType)
    Cargo cargo = Cargo.make(containerType, commodity, maxAllowedWeightPerContainerPerPolicyInKilograms)

    // Potentially we might also want the max container TEU count per commodity type policy.
    // This is very similar policy we have in BookingOfferCargos.canAcceptCargo(). There it is the cumulative across the whole BookingOffer.
    // We will not implement this at the moment, and we'll rely on allowed weight policy only. Maybe we can introduce this later.
//    if (cargoContainerTeuCount > 5000) {
//      throw new CommandException(ViolationInfo.createForBadRequestWithCustomCodeKey("bookingOfferAggregate.cargoContainerTeuCountTooHigh"))
//    }

    return cargo
  }
}
