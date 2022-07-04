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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.adapter.out.persistence

import groovy.transform.CompileStatic
import net.croz.nrich.search.api.model.AdditionalRestrictionResolver
import net.croz.nrich.search.api.model.SearchConfiguration
import net.croz.nrich.search.api.model.property.SearchPropertyConfiguration
import org.axonframework.queryhandling.QueryHandler
import org.klokwrk.cargotracker.booking.queryside.model.rdbms.jpa.BookingOfferSummaryJpaEntity
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryResponse
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryResponse
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryResponse
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.mapping.PropertyReferenceException
import org.springframework.stereotype.Service

/**
 * Implements query handling related to booking offer summary.
 * <p/>
 * Do note that there is no point for marking this class/method with <code>Transactional</code> annotations. Transaction is under control of Axon's <code>SpringTransactionManager</code> which does
 * not supports <code>Transactional</code> annotation. Rather <code>SpringTransactionManager</code> uses a single <code>TransactionDefinition</code> for all its message handlers.
 * <p/>
 * This is unfortunate since we are loosing capability to specify transaction attributes per class or method, and readOnly attribute will be quite nice to have. Fortunately, since this (and others)
 * query handler runs in standalone application, we can resolve this by configuring "global" Axon's transaction definition to be read-only.
 * <p/>
 * For more information and some resources, take a look at <code>axonTransactionManager</code> bean in <code>SpringBootConfig</code> class.
 */
@Service
@CompileStatic
class BookingOfferSummaryQueryHandlerService {
  private final BookingOfferSummaryViewJpaRepository bookingOfferSummaryViewJpaRepository

  @SuppressWarnings('SpringJavaInjectionPointsAutowiringInspection')
  BookingOfferSummaryQueryHandlerService(BookingOfferSummaryViewJpaRepository bookingOfferSummaryViewJpaRepository) {
    this.bookingOfferSummaryViewJpaRepository = bookingOfferSummaryViewJpaRepository
  }

  @QueryHandler
  BookingOfferSummaryFindByIdQueryResponse handleBookingOfferSummaryFindByIdQueryRequest(BookingOfferSummaryFindByIdQueryRequest bookingOfferSummaryFindByIdQueryRequest) {
    BookingOfferSummaryJpaEntity bookingOfferSummaryJpaEntity = bookingOfferSummaryViewJpaRepository
        .findByBookingOfferIdentifierAndCustomerIdentifier(UUID.fromString(bookingOfferSummaryFindByIdQueryRequest.bookingOfferIdentifier), bookingOfferSummaryFindByIdQueryRequest.customerIdentifier)

    if (!bookingOfferSummaryJpaEntity) {
      throw new QueryException(ViolationInfo.NOT_FOUND)
    }

    BookingOfferSummaryFindByIdQueryResponse bookingOfferSummaryFindByIdQueryResponse =
        new BookingOfferSummaryFindByIdQueryResponse(fetchBookingOfferSummaryJpaEntityProperties(bookingOfferSummaryJpaEntity))

    return bookingOfferSummaryFindByIdQueryResponse
  }

  protected Map<String, Object> fetchBookingOfferSummaryJpaEntityProperties(BookingOfferSummaryJpaEntity bookingOfferSummaryJpaEntity) {
    return bookingOfferSummaryJpaEntity.properties.tap({ it["bookingOfferIdentifier"] = bookingOfferSummaryJpaEntity.bookingOfferIdentifier.toString() })
  }

