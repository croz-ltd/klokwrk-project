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
import groovy.transform.TupleConstructor
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
import org.klokwrk.lib.uom.format.KwrkQuantityFormat
import org.springframework.dao.InvalidDataAccessApiUsageException
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
  private static final String BOOKING_OFFER_IDENTIFIER = "bookingOfferIdentifier"
  private static final String TOTAL_COMMODITY_WEIGHT = "totalCommodityWeight"

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
    return bookingOfferSummaryJpaEntity.properties.tap({
      it[this.BOOKING_OFFER_IDENTIFIER] = bookingOfferSummaryJpaEntity.bookingOfferIdentifier.toString()
      it[this.TOTAL_COMMODITY_WEIGHT] = KwrkQuantityFormat.instance.parse(bookingOfferSummaryJpaEntity.totalCommodityWeight)
    })
  }

  // Implementation notes:
  // Querying for JPA entities with contained collections is complicated. There are several serious pitfalls, such as the n+1 problem, paging in memory, and the unnecessary usage of a distinct SQL
  // clause.
  //
  // We can fix the n+1 problem by using JPA JOIN FETCH, which will load collections from a single SQL query. Details can be found here: https://vladmihalcea.com/n-plus-1-query-problem/
  //
  // Paging in memory can be fixed by splitting a query into two. The first query retrieves entity identifiers only, while the second query loads complete entities based on previously fetched
  // identifiers. Details can be found here: https://vladmihalcea.com/fix-hibernate-hhh000104-entity-fetch-pagination-warning-message/
  //
  // Unnecessary SQL distinct (with related performance implications) is a consequence of required JPA distinct in queries using a JOIN to avoid duplicate entities in the final resultset. We must use
  // query hints to remove SQL distinct from the query. Details can be found here: https://vladmihalcea.com/jpql-distinct-jpa-hibernate/
  //
  // In general, we should use all three pieces of advice to write correct and performant JPA queries for entities with collections.
  //
  @QueryHandler
  BookingOfferSummaryFindAllQueryResponse handleBookingOfferSummaryFindAllQueryRequest(BookingOfferSummaryFindAllQueryRequest bookingOfferSummaryFindAllQueryRequest) {
    PageRequest pageRequest =
        QueryHandlerSpringDataJpaUtil.makePageRequestFromPageAndSortRequirements(bookingOfferSummaryFindAllQueryRequest.pageRequirement, bookingOfferSummaryFindAllQueryRequest.sortRequirementList)

    Page<UUID> pageOfBookingOfferIdentifiers = null
    try {
      pageOfBookingOfferIdentifiers =
          bookingOfferSummaryViewJpaRepository.findPageOfBookingOfferIdentifiersByCustomerIdentifier(bookingOfferSummaryFindAllQueryRequest.customerIdentifier, pageRequest)
    }
    catch (InvalidDataAccessApiUsageException idaaue) {
      throw QueryHandlerSpringDataJpaUtil.makeQueryExceptionFromInvalidDataAccessApiUsageException(idaaue)
    }

    List<UUID> foundBookingOfferIdentifiers = pageOfBookingOfferIdentifiers.content

    List<BookingOfferSummaryJpaEntity> foundBookingOfferSummaryJpaEntities =
        bookingOfferSummaryViewJpaRepository.findAllByBookingOfferIdentifiersAndCustomerIdentifier(foundBookingOfferIdentifiers, bookingOfferSummaryFindAllQueryRequest.customerIdentifier)

    BookingOfferSummaryFindAllQueryResponse bookingOfferSummaryFindAllQueryResponse = new BookingOfferSummaryFindAllQueryResponse().tap {
      pageContent = foundBookingOfferIdentifiers
          .collect({ UUID bookingOfferIdentifier ->
            BookingOfferSummaryJpaEntity myBookingOfferSummaryJpaEntity = foundBookingOfferSummaryJpaEntities
                .find({ BookingOfferSummaryJpaEntity foundBookingOfferSummaryJpaEntity -> foundBookingOfferSummaryJpaEntity.bookingOfferIdentifier == bookingOfferIdentifier })

            new BookingOfferSummaryFindByIdQueryResponse(fetchBookingOfferSummaryJpaEntityProperties(myBookingOfferSummaryJpaEntity))
          })

      pageInfo = QueryHandlerSpringDataJpaUtil
          .makePageInfoFromPage(pageOfBookingOfferIdentifiers, bookingOfferSummaryFindAllQueryRequest.pageRequirement, bookingOfferSummaryFindAllQueryRequest.sortRequirementList)
    }

    return bookingOfferSummaryFindAllQueryResponse
  }

  @TupleConstructor
  static class BookingOfferIdentifierDto {
    UUID bookingOfferIdentifier
  }

  @QueryHandler
  BookingOfferSummarySearchAllQueryResponse handleBookingOfferSummarySearchAllQueryRequest(BookingOfferSummarySearchAllQueryRequest bookingOfferSummarySearchAllQueryRequest) {
    SearchConfiguration<BookingOfferSummaryJpaEntity, BookingOfferIdentifierDto, BookingOfferSummarySearchAllQueryRequest> searchConfiguration = SearchConfiguration
        .<BookingOfferSummaryJpaEntity, BookingOfferIdentifierDto, BookingOfferSummarySearchAllQueryRequest>builder()
        .resultClass(BookingOfferIdentifierDto)
        .anyMatch(false)
        .searchPropertyConfiguration(
            SearchPropertyConfiguration
                .defaultSearchPropertyConfiguration()
                .tap({ searchIgnoredPropertyList = ["customerIdentifier", "totalCommodityWeightFromIncluding", "totalCommodityWeightToIncluding"] })
        )
        .additionalRestrictionResolverList([new BookingOfferSummaryJpaEntityAdditionalRestrictionResolver()] as List<AdditionalRestrictionResolver>) // codenarc-disable-line UnnecessaryCast
        .build()

    PageRequest pageRequest =
        QueryHandlerSpringDataJpaUtil.makePageRequestFromPageAndSortRequirements(bookingOfferSummarySearchAllQueryRequest.pageRequirement, bookingOfferSummarySearchAllQueryRequest.sortRequirementList)

    Page<BookingOfferIdentifierDto> pageOfBookingOfferIdentifierDtos = null
    try {
      pageOfBookingOfferIdentifierDtos = bookingOfferSummaryViewJpaRepository.findAll(bookingOfferSummarySearchAllQueryRequest, searchConfiguration, pageRequest)
    }
    catch (PropertyReferenceException pre) {
      throw QueryHandlerSpringDataJpaUtil.makeQueryExceptionFromPropertyReferenceException(pre)
    }

    List<UUID> foundBookingOfferIdentifiers = (pageOfBookingOfferIdentifierDtos.content as List<BookingOfferIdentifierDto>).collect({ BookingOfferIdentifierDto dto -> dto.bookingOfferIdentifier })
    List<BookingOfferSummaryJpaEntity> foundBookingOfferSummaryJpaEntities =
        bookingOfferSummaryViewJpaRepository.findAllByBookingOfferIdentifiersAndCustomerIdentifier(foundBookingOfferIdentifiers, bookingOfferSummarySearchAllQueryRequest.customerIdentifier)

    BookingOfferSummarySearchAllQueryResponse bookingOfferSummarySearchAllQueryResponse = new BookingOfferSummarySearchAllQueryResponse().tap {
      pageContent = foundBookingOfferIdentifiers
          .collect({ UUID bookingOfferIdentifier ->
            BookingOfferSummaryJpaEntity myBookingOfferSummaryJpaEntity = foundBookingOfferSummaryJpaEntities
                .find({ BookingOfferSummaryJpaEntity foundBookingOfferSummaryJpaEntity -> foundBookingOfferSummaryJpaEntity.bookingOfferIdentifier == bookingOfferIdentifier })

            new BookingOfferSummaryFindByIdQueryResponse(fetchBookingOfferSummaryJpaEntityProperties(myBookingOfferSummaryJpaEntity))
          })

      pageInfo = QueryHandlerSpringDataJpaUtil
          .makePageInfoFromPage(pageOfBookingOfferIdentifierDtos, bookingOfferSummarySearchAllQueryRequest.pageRequirement, bookingOfferSummarySearchAllQueryRequest.sortRequirementList)
    }

    return bookingOfferSummarySearchAllQueryResponse
  }
}
