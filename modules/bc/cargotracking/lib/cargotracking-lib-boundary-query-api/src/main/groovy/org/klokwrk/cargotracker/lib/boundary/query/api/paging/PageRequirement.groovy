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
package org.klokwrk.cargotracker.lib.boundary.query.api.paging

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.klokwrk.lib.lo.validation.group.Level1
import org.klokwrk.lib.lo.validation.group.Level2

import jakarta.validation.GroupSequence
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

/**
 * Specifies requirements for desired paged results of a query.
 * <p/>
 * We have specifications for the desired page's ordinal number (default is 0) and size (default is 25) as specified by the api client.
 */
@GroupSequence([PageRequirement, Level1, Level2])
@EqualsAndHashCode
@CompileStatic
class PageRequirement {
  static final Integer PAGE_REQUIREMENT_SIZE_DEFAULT = 25
  static final PageRequirement PAGE_REQUIREMENT_INSTANCE_DEFAULT = new PageRequirement(ordinal: 0, size: PAGE_REQUIREMENT_SIZE_DEFAULT)

  /**
   * The ordinal number of requested page.
   * <p/>
   * Must be not {@code null}, and must be greater or equal to 0. Default is 0.
   */
  @Min(value = 0L, groups = [Level2])
  @NotNull(groups = [Level1])
  Integer ordinal = 0

  /**
   * The requested number of page elements.
   * <p/>
   * Must be not {@code null}, and must be greater or equal to 1. Default is 25.
   */
  @Min(value = 1L, groups = [Level2])
  @NotNull(groups = [Level1])
  Integer size = PAGE_REQUIREMENT_SIZE_DEFAULT
}
