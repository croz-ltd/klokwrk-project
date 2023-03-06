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
import org.klokwrk.cargotracker.booking.queryside.model.rdbms.jpa.BookingOfferDetailsJpaEntity
import org.klokwrk.lib.springframework.data.jpa.repository.hibernate.ReadOnlyJpaRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param

@CompileStatic
interface BookingOfferDetailsViewJpaRepository extends JpaRepository<BookingOfferDetailsJpaEntity, UUID>, ReadOnlyJpaRepository<BookingOfferDetailsJpaEntity> {
  BookingOfferDetailsJpaEntity findByBookingOfferIdAndCustomerId(@Param("bookingOfferId") UUID bookingOfferId, @Param("customerId") String customerId)
}
