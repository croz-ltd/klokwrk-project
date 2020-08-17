package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.feature.cargosummary.adapter.out.persistence

import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.event.CargoBookedEvent
import org.klokwrk.cargotracker.booking.boundary.web.metadata.WebMetaDataConstant
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.cargobooking.CargoBookedEventFixtures
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.metadata.WebMetaDataFixtures
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.test.base.AbstractRdbmsProjectionIntegrationSpecification
import org.klokwrk.cargotracker.booking.queryside.test.axon.GenericDomainEventMessageFactory
import org.klokwrk.cargotracker.booking.queryside.test.feature.cargosummary.sql.CargoSummarySqlHelper
import org.klokwrk.lang.groovy.constant.CommonConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import spock.util.concurrent.PollingConditions

import javax.sql.DataSource

@SpringBootTest
@ActiveProfiles("testIntegration")
class CargoSummaryProjectorServiceIntegrationSpecification extends AbstractRdbmsProjectionIntegrationSpecification {

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

  void "should work for event message with metadata"() {
    given:
    Long startingCargoSummaryRecordsCount = CargoSummarySqlHelper.selectCurrentCargoSummaryRecordsCount(groovySql)

    CargoBookedEvent cargoBookedEvent = CargoBookedEventFixtures.eventValidConnectedViaRail()
    String aggregateIdentifier = cargoBookedEvent.aggregateIdentifier

    GenericDomainEventMessage<CargoBookedEvent> genericDomainEventMessage = GenericDomainEventMessageFactory.createEventMessage(cargoBookedEvent, WebMetaDataFixtures.metaDataMapForWebBookingChannel())
    eventBus.publish(genericDomainEventMessage)

    expect:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      CargoSummarySqlHelper.selectCurrentCargoSummaryRecordsCount(groovySql) == startingCargoSummaryRecordsCount + 1
      verifyAll(CargoSummarySqlHelper.selectCargoSummaryRecord(groovySql, aggregateIdentifier)) {
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
    Long startingCargoSummaryRecordsCount = CargoSummarySqlHelper.selectCurrentCargoSummaryRecordsCount(groovySql)

    CargoBookedEvent cargoBookedEvent = CargoBookedEventFixtures.eventValidConnectedViaRail()
    String aggregateIdentifier = cargoBookedEvent.aggregateIdentifier

    GenericDomainEventMessage<CargoBookedEvent> genericDomainEventMessage = GenericDomainEventMessageFactory.createEventMessage(cargoBookedEvent, [:])
    eventBus.publish(genericDomainEventMessage)

    expect:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      CargoSummarySqlHelper.selectCurrentCargoSummaryRecordsCount(groovySql) == startingCargoSummaryRecordsCount + 1
      verifyAll(CargoSummarySqlHelper.selectCargoSummaryRecord(groovySql, aggregateIdentifier)) {
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
