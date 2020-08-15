package org.klokwrk.cargotracker.booking.queryside.cargosummary.domain.facade

import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracker.booking.queryside.cargosummary.test.base.AbstractCargoSummaryQuerySideIntegrationSpecification
import org.klokwrk.cargotracker.lib.boundary.api.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.metadata.constant.MetaDataConstant
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles

import javax.sql.DataSource

@SpringBootTest
@ActiveProfiles("testIntegration")
class CargoSummaryQueryFacadeServiceIntegrationSpecification extends AbstractCargoSummaryQuerySideIntegrationSpecification {
  @TestConfiguration
  static class TestSpringBootConfiguration {
    @Bean
    Sql groovySql(DataSource dataSource) {
      return new Sql(dataSource)
    }
  }

  @Autowired
  EventBus eventBus

  @Autowired
  Sql groovySql

  @Autowired
  CargoSummaryQueryFacadeService cargoSummaryQueryFacadeService

  void "should work for correct request - [locale: #locale]"() {
    given:
    String myAggregateIdentifier = publishAndWaitForProjectedCargoBookedEvent(eventBus, groovySql)

    CargoSummaryQueryRequest cargoSummaryQueryRequest = new CargoSummaryQueryRequest(aggregateIdentifier: myAggregateIdentifier)
    OperationRequest<CargoSummaryQueryRequest> operationRequest = new OperationRequest(
        payload: cargoSummaryQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): locale]
    )

    when:
    OperationResponse<CargoSummaryQueryResponse> operationResponse = cargoSummaryQueryFacadeService.queryCargoSummary(operationRequest)

    then:
    verifyAll(operationResponse.payload) {
      aggregateIdentifier == myAggregateIdentifier
      aggregateSequenceNumber == 0
      originLocation == "HRRJK"
      destinationLocation == "HRZAG"
    }

    verifyAll(operationResponse.metaData) {
      timestamp
      severity == Severity.INFO
      violation == null
      location == null
      titleText == null
      titleDetailedText == null
    }

    where:
    locale                         | _
    Locale.forLanguageTag("hr-HR") | _
    Locale.forLanguageTag("en")    | _
  }

  void "should throw when CargoSummary cannot be found - [locale: #locale]"() {
    given:
    CargoSummaryQueryRequest cargoSummaryQueryRequest = new CargoSummaryQueryRequest(aggregateIdentifier: UUID.randomUUID())
    OperationRequest<CargoSummaryQueryRequest> operationRequest = new OperationRequest(
        payload: cargoSummaryQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): locale]
    )

    when:
    cargoSummaryQueryFacadeService.queryCargoSummary(operationRequest)

    then:
    QueryException queryException = thrown()
    queryException.violationInfo == ViolationInfo.NOT_FOUND

    where:
    locale                         | _
    Locale.forLanguageTag("hr-HR") | _
    Locale.forLanguageTag("en")    | _
  }
}
