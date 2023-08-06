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
package org.klokwrk.cargotracking.domain.model.command.data

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.domain.model.value.Commodity
import org.klokwrk.cargotracking.domain.model.value.ContainerDimensionType
import org.klokwrk.lib.xlang.groovy.base.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkImmutable

import static org.hamcrest.Matchers.notNullValue

/**
 * Encapsulates cargo-related properties in CreteBookingOfferCommand.
 * <p/>
 * It is interesting to note the reasons for using CargoCommandData class instead of just using the Cargo value object.
 * <p/>
 * The Cargo value object contains multiple calculated properties, which are populated in the aggregate with the help of additional services injected into the aggregate. If we use the Cargo value
 * object instead of CargoCommandData, we will end up with a partial Cargo instance that has to be recreated when the command arrives in the aggregate. And this will work without any technical issues.
 * <p/>
 * However, to make an explicit distinction between cargo specification and complete cargo instance, we opted to use CargoCommandData. That way, we don't have to care about how to construct a Cargo
 * instance and whether or not we should use some parameters. This is very useful for potential external users (i.e., projection app) of the command API.
 * <p/>
 * Regarding dependencies, CargoCommandData belongs to the command model (commands and aggregates) and can be referenced and used from wherever the command model is allowed to be used.
 */
@KwrkImmutable
@CompileStatic
class CargoCommandData implements PostMapConstructorCheckable {
  Commodity commodity
  ContainerDimensionType containerDimensionType

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(commodity, notNullValue())
    requireMatch(containerDimensionType, notNullValue())
  }
}
