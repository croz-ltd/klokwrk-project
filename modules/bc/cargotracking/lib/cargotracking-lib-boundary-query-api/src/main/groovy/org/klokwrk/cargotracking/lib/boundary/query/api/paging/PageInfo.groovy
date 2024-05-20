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
package org.klokwrk.cargotracking.lib.boundary.query.api.paging

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.cargotracking.lib.boundary.query.api.sorting.SortRequirement
import org.klokwrk.lib.xlang.groovy.base.transform.options.RelaxedPropertyHandler

/**
 * Info about paged response.
 * <p/>
 * Intended to be used as a part of response containing paged data.
 */
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class PageInfo {
  /**
   * The ordinal number of the page.
   */
  Integer pageOrdinal

  /**
   * The number of contained page elements.
   */
  Integer pageElementsCount

  /**
   * Whether this is the first page.
   */
  Boolean first

  /**
   * Whether this is the last page.
   */
  Boolean last

  /**
   * The total number of all available pages.
   * <p/>
   * If data source does not support paging (or the paging was not requested), it is -1. Default is -1.
   */
  Integer totalPagesCount = -1

  /**
   * The total number of all data elements available for paging.
   * <p/>
   * If data source does not support paging (or the paging was not requested), it is -1. Default is -1.
   */
  Long totalElementsCount = -1

  /**
   * The page requirement sent when this page was requested.
   */
  PageRequirement requestedPageRequirement

  /**
   * The list of sort requirements sent when this page was requested.
   */
  List<SortRequirement> requestedSortRequirementList
}
