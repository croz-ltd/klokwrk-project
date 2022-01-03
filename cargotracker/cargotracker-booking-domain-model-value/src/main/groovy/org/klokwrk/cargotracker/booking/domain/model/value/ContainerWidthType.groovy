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
 * Enumerates widths of a container.
 * <p/>
 * The second size-code character of ISO 6346 standard encodes both height and width. Characters of 0 through 9 correspond to the 8-feet width, while the rest correspond to the larger width
 * <b>ranges</b>.
 * <p/>
 * Here we use widths constants with names independent from the second size-code character from the standard. That way, we can potentially combine non-common widths expressed here with heights at the
 * higher level of {@code ContainerDimensionType}.
 *
 * @see ContainerDimensionType
 * @see ContainerHeightType
 */
@CompileStatic
enum ContainerWidthType {
  /**
   * Standard 8 feet width.
   */
  WIDTH_ISO_STANDARD
}
