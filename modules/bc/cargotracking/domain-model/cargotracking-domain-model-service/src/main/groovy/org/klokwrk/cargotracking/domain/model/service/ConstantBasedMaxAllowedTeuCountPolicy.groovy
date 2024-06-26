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

import static org.hamcrest.Matchers.notNullValue

/**
 * {@link MaxAllowedTeuCountPolicy} implementation that limits container TEU count based on provided constant.
 */
@CompileStatic
class ConstantBasedMaxAllowedTeuCountPolicy implements MaxAllowedTeuCountPolicy {
  BigDecimal maxAllowedTeuCount

  /**
   * Constructor.
   * <p/>
   * Parameter {@code maxAllowedTeuCount} must not be {@code null}.<br/>
   */
  ConstantBasedMaxAllowedTeuCountPolicy(BigDecimal maxAllowedTeuCount) {
    requireMatch(maxAllowedTeuCount, notNullValue())

    this.maxAllowedTeuCount = maxAllowedTeuCount
  }

  @Override
  boolean isTeuCountAllowed(BigDecimal teuCountToCheck) {
    if (teuCountToCheck <= maxAllowedTeuCount) {
      return true
    }

    return false
  }
}
