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
package org.klokwrk.cargotracking.domain.model.service

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.domain.model.value.ContainerType

import javax.measure.Quantity
import javax.measure.quantity.Mass

/**
 * Domain policy service for calculating maximum allowed weight per container.
 * <p/>
 * {@link ContainerType} already specifies maximum allowed weight for that particular container type. However, business might want to reduce that weight further to avoid overweight containers.
 */
@CompileStatic
interface MaxAllowedWeightPerContainerPolicy {
  Quantity<Mass> maxAllowedWeightPerContainer(ContainerType containerType)
}
