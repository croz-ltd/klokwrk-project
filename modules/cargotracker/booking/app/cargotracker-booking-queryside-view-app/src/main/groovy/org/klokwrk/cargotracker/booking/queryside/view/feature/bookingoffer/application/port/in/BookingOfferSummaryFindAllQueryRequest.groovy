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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.query.api.paging.PageRequirement
import org.klokwrk.cargotracker.lib.boundary.query.api.sorting.SortDirection
import org.klokwrk.cargotracker.lib.boundary.query.api.sorting.SortRequirement
import org.klokwrk.lib.validation.constraint.NotNullElementsConstraint
import org.klokwrk.lib.validation.constraint.TrimmedStringConstraint
import org.klokwrk.lib.validation.group.Level1
import org.klokwrk.lib.validation.group.Level2

import javax.validation.GroupSequence
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Null

/**
 * Request DTO parameter for {@code bookingOfferSummaryFindAllQuery} operation from {@link BookingOfferSummaryFindAllQueryPortIn} inbound port interface.
 * <p/>
 * The usage and meaning of properties {@code userIdentifier} and {@code customerId} are the same as in {@code BookingOfferSummaryFindByIdQueryRequest}. Therefore, for more details, look there.
 */
@GroupSequence([BookingOfferSummaryFindAllQueryRequest, Level1, Level2])
@CompileStatic
class BookingOfferSummaryFindAllQueryRequest {
  @TrimmedStringConstraint(groups = [Level2])
  @NotBlank(groups = [Level1])
  String userIdentifier

  @Null(groups = [Level1])
  String customerId

  /**
   * Specifies requirements for desired paged results of a query.
   * <p/>
   * Must be not {@code null}, and must be valid.
   */
  @Valid
  @NotNull(groups = [Level1])
  PageRequirement pageRequirement = PageRequirement.PAGE_REQUIREMENT_INSTANCE_DEFAULT

  /**
   * The list of requirements for desired sorting of paged results.
   * <p/>
   * Must be not {@code null}, and must be valid.
   */
  @Valid
  @NotNullElementsConstraint(groups = [Level2])
  @NotEmpty(groups = [Level1])
  List<SortRequirement> sortRequirementList = [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.DESC)]
}
