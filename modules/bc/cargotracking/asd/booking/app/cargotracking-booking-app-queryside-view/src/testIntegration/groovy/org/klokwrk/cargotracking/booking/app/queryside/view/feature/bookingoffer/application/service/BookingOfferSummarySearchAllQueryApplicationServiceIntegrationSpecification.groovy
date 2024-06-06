/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.service

import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryPortIn
import org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryRequest
import org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryResponse
import org.klokwrk.cargotracking.booking.app.queryside.view.test.base.AbstractQuerySide_forFindAllAndSearchAllTests_IntegrationSpecification
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.domain.model.value.CustomerType
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracking.lib.boundary.api.domain.metadata.constant.MetaDataConstant
import org.klokwrk.cargotracking.lib.boundary.api.domain.severity.Severity
import org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationCode
import org.klokwrk.cargotracking.lib.boundary.query.api.paging.PageRequirement
import org.klokwrk.cargotracking.lib.boundary.query.api.sorting.SortDirection
import org.klokwrk.cargotracking.lib.boundary.query.api.sorting.SortRequirement
import org.spockframework.spring.EnableSharedInjection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import spock.lang.Shared

import jakarta.validation.ConstraintViolationException
import javax.sql.DataSource

@EnableSharedInjection
@SpringBootTest
@ActiveProfiles("testIntegration")
class BookingOfferSummarySearchAllQueryApplicationServiceIntegrationSpecification extends AbstractQuerySide_forFindAllAndSearchAllTests_IntegrationSpecification {
  @TestConfiguration
  static class TestSpringBootConfiguration {
    @Bean
    Sql groovySql(DataSource dataSource) {
      return new Sql(dataSource)
    }
  }

  @Shared
  @Autowired
  EventBus eventBus

  @Shared
  @Autowired
  Sql groovySql

  @Autowired
  BookingOfferSummarySearchAllQueryPortIn bookingOfferSummarySearchAllQueryPortIn

  void setup() {
    setupProjection_forFindAllAndSearchAllTests(eventBus, groovySql)
  }

  void "should work for minimal search request with default paging and sorting of request"() {
    given:
    OperationRequest<BookingOfferSummarySearchAllQueryRequest> operationRequest = new OperationRequest(
        payload: new BookingOfferSummarySearchAllQueryRequest(userId: "standard-customer@cargotracking.com"),
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferSummarySearchAllQueryResponse> operationResponse = bookingOfferSummarySearchAllQueryPortIn.bookingOfferSummarySearchAllQuery(operationRequest)

    then:
    verifyAll(operationResponse.payload.pageInfo) {
      propertiesFiltered.size() == 8
      pageOrdinal == 0
      pageElementsCount >= 8
      first
      requestedPageRequirement == PageRequirement.PAGE_REQUIREMENT_INSTANCE_DEFAULT
      requestedSortRequirementList == [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.DESC)]
    }

    verifyAll(operationResponse.payload.pageContent.first()) {
      propertiesFiltered.size() == 17

      customerType == CustomerType.STANDARD
      originLocationName == "Hamburg"
      destinationLocationName == "Rotterdam"
      totalCommodityWeight == 45000.kg
      totalContainerTeuCount == 3.00G
      lastEventSequenceNumber == 2
    }

    verifyAll(operationResponse.payload.pageContent.last()) {
      propertiesFiltered.size() == 17

      customerType == CustomerType.STANDARD
      originLocationName == null
      destinationLocationName == null
      totalCommodityWeight == null
      totalContainerTeuCount == null
      lastEventSequenceNumber == 0
    }
  }

  void "should work for minimal search request with explicit paging and sorting of request"() {
    given:
    OperationRequest<BookingOfferSummarySearchAllQueryRequest> operationRequest = new OperationRequest(
        payload: new BookingOfferSummarySearchAllQueryRequest(
            userId: "standard-customer@cargotracking.com",
            pageRequirement: new PageRequirement(ordinal: 0, size: 16),
            sortRequirementList: [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.DESC)]
        ),
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferSummarySearchAllQueryResponse> operationResponse = bookingOfferSummarySearchAllQueryPortIn.bookingOfferSummarySearchAllQuery(operationRequest)

    then:
    verifyAll(operationResponse.payload.pageInfo) {
      propertiesFiltered.size() == 8
      pageOrdinal == 0
      pageElementsCount >= 8
      first
      requestedPageRequirement == new PageRequirement(ordinal: 0, size: 16)
      requestedSortRequirementList == [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.DESC)]
    }

    verifyAll(operationResponse.payload.pageContent.first()) {
      propertiesFiltered.size() == 17

      customerType == CustomerType.STANDARD
      originLocationName == "Hamburg"
      destinationLocationName == "Rotterdam"
      totalCommodityWeight == 45000.kg
      totalContainerTeuCount == 3.00G
      lastEventSequenceNumber == 2
    }

