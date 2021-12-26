/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.quantity.QuantityRange
import tech.units.indriya.unit.Units

import javax.measure.quantity.Temperature

/**
 * Enumerates functional characteristics, or features, of different container types.
 * <p/>
 * In intention, enumerated constants are identical to the container types of ISO 6346 standard. However, we are not constrained to the ISO 6346 only. For example, we might have a custom container
 * with its own feature set and assign another enum value.
 * <p/>
 * Names of enum values for ISO container types are taken directly from the standard. For example, {@code FEATURES_ISO_G1} corresponds to the general-purpose container encoded as {@code G1}
 * in the ISO standard.
 * <p/>
 * Each {@code ContainerFeaturesType} enum value is created with a single parameter:
 * <ul>
 * <li>
 *   <b>containerTemperatureRange</b>: specifies an inclusive temperature range appropriate for this commodity type. Note that the ISO standard does not prescribe this temperature range. We are using
 *   here some values found online. When particular {@code ContainerFeatureType} does not provide cooling or heating capabilities, {@code containerTemperatureRange} is unbounded as defined by
 *   {@link ContainerFeaturesType.Constants#UNBOUNDED_TEMPERATURE_RANGE}.
 * </li>
 * </ul>
 * <p/>
 * Useful references:
 * <ul>
 * <li>https://en.wikipedia.org/wiki/ISO_6346</li>
 * <li>https://www.csiu.co/resources-and-links/iso-container-size-and-type-iso-6346</li>
 * <li>https://www.containercontainer.com/iso6346/</li>
 * <li>https://www.bic-code.org/size-and-type-code/</li>
 * <li>https://www.bic-code.org/wp-content/uploads/2018/01/SizeAndType-Table1-3.pdf</li>
 * <li>https://www.maersk.com/support/faqs/reefer-container-temperature-range</li>
 * <li>https://www.containercontainer.com/containers/reefer-refrigerated-shipping-containers/</li>
 * </ul>
 */
@CompileStatic
enum ContainerFeaturesType {
  /**
   * General-purpose ISO G1 container.
   */
  FEATURES_ISO_G1(Constants.UNBOUNDED_TEMPERATURE_RANGE),

  /**
   * Standard refrigerated (reefer) ISO R1 container. The temperature range is [-30, 30] Celsius degree inclusive.
   */
  FEATURES_ISO_R1_STANDARD_REEFER(Constants.STANDARD_REEFER_TEMPERATURE_RANGE)

  static class Constants {
    static final QuantityRange<Temperature> UNBOUNDED_TEMPERATURE_RANGE = QuantityRange.of(null, null)
    static final QuantityRange<Temperature> STANDARD_REEFER_TEMPERATURE_RANGE = QuantityRange.of(Quantities.getQuantity(-30, Units.CELSIUS), Quantities.getQuantity(30, Units.CELSIUS))
  }

  private final QuantityRange<Temperature> containerTemperatureRange

  private ContainerFeaturesType(QuantityRange<Temperature> containerTemperatureRange) {
    this.containerTemperatureRange = containerTemperatureRange
  }

  QuantityRange<Temperature> getContainerTemperatureRange() {
    return containerTemperatureRange
  }

  /**
   * Answers if this container feature type controls internal temperature or not.
   */
  Boolean isContainerTemperatureControlled() {
    if (getContainerTemperatureRange() == Constants.UNBOUNDED_TEMPERATURE_RANGE) {
      return false
    }

    return true
  }
}
