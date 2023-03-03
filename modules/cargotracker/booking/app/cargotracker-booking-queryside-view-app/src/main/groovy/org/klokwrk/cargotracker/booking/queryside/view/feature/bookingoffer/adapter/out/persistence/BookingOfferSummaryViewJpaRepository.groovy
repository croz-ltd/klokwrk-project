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
import net.croz.nrich.search.api.repository.SearchExecutor
import org.klokwrk.cargotracker.booking.queryside.model.rdbms.jpa.BookingOfferSummaryJpaEntity
import org.klokwrk.lib.springframework.data.jpa.repository.hibernate.ReadOnlyJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.query.Param

import javax.persistence.QueryHint

@SuppressWarnings("CodeNarc.BracesForClass")
@CompileStatic
interface BookingOfferSummaryViewJpaRepository extends
    JpaRepository<BookingOfferSummaryJpaEntity, UUID>, SearchExecutor<BookingOfferSummaryJpaEntity>, ReadOnlyJpaRepository<BookingOfferSummaryJpaEntity>
{
  @Query("""
      SELECT b FROM BookingOfferSummaryJpaEntity b
      LEFT JOIN FETCH b.commodityTypes
      WHERE
        b.bookingOfferIdentifier = :bookingOfferIdentifier
        AND b.customerId = :customerId
  """)
  BookingOfferSummaryJpaEntity findByBookingOfferIdentifierAndCustomerId(
      @Param("bookingOfferIdentifier") UUID bookingOfferIdentifier, @Param("customerId") String customerId)

  // Intended to be used in combination with findAllByBookingOfferIdentifiersAndCustomerId to avoid paging in memory while fetching a collection.
  // Reference: https://vladmihalcea.com/fix-hibernate-hhh000104-entity-fetch-pagination-warning-message/
  @Query(
      value ="""
          SELECT b.bookingOfferIdentifier FROM BookingOfferSummaryJpaEntity b
          WHERE b.customerId = :customerId
      """,
      countQuery = """
          SELECT COUNT(b.bookingOfferIdentifier)
          FROM BookingOfferSummaryJpaEntity b
          WHERE b.customerId = :customerId"""
  )
  Page<UUID> findPageOfBookingOfferIdentifiersByCustomerId(@Param("customerId") String customerId, Pageable pageable)

  // Intended to be used in combination with findPageOfBookingOfferIdentifiersByCustomerId to avoid paging in memory while fetching a collection.
  // Fixing in-memory paging: https://vladmihalcea.com/fix-hibernate-hhh000104-entity-fetch-pagination-warning-message/
  // JOIN FETCH: https://vladmihalcea.com/n-plus-1-query-problem/
  // DISTINCT JPQL keyword and QueryHint: https://vladmihalcea.com/jpql-distinct-jpa-hibernate/
  @Query("""
      SELECT DISTINCT b FROM BookingOfferSummaryJpaEntity b
      LEFT JOIN FETCH b.commodityTypes
      WHERE
        b.bookingOfferIdentifier IN :bookingOfferIdentifiers
        AND b.customerId = :customerId
  """)
  @QueryHints(@QueryHint(name = org.hibernate.jpa.QueryHints.HINT_PASS_DISTINCT_THROUGH, value = "false"))
  List<BookingOfferSummaryJpaEntity> findAllByBookingOfferIdentifiersAndCustomerId(
      @Param("bookingOfferIdentifiers") List<UUID> bookingOfferIdentifiers, @Param("customerId") String customerId)
}
