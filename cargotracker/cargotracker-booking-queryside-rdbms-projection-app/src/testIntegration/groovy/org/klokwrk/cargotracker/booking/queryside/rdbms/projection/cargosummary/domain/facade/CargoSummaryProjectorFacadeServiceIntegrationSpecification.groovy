package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.cargosummary.domain.facade

import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.axonframework.eventhandling.gateway.EventGateway
import org.klokwrk.cargotracker.booking.boundary.web.metadata.WebMetaDataConstant
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.CargoBookedEvent
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.cargobook.CargoBookedEventFixtures
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.metadata.WebMetaDataFixtures
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.cargosummary.test.base.AbstractCargoSummaryIntegrationSpecification
import org.klokwrk.cargotracker.booking.queryside.test.axon.GenericDomainEventMessageFactory
import org.klokwrk.cargotracker.booking.queryside.test.domain.sql.CargoSummaryQueryHelper
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

  @Autowired
  DataSource dataSource

  @Autowired
  EventGateway eventGateway

  @Autowired
  EventBus eventBus

  @Autowired
  Sql groovySql

  void "should work for event message with metadata"() {
    given:
    Long startingCargoSummaryRecordsCount = CargoSummaryQueryHelper.selectCurrentCargoSummaryRecordsCount(groovySql)

    CargoBookedEvent cargoBookedEvent = CargoBookedEventFixtures.eventValidConnectedViaRail()
    String aggregateIdentifier = cargoBookedEvent.aggregateIdentifier

    GenericDomainEventMessage<CargoBookedEvent> genericDomainEventMessage = GenericDomainEventMessageFactory.createEventMessage(cargoBookedEvent, WebMetaDataFixtures.metaDataMapForWebBookingChannel())
    eventBus.publish(genericDomainEventMessage)

    expect:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      CargoSummaryQueryHelper.selectCurrentCargoSummaryRecordsCount(groovySql) == startingCargoSummaryRecordsCount + 1
      verifyAll(CargoSummaryQueryHelper.selectCargoSummaryRecord(groovySql, aggregateIdentifier)) {
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

  void "should work for event message without metadata"() {
    given:
    Long startingCargoSummaryRecordsCount = CargoSummaryQueryHelper.selectCurrentCargoSummaryRecordsCount(groovySql)

    CargoBookedEvent cargoBookedEvent = CargoBookedEventFixtures.eventValidConnectedViaRail()
    String aggregateIdentifier = cargoBookedEvent.aggregateIdentifier

    GenericDomainEventMessage<CargoBookedEvent> genericDomainEventMessage = GenericDomainEventMessageFactory.createEventMessage(cargoBookedEvent, [:])
    eventBus.publish(genericDomainEventMessage)

    expect:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      CargoSummaryQueryHelper.selectCurrentCargoSummaryRecordsCount(groovySql) == startingCargoSummaryRecordsCount + 1
      verifyAll(CargoSummaryQueryHelper.selectCargoSummaryRecord(groovySql, aggregateIdentifier)) {
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
