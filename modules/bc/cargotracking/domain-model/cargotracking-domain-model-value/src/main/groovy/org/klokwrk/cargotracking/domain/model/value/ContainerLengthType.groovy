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
package org.klokwrk.cargotracking.domain.model.value

import groovy.transform.CompileStatic

/**
 * Enumerates lengths of a container.
 * <p/>
 * We are using some ISO 6346 lengths here, but we are not constrained to use only those.
 * <p/>
 * Useful references:
 * <ul>
 * <li>https://en.wikipedia.org/wiki/ISO_6346</li>
 * <li>https://www.bic-code.org/wp-content/uploads/2018/01/SizeAndType-Table1-3.pdf</li>
 * <li>https://www.discovercontainers.com/shipping-container-dimensions/</li>
 * <li>http://shipping-container-info.com/size-type-info/</li>
 * </ul>
 */
@CompileStatic
enum ContainerLengthType {
  /**
   * 10 feet.
   */
  LENGTH_ISO_1,

  /**
   * 20 feet.
   */
  LENGTH_ISO_2,

  /**
   * 40 feet.
   */
  LENGTH_ISO_4,

  /**
   * 45 feet.
   */
  LENGTH_ISO_L
}
