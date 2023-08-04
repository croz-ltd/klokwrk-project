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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.adapter.out.persistence

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationInfo
import org.klokwrk.cargotracking.lib.boundary.query.api.paging.PageInfo
import org.klokwrk.cargotracking.lib.boundary.query.api.paging.PageRequirement
import org.klokwrk.cargotracking.lib.boundary.query.api.sorting.SortRequirement
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mapping.PropertyReferenceException

import java.util.regex.Matcher
import java.util.regex.Pattern

@CompileStatic
class QueryHandlerSpringDataJpaUtil {
  private final static Pattern QUERY_EXCEPTION_PROPERTY_NAME_PATTERN = Pattern.compile(/(?s)^Could.not.resolve.attribute\s'(\w*)'\sof\s'.*/)
  private static final String INVALID_PROPERTY_MESSAGE_KEY = "badRequest.query.sorting.invalidProperty"

  static PageRequest makePageRequestFromPageAndSortRequirements(PageRequirement pageRequirement, List<SortRequirement> sortRequirementList) {
    List<Sort.Order> sortOrderList = sortRequirementList
        .collect({ SortRequirement sortRequirement -> new Sort.Order(Sort.Direction.fromString(sortRequirement.direction.name()), sortRequirement.propertyName) })

    Sort sort = Sort.by(sortOrderList)
    PageRequest pageRequest = PageRequest.of(pageRequirement.ordinal, pageRequirement.size, sort)
    return pageRequest
  }

  static QueryException makeQueryExceptionFromPropertyReferenceException(PropertyReferenceException propertyReferenceException) {
    List<String> messageParams = [propertyReferenceException.propertyName]
    return new QueryException(ViolationInfo.makeForBadRequestWithCustomCodeKey(INVALID_PROPERTY_MESSAGE_KEY, messageParams))
  }

  static QueryException makeQueryExceptionFromInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException invalidDataAccessApiUsageException) {
    if (invalidDataAccessApiUsageException.rootCause?.message?.startsWith("Could not resolve attribute '")) {
      String propertyName = "unknown"
      Matcher matcher = QUERY_EXCEPTION_PROPERTY_NAME_PATTERN.matcher(invalidDataAccessApiUsageException.rootCause.message)
      if (matcher.matches()) {
        propertyName = matcher.group(1)
      }

      List<String> messageParams = [propertyName]
      return new QueryException(ViolationInfo.makeForBadRequestWithCustomCodeKey(INVALID_PROPERTY_MESSAGE_KEY, messageParams))
    }

    throw invalidDataAccessApiUsageException
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
