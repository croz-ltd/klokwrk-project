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
import net.croz.nrich.search.api.model.AdditionalRestrictionResolver
import org.klokwrk.cargotracker.booking.queryside.model.rdbms.jpa.BookingOfferSummaryJpaEntity
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryRequest

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@SuppressWarnings("CodeNarc.BracesForClass")
@CompileStatic
class BookingOfferSummaryJpaEntityAdditionalRestrictionResolver implements
    AdditionalRestrictionResolver<BookingOfferSummaryJpaEntity, BookingOfferSummaryJpaEntity, BookingOfferSummarySearchAllQueryRequest>
{
  @Override
  List<Predicate> resolvePredicateList(
      CriteriaBuilder criteriaBuilder, CriteriaQuery<BookingOfferSummaryJpaEntity> query, Root<BookingOfferSummaryJpaEntity> root, BookingOfferSummarySearchAllQueryRequest request)
  {
    return [criteriaBuilder.equal(root.get("customerIdentifier"), request.customerIdentifier)]
  }
}
