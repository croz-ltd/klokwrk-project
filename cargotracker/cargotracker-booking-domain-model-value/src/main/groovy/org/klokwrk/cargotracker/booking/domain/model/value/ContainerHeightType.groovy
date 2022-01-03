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

/**
 * Enumerates heights of a container.
 * <p/>
 * We are using some ISO 6346 heights here, but we are not constrained to use only those.
 * <p/>
 * Note that ISO 6346 second size-code character does not represent height only, but it also includes width. However, almost anything other than the standard 8-feet width is rarely used. In addition,
 * widths above 8-feet are given as ranges in the standard, making it impossible to have an exact value for the width.
 * <p/>
 * Therefore, we are using second size code characters as height codes only. If the usage of non-common widths is necessary, it is best to express those at the higher {@code ContainerDimensionType}
 * level by combining appropriate {@code ContainerHeightType} and {@code ContainerWidthType} constants.
 * <p/>
 * Useful references:
 * <ul>
 * <li>https://en.wikipedia.org/wiki/ISO_6346</li>
 * <li>https://www.bic-code.org/wp-content/uploads/2018/01/SizeAndType-Table1-3.pdf</li>
 * <li>https://www.discovercontainers.com/shipping-container-dimensions/</li>
 * <li>http://shipping-container-info.com/size-type-info/</li>
 * </ul>
 *
 * @see ContainerDimensionType
 * @see ContainerWidthType
 */
@CompileStatic
enum ContainerHeightType {
  /**
   * 8 feet 6 inches (standard).
   */
  HEIGHT_ISO_2,

  /**
   * 9 feet 6 inches (high cube).
   */
  HEIGHT_ISO_5
}
