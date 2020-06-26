package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.cargosummary.domain.facade

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.axonframework.eventhandling.gateway.EventGateway
import org.klokwrk.cargotracker.booking.boundary.web.metadata.WebMetaDataConstant
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.CargoBookedEvent
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.WebMetaDataFixtures
import org.klokwrk.cargotracker.booking.domain.model.Location
import org.klokwrk.cargotracker.booking.domain.modelsample.LocationSample
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.cargosummary.test.base.AbstractCargoSummaryIntegrationSpecification
import org.klokwrk.cargotracker.lib.axon.api.event.BaseEvent
import org.klokwrk.lang.groovy.constant.CommonConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.util.concurrent.PollingConditions

import javax.sql.DataSource

@SpringBootTest
class CargoSummaryProjectorFacadeServiceIntegrationSpecification extends AbstractCargoSummaryIntegrationSpecification {

  @TestConfiguration
  static class TestSpringBootConfiguration {
    @Bean
    Sql groovySql(DataSource dataSource) {
      return new Sql(dataSource)
    }
  }

  static Long selectCurrentCargoSummaryRecordsCount(Sql groovySql) {
    GroovyRowResult groovyRowResult = groovySql.firstRow("SELECT count(*) as recordsCount from cargo_summary")
    return groovyRowResult.recordsCount as Long
  }

  static Map<String, ?> selectCargoSummaryRecord(Sql groovySql, String aggregateIdentifier) {
    List<GroovyRowResult> groovyRowResultList = groovySql.rows([aggregateIdentifier: aggregateIdentifier], "SELECT * from cargo_summary where aggregate_identifier = :aggregateIdentifier")
    return groovyRowResultList[0]
  }

  static CargoBookedEvent createCorrectCargoBookedEvent() {
    String aggregateIdentifier = UUID.randomUUID()
    Location originLocation = LocationSample.findByUnLoCode("HRRJK")
    Location destinationLocation = LocationSample.findByUnLoCode("HRZAG")
    CargoBookedEvent cargoBookedEvent = new CargoBookedEvent(aggregateIdentifier: aggregateIdentifier, originLocation: originLocation, destinationLocation: destinationLocation)

    return cargoBookedEvent
  }

  static <T extends BaseEvent> GenericDomainEventMessage createEventMessage(T event, Map<String, ?> metadataMap, Long sequenceNumber = 0) {
    GenericDomainEventMessage<T> eventMessage = new GenericDomainEventMessage<>(event.getClass().simpleName, event.aggregateIdentifier, sequenceNumber, event, metadataMap)
    return eventMessage
  }

  @Autowired
  DataSource dataSource

  @Autowired
  EventGateway eventGateway

  @Autowired
  EventBus eventBus

  @Autowired
  Sql groovySql

  void "should work for event with metadata"() {
    given:
    Long startingCargoSummaryRecordsCount = selectCurrentCargoSummaryRecordsCount(groovySql)

    CargoBookedEvent cargoBookedEvent = createCorrectCargoBookedEvent()
    String aggregateIdentifier = cargoBookedEvent.aggregateIdentifier

    GenericDomainEventMessage<CargoBookedEvent> genericDomainEventMessage = createEventMessage(cargoBookedEvent, WebMetaDataFixtures.metaDataMapForWebBookingChannel())
    eventBus.publish(genericDomainEventMessage)

    expect:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      selectCurrentCargoSummaryRecordsCount(groovySql) == startingCargoSummaryRecordsCount + 1
      verifyAll(selectCargoSummaryRecord(groovySql, aggregateIdentifier)) {
        size() == 7
        id >= 0
        aggregate_identifier == aggregateIdentifier
        aggregate_sequence_number == 0
        origin_location == "HRRJK"
        destination_location == "HRZAG"
        inbound_channel_name == WebMetaDataConstant.WEB_BOOKING_CHANNEL_NAME
        inbound_channel_type == WebMetaDataConstant.WEB_BOOKING_CHANNEL_TYPE
      }
    }
  }

  void "should work for event without metadata"() {
    given:
    Long startingCargoSummaryRecordsCount = selectCurrentCargoSummaryRecordsCount(groovySql)

    CargoBookedEvent cargoBookedEvent = createCorrectCargoBookedEvent()
    String aggregateIdentifier = cargoBookedEvent.aggregateIdentifier

    GenericDomainEventMessage<CargoBookedEvent> genericDomainEventMessage = createEventMessage(cargoBookedEvent, [:])
    eventBus.publish(genericDomainEventMessage)

    expect:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      selectCurrentCargoSummaryRecordsCount(groovySql) == startingCargoSummaryRecordsCount + 1
      verifyAll(selectCargoSummaryRecord(groovySql, aggregateIdentifier)) {
        size() == 7
        id >= 0
        aggregate_identifier == aggregateIdentifier
        aggregate_sequence_number == 0
        origin_location == "HRRJK"
        destination_location == "HRZAG"
        inbound_channel_name == CommonConstants.NOT_AVAILABLE
        inbound_channel_type == CommonConstants.NOT_AVAILABLE
      }
    }
  }
}
