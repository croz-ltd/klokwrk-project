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
import org.springframework.data.repository.query.Param

@SuppressWarnings("CodeNarc.BracesForClass")
@CompileStatic
interface BookingOfferSummaryViewJpaRepository extends
    JpaRepository<BookingOfferSummaryJpaEntity, UUID>, SearchExecutor<BookingOfferSummaryJpaEntity>, ReadOnlyJpaRepository<BookingOfferSummaryJpaEntity>
{
  @Query("""
      SELECT b FROM BookingOfferSummaryJpaEntity b
      LEFT JOIN FETCH b.commodityTypes
      WHERE
        b.bookingOfferId = :bookingOfferId
        AND b.customerId = :customerId
  """)
  BookingOfferSummaryJpaEntity findByBookingOfferIdAndCustomerId(@Param("bookingOfferId") UUID bookingOfferId, @Param("customerId") String customerId)

  // Intended to be used in combination with findAllByBookingOfferIdAndCustomerId to avoid paging in memory while fetching a collection.
  // Reference: https://vladmihalcea.com/fix-hibernate-hhh000104-entity-fetch-pagination-warning-message/
  @Query(
      value ="""
          SELECT b.bookingOfferId FROM BookingOfferSummaryJpaEntity b
          WHERE b.customerId = :customerId
      """,
      countQuery = """
          SELECT COUNT(b.bookingOfferId)
          FROM BookingOfferSummaryJpaEntity b
          WHERE b.customerId = :customerId"""
  )
  Page<UUID> findPageOfBookingOfferIdByCustomerId(@Param("customerId") String customerId, Pageable pageable)

  // Intended to be used in combination with findPageOfBookingOfferIdsByCustomerId to avoid paging in memory while fetching a collection.
  // Fixing in-memory paging: https://vladmihalcea.com/fix-hibernate-hhh000104-entity-fetch-pagination-warning-message/
  // JOIN FETCH: https://vladmihalcea.com/n-plus-1-query-problem/
  //
  // DISTINCT JPQL keyword and QueryHint: https://vladmihalcea.com/jpql-distinct-jpa-hibernate/
  // Note: With Hibernate 6, there is no more need to use DISTINCT in JPA query (and to pass HINT_PASS_DISTINCT_THROUGH = false query hint) to filter out the same parent entity references when join
  //       fetching a child collection
  @Query("""
      SELECT b FROM BookingOfferSummaryJpaEntity b
      LEFT JOIN FETCH b.commodityTypes
      WHERE
        b.bookingOfferId IN :bookingOfferIds
        AND b.customerId = :customerId
  """)
  List<BookingOfferSummaryJpaEntity> findAllByBookingOfferIdsAndCustomerId(@Param("bookingOfferIds") List<UUID> bookingOfferIds, @Param("customerId") String customerId)
}
