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
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.domain.model.value.CustomerType
import org.klokwrk.cargotracking.lib.boundary.query.api.paging.PageRequirement
import org.klokwrk.cargotracking.lib.boundary.query.api.sorting.SortDirection
import org.klokwrk.cargotracking.lib.boundary.query.api.sorting.SortRequirement
import org.klokwrk.lib.lo.validation.constraint.NotBlankWhenNullableConstraint
import org.klokwrk.lib.lo.validation.constraint.NotEmptyWhenNullableConstraint
import org.klokwrk.lib.lo.validation.constraint.NotNullElementsConstraint
import org.klokwrk.lib.lo.validation.constraint.TrimmedStringConstraint
import org.klokwrk.lib.lo.validation.constraint.uom.QuantityMinConstraint
import org.klokwrk.lib.lo.validation.group.Level1
import org.klokwrk.lib.lo.validation.group.Level2

import jakarta.validation.GroupSequence
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Null
import javax.measure.Quantity
import javax.measure.quantity.Mass
import java.time.Instant

@GroupSequence([BookingOfferSummarySearchAllQueryRequest, Level1, Level2])
@CompileStatic
class BookingOfferSummarySearchAllQueryRequest {
  @TrimmedStringConstraint(groups = [Level2])
  @NotBlank(groups = [Level1])
  String userId

  @Null(groups = [Level1])
  String customerId

  // Note: Groovy 3 did not output in bytecode the annotation of generic type (@NotNull in List<@NotNull CustomerType>). This feature is introduced in Groovy 4.
  //       Although we have upgraded to Groovy 4, we will stay on our own @NotNullElementsConstraint annotation for time being.
  @SuppressWarnings("unused")
  @NotNullElementsConstraint(groups = [Level2])
  @NotEmptyWhenNullableConstraint(groups = [Level1])
  List<CustomerType> customerTypeSearchList

  @SuppressWarnings("unused")
  @TrimmedStringConstraint(groups = [Level2])
  @NotBlankWhenNullableConstraint(groups = [Level1])
  String originLocationName

  @SuppressWarnings("unused")
  @TrimmedStringConstraint(groups = [Level2])
  @NotBlankWhenNullableConstraint(groups = [Level1])
  String originLocationCountryName

  @SuppressWarnings("unused")
  @TrimmedStringConstraint(groups = [Level2])
  @NotBlankWhenNullableConstraint(groups = [Level1])
  String destinationLocationName

  @SuppressWarnings("unused")
  @TrimmedStringConstraint(groups = [Level2])
  @NotBlankWhenNullableConstraint(groups = [Level1])
  String destinationLocationCountryName

  @SuppressWarnings("unused")
  @NotNullElementsConstraint(groups = [Level2])
  @NotEmptyWhenNullableConstraint(groups = [Level1])
  Set<CommodityType> commodityTypes

  @Null
  Long totalCommodityWeightKgFromIncluding

  @QuantityMinConstraint(minQuantity = "1 kg", groups = [Level1])
  Quantity<Mass> totalCommodityWeightFromIncluding

  @Null
  Long totalCommodityWeightKgToIncluding

  // Here we should validate if ToIncluding is greater than or equal to FromIncluding.
  // However, as the only consequence is execution of a single query returning an empty result we didn't implement this check.
  @QuantityMinConstraint(minQuantity = "1 kg", groups = [Level1])
  Quantity<Mass> totalCommodityWeightToIncluding

  @SuppressWarnings("unused")
  @Min(groups = [Level1], value = 0L)
  BigDecimal totalContainerTeuCountFromIncluding

  @SuppressWarnings("unused")
  @Min(groups = [Level1], value = 0L)
  BigDecimal totalContainerTeuCountToIncluding

  @SuppressWarnings("unused")
  Instant firstEventRecordedAtFromIncluding

  @SuppressWarnings("unused")
  Instant firstEventRecordedAtToIncluding

  @SuppressWarnings("unused")
  Instant lastEventRecordedAtFromIncluding

  @SuppressWarnings("unused")
  Instant lastEventRecordedAtToIncluding

  @Valid
  @NotNull(groups = [Level1])
  PageRequirement pageRequirement = PageRequirement.PAGE_REQUIREMENT_INSTANCE_DEFAULT

  @Valid
  @NotNullElementsConstraint(groups = [Level2])
  @NotEmpty(groups = [Level1])
  List<SortRequirement> sortRequirementList = [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.DESC)]
}
