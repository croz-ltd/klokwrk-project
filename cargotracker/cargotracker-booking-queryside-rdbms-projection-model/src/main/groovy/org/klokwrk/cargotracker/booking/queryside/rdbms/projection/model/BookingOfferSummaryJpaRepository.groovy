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
package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model

import org.klokwrk.lib.springframework.data.jpa.repository.hibernate.HibernateJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface BookingOfferSummaryJpaRepository extends JpaRepository<BookingOfferSummaryJpaEntity, UUID>, HibernateJpaRepository<BookingOfferSummaryJpaEntity> {
  BookingOfferSummaryJpaEntity findByBookingOfferIdentifierAndCustomerIdentifier(UUID bookingOfferIdentifier, String customerIdentifier)
  Page<BookingOfferSummaryJpaEntity> findAllByCustomerIdentifier(String customerIdentifier, Pageable pageable)
}