    verifyAll(operationResponse.payload.pageContent.last()) {
      propertiesFiltered.size() == 17

      customerType == CustomerType.STANDARD
      originLocationName == "Rijeka"
      destinationLocationName == "Rotterdam"
      totalCommodityWeight == null
      totalContainerTeuCount == null
      lastEventSequenceNumber == 1
    }
  }

  void "should work for customized search request with default paging and sorting of request"() {
    given:
    OperationRequest<BookingOfferSummarySearchAllQueryRequest> operationRequest = new OperationRequest(
        payload: new BookingOfferSummarySearchAllQueryRequest(
            userId: "standard-customer@cargotracking.com",
            customerTypeSearchList: [CustomerType.STANDARD, CustomerType.GOLD],
            originLocationName: "Rijeka",
            totalCommodityWeightFromIncluding: 5_000.kg,
            totalCommodityWeightToIncluding: 50_000.kg
        ),
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferSummarySearchAllQueryResponse> operationResponse = bookingOfferSummarySearchAllQueryPortIn.bookingOfferSummarySearchAllQuery(operationRequest)

    then:
    verifyAll(operationResponse.payload.pageInfo) {
      propertiesFiltered.size() == 8
      pageOrdinal == 0
      pageElementsCount >= 5
      first
      requestedPageRequirement == PageRequirement.PAGE_REQUIREMENT_INSTANCE_DEFAULT
      requestedSortRequirementList == [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.DESC)]
    }

    verifyAll(operationResponse.payload.pageContent.first()) { // first is an equivalent of lastEventRecorded DESC
      propertiesFiltered.size() == 17

      customerType == CustomerType.STANDARD
      originLocationName == "Rijeka"
      destinationLocationName == "Los Angeles"
      totalCommodityWeight == 15000.kg
      totalContainerTeuCount == 1.00G
      lastEventSequenceNumber == 2
    }
  }

  void "should search over commodityTypes element collection with default paging and sorting of request"() {
    given:
    OperationRequest<BookingOfferSummarySearchAllQueryRequest> operationRequest = new OperationRequest(
        payload: new BookingOfferSummarySearchAllQueryRequest(
            userId: "standard-customer@cargotracking.com",
            originLocationName: "Rijeka",
            commodityTypes: [CommodityType.AIR_COOLED, CommodityType.FROZEN, CommodityType.CHILLED]
        ),
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferSummarySearchAllQueryResponse> operationResponse = bookingOfferSummarySearchAllQueryPortIn.bookingOfferSummarySearchAllQuery(operationRequest)

    then:
    verifyAll(operationResponse.payload.pageInfo) {
      propertiesFiltered.size() == 8
      pageOrdinal == 0
      pageElementsCount >= 3
      first
      requestedPageRequirement == PageRequirement.PAGE_REQUIREMENT_INSTANCE_DEFAULT
      requestedSortRequirementList == [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.DESC)]
    }

    verifyAll(operationResponse.payload.pageContent.first()) { // first is an equivalent of lastEventRecorded DESC
      propertiesFiltered.size() == 17

      customerType == CustomerType.STANDARD
      originLocationName == "Rijeka"
      destinationLocationName == "New York"
      totalCommodityWeight == 30000.kg
      totalContainerTeuCount == 2.00G
      lastEventSequenceNumber == 2
    }
  }

  void "should fail for invalid sort requirements list"() {
    given:
    OperationRequest<BookingOfferSummarySearchAllQueryRequest> operationRequest = new OperationRequest(
        payload: new BookingOfferSummarySearchAllQueryRequest(
            userId: "standard-customer@cargotracking.com",
            pageRequirement: PageRequirement.PAGE_REQUIREMENT_INSTANCE_DEFAULT,
            sortRequirementList: sortRequirementListParam
        ),
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    bookingOfferSummarySearchAllQueryPortIn.bookingOfferSummarySearchAllQuery(operationRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()
    constraintViolationException.message.startsWith("sortRequirementList: ")

    where:
    sortRequirementListParam | _
    null                     | _
    []                       | _
    [null]                   | _
  }

  void "should fail for invalid property name in sort requirements of request"() {
    given:
    OperationRequest<BookingOfferSummarySearchAllQueryRequest> operationRequest = new OperationRequest(
        payload: new BookingOfferSummarySearchAllQueryRequest(
            userId: "standard-customer@cargotracking.com",
            pageRequirement: PageRequirement.PAGE_REQUIREMENT_INSTANCE_DEFAULT,
            sortRequirementList: [new SortRequirement(propertyName: "nonExistingProperty", direction: SortDirection.ASC)]
        ),
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    bookingOfferSummarySearchAllQueryPortIn.bookingOfferSummarySearchAllQuery(operationRequest)

    then:
    QueryException queryException = thrown()

    queryException.message == "Bad Request"
    verifyAll(queryException.violationInfo, {
      severity == Severity.WARNING
      violationCode.code == ViolationCode.BAD_REQUEST.code
      violationCode.codeMessage == ViolationCode.BAD_REQUEST.codeMessage
      violationCode.resolvableMessageKey == "badRequest.query.sorting.invalidProperty"
      violationCode.resolvableMessageParameters == ["nonExistingProperty"]
    })
  }
}
