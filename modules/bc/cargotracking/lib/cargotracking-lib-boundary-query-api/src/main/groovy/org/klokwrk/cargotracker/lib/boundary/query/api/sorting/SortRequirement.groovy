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
package org.klokwrk.cargotracker.lib.boundary.query.api.sorting

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.klokwrk.lib.lo.validation.constraint.TrimmedStringConstraint
import org.klokwrk.lib.lo.validation.group.Level1
import org.klokwrk.lib.lo.validation.group.Level2
import org.klokwrk.lib.lo.validation.group.Level3

import jakarta.validation.GroupSequence
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * Specifies requirements for desired sorting of query results.
 * <p/>
 * We have specifications for the property name on which results should be sorted, and desired sort direction (default is ascending direction).
 */
@GroupSequence([SortRequirement, Level1, Level2, Level3])
@EqualsAndHashCode
@CompileStatic
class SortRequirement {
  static final SortDirection SORT_REQUIREMENT_DIRECTION_DEFAULT = SortDirection.ASC

  /**
   * The property name on which results will be sorted.
   * <p/>
   * Must not be blank and must be trimmed of whitespace.
   */
  @TrimmedStringConstraint(groups = [Level2])
  @NotBlank(groups = [Level1])
  String propertyName

  /**
   * The desired sort direction.
   * <p/>
   * Must be not {@code null}, and must be a valid element of {@link SortDirection} enum.
   */
  @NotNull(groups = [Level1])
  SortDirection direction = SORT_REQUIREMENT_DIRECTION_DEFAULT
}