  @QueryHandler
  BookingOfferSummaryFindAllQueryResponse handleBookingOfferSummaryFindAllQueryRequest(BookingOfferSummaryFindAllQueryRequest bookingOfferSummaryFindAllQueryRequest) {
    PageRequest pageRequest =
        QueryHandlerSpringDataJpaUtil.makePageRequestFromPageAndSortRequirements(bookingOfferSummaryFindAllQueryRequest.pageRequirement, bookingOfferSummaryFindAllQueryRequest.sortRequirementList)

    Page<BookingOfferSummaryJpaEntity> pageOfBookingOfferSummaryJpaEntity = null
    try {
      pageOfBookingOfferSummaryJpaEntity = bookingOfferSummaryViewJpaRepository.findAllByCustomerIdentifier(bookingOfferSummaryFindAllQueryRequest.customerIdentifier, pageRequest)
    }
    catch (PropertyReferenceException pre) {
      QueryHandlerSpringDataJpaUtil.handlePropertyReferenceException(pre)
    }

    BookingOfferSummaryFindAllQueryResponse bookingOfferSummaryFindAllQueryResponse = new BookingOfferSummaryFindAllQueryResponse().tap {
      pageContent = pageOfBookingOfferSummaryJpaEntity.content
          .collect({ BookingOfferSummaryJpaEntity bookingOfferSummaryJpaEntity ->
              new BookingOfferSummaryFindByIdQueryResponse(fetchBookingOfferSummaryJpaEntityProperties(bookingOfferSummaryJpaEntity))
          })

      pageInfo = QueryHandlerSpringDataJpaUtil
          .makePageInfoFromPage(pageOfBookingOfferSummaryJpaEntity, bookingOfferSummaryFindAllQueryRequest.pageRequirement, bookingOfferSummaryFindAllQueryRequest.sortRequirementList)
    }

    return bookingOfferSummaryFindAllQueryResponse
  }

  @QueryHandler
  BookingOfferSummarySearchAllQueryResponse handleBookingOfferSummarySearchAllQueryRequest(BookingOfferSummarySearchAllQueryRequest bookingOfferSummarySearchAllQueryRequest) {
    SearchConfiguration<BookingOfferSummaryJpaEntity, BookingOfferSummaryJpaEntity, BookingOfferSummarySearchAllQueryRequest> searchConfiguration =
        SearchConfiguration
            .<BookingOfferSummaryJpaEntity, BookingOfferSummaryJpaEntity, BookingOfferSummarySearchAllQueryRequest>builder()
            .anyMatch(false)
            .searchPropertyConfiguration(SearchPropertyConfiguration.defaultSearchPropertyConfiguration().tap { searchIgnoredPropertyList = ["customerIdentifier"] })
            .additionalRestrictionResolverList([new BookingOfferSummaryJpaEntityAdditionalRestrictionResolver()] as List<AdditionalRestrictionResolver>) // codenarc-disable-line UnnecessaryCast
            .build()

    PageRequest pageRequest =
        QueryHandlerSpringDataJpaUtil.makePageRequestFromPageAndSortRequirements(bookingOfferSummarySearchAllQueryRequest.pageRequirement, bookingOfferSummarySearchAllQueryRequest.sortRequirementList)

    Page<BookingOfferSummaryJpaEntity> pageOfBookingOfferSummaryJpaEntity = null
    try {
      pageOfBookingOfferSummaryJpaEntity = bookingOfferSummaryViewJpaRepository.findAll(bookingOfferSummarySearchAllQueryRequest, searchConfiguration, pageRequest)
    }
    catch (PropertyReferenceException pre) {
      QueryHandlerSpringDataJpaUtil.handlePropertyReferenceException(pre)
    }

    BookingOfferSummarySearchAllQueryResponse bookingOfferSummarySearchAllQueryResponse = new BookingOfferSummarySearchAllQueryResponse().tap {
      pageContent = pageOfBookingOfferSummaryJpaEntity
          .content
          .collect({ BookingOfferSummaryJpaEntity bookingOfferSummaryJpaEntity ->
              new BookingOfferSummaryFindByIdQueryResponse(fetchBookingOfferSummaryJpaEntityProperties(bookingOfferSummaryJpaEntity))
          })

      pageInfo = QueryHandlerSpringDataJpaUtil
          .makePageInfoFromPage(pageOfBookingOfferSummaryJpaEntity, bookingOfferSummarySearchAllQueryRequest.pageRequirement, bookingOfferSummarySearchAllQueryRequest.sortRequirementList)
    }

    return bookingOfferSummarySearchAllQueryResponse
  }
}
