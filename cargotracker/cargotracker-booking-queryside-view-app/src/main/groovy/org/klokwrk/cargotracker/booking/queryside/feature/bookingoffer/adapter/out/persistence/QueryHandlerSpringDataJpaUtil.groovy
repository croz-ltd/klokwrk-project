/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.adapter.out.persistence

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo
import org.klokwrk.cargotracker.lib.boundary.query.api.paging.PageInfo
import org.klokwrk.cargotracker.lib.boundary.query.api.paging.PageRequirement
import org.klokwrk.cargotracker.lib.boundary.query.api.sorting.SortRequirement
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mapping.PropertyReferenceException

@CompileStatic
class QueryHandlerSpringDataJpaUtil {
  static PageRequest makePageRequestFromPageAndSortRequirements(PageRequirement pageRequirement, List<SortRequirement> sortRequirementList) {
    List<Sort.Order> sortOrderList = sortRequirementList
        .collect({ SortRequirement sortRequirement -> new Sort.Order(Sort.Direction.fromString(sortRequirement.direction.name()), sortRequirement.propertyName) })

    Sort sort = Sort.by(sortOrderList)
    PageRequest pageRequest = PageRequest.of(pageRequirement.ordinal, pageRequirement.size, sort)
    return pageRequest
  }

  static void handlePropertyReferenceException(PropertyReferenceException propertyReferenceException) {
    String messageKey = "badRequest.query.sorting.invalidProperty"
    List<String> messageParams = [propertyReferenceException.propertyName]

    throw new QueryException(ViolationInfo.makeForBadRequestWithCustomCodeKey(messageKey, messageParams))
  }

  static PageInfo makePageInfoFromPage(Page page, PageRequirement pageRequirement, List<SortRequirement> sortRequirementList) {
    PageInfo pageInfo = new PageInfo().tap {
      pageOrdinal = page.number
      pageElementsCount = page.numberOfElements

      first = page.first
      last = page.last

      totalPagesCount = page.totalPages
      totalElementsCount = page.totalElements

      requestedPageRequirement = pageRequirement
      requestedSortRequirementList = sortRequirementList
    }

    return pageInfo
  }
}
