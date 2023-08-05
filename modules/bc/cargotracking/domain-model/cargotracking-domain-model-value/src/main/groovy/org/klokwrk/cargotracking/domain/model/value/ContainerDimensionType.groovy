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
package org.klokwrk.cargotracking.domain.model.value

import groovy.transform.CompileStatic

/**
 * Enumerates different dimension combinations of containers.
 * <p/>
 * Although we are using mainly ISO 6346 size codes here, we are not constrained by the standard, and we can express completely custom dimensions if needed.
 * <p/>
 * ISO 6346 provides only a single code for both height and width. In addition, some code values correspond to the exact widths while others refer to the width ranges. As we want exactness and
 * flexibility, heights and widths are combined explicitly.
 * <p/>
 * If we need to use some non-common width, we can do it easily. However, we should care about the constants naming in the process. For example, for a container height of 9 feet and a width of 2500
 * millimeters, we can have a constant of {@code DIMENSION_ISO_2D(LENGTH_ISO_2, HEIGHT_ISO_4, WIDTH_ISO_2500MM)}.
 * <p/>
 * {@code ContainerDimensionType} also encapsulates appropriate Twenty-foot Equivalent Unit (TEU) value.
 * <p/>
 * Useful references:
 * <ul>
 * <li>https://en.wikipedia.org/wiki/ISO_6346</li>
 * <li>https://www.bic-code.org/wp-content/uploads/2018/01/SizeAndType-Table1-3.pdf</li>
 * <li>https://www.discovercontainers.com/shipping-container-dimensions/</li>
 * <li>https://www.freightright.com/kb/teu</li>
 * </ul>
 *
 * @see ContainerLengthType
 * @see ContainerHeightType
 * @see ContainerWidthType
 */
@CompileStatic
enum ContainerDimensionType {
  /**
   * 10 feet long x 8 feet 6 inches high x 8 feet width.
   */
  DIMENSION_ISO_12(ContainerLengthType.LENGTH_ISO_1, ContainerHeightType.HEIGHT_ISO_2, ContainerWidthType.WIDTH_ISO_STANDARD, 0.50G),

  /**
   * 20 feet long x 8 feet 6 inches high x 8 feet width.
   */
  DIMENSION_ISO_22(ContainerLengthType.LENGTH_ISO_2, ContainerHeightType.HEIGHT_ISO_2, ContainerWidthType.WIDTH_ISO_STANDARD, 1.00G),

  /**
   * 40 feet long x 8 feet 6 inches high x 8 feet width.
   */
  DIMENSION_ISO_42(ContainerLengthType.LENGTH_ISO_4, ContainerHeightType.HEIGHT_ISO_2, ContainerWidthType.WIDTH_ISO_STANDARD, 2.00G)

  private final ContainerLengthType length
  private final ContainerHeightType height
  private final ContainerWidthType width

  // Twenty-foot Equivalent Unit
  private final BigDecimal teu

  private ContainerDimensionType(ContainerLengthType length, ContainerHeightType height, ContainerWidthType width, BigDecimal teu) {
    this.length = length
    this.height = height
    this.width = width
    this.teu =  teu
  }

  BigDecimal getTeu() {
    return teu
  }
}
