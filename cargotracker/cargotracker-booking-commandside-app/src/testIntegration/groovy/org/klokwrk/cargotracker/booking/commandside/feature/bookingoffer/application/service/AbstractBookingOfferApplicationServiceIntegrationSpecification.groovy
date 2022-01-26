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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.axonframework.commandhandling.CommandBus
import org.axonframework.common.Registration
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CommodityInfoData
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandPortIn
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequest
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandResponse
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.RouteSpecificationData
import org.klokwrk.cargotracker.booking.commandside.infrastructure.springbootconfig.SpringBootConfig
import org.klokwrk.cargotracker.booking.commandside.test.base.AbstractCommandSideIntegrationSpecification
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.metadata.WebMetaDataFixtures
import org.klokwrk.cargotracker.booking.domain.model.aggregate.BookingOfferCommodities
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import org.klokwrk.cargotracker.lib.boundary.api.application.exception.RemoteHandlerException
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.lang.groovy.misc.InstantUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import java.time.Duration
import java.time.Instant

abstract class AbstractBookingOfferApplicationServiceIntegrationSpecification extends AbstractCommandSideIntegrationSpecification {
  @Autowired
  CreateBookingOfferCommandPortIn createBookingOfferCommandPortIn

  @Autowired
  CommandBus commandBus

  void "should work for correct request"() {
    given:
    Instant currentInstant = Instant.now()
    Instant currentInstantAndOneHour = currentInstant + Duration.ofHours(1)
    Instant currentInstantAndTwoHours = currentInstant + Duration.ofHours(2)
    Instant currentInstantAndThreeHours = currentInstant + Duration.ofHours(3)

    Instant currentInstantRoundedAndOneHour = InstantUtils.roundUpInstantToTheHour(currentInstantAndOneHour)
    Instant currentInstantRoundedAndTwoHours = InstantUtils.roundUpInstantToTheHour(currentInstantAndTwoHours)
    Instant currentInstantRoundedAndThreeHours = InstantUtils.roundUpInstantToTheHour(currentInstantAndThreeHours)

    String myBookingOfferIdentifier = UUID.randomUUID()
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userIdentifier: "standard-customer@cargotracker.com",
        bookingOfferIdentifier: myBookingOfferIdentifier,
        routeSpecification: new RouteSpecificationData(
            originLocation: "NLRTM", destinationLocation: "HRRJK",
            departureEarliestTime: currentInstantAndOneHour, departureLatestTime: currentInstantAndTwoHours,
            arrivalLatestTime: currentInstantAndThreeHours
        ),
        commodityInfo: new CommodityInfoData(commodityType: CommodityType.DRY.name(), totalWeightInKilograms: 1000),
        containerDimensionType: "DIMENSION_ISO_22"
    )
    Map requestMetadataMap = WebMetaDataFixtures.metaDataMapForWebBookingChannel()

    BookingOfferCommodities expectedBookingOfferCommodities = new BookingOfferCommodities()
    expectedBookingOfferCommodities.storeCommodity(Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 1000), Quantities.getQuantity(20_615, Units.KILOGRAM)))

    when:
    OperationResponse<CreateBookingOfferCommandResponse> createBookingOfferCommandOperationResponse =
        createBookingOfferCommandPortIn.createBookingOfferCommand(new OperationRequest<>(payload: createBookingOfferCommandRequest, metaData: requestMetadataMap))

    CreateBookingOfferCommandResponse createBookingOfferCommandResponsePayload = createBookingOfferCommandOperationResponse.payload
    Map createBookingOfferCommandResponseMetadata = createBookingOfferCommandOperationResponse.metaData

    then:
    createBookingOfferCommandResponseMetadata.isEmpty()
    verifyAll(createBookingOfferCommandResponsePayload) {
      bookingOfferId.identifier == myBookingOfferIdentifier

      routeSpecification.with {
        originLocation.name == "Rotterdam"
        destinationLocation.name == "Rijeka"
        departureEarliestTime == currentInstantRoundedAndOneHour
        departureLatestTime == currentInstantRoundedAndTwoHours
        arrivalLatestTime == currentInstantRoundedAndThreeHours
      }

      bookingOfferCommodities.with {
        size() == 3

        commodityTypeToCommodityMap == expectedBookingOfferCommodities.commodityTypeToCommodityMap
        totalCommodityWeight == Quantities.getQuantity(1000, Units.KILOGRAM)
        totalContainerTeuCount == 1
      }
    }
  }

  // Note (logging testing): Here we have an example of testing logging entries with pure logback, without any additional library.
  void "should retry for transient failures"() {
    given:
    // Create and start logback list appender, and add it to the logger
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    Logger abstractRetrySchedulerLogger = (Logger) LoggerFactory.getLogger("org.axonframework.commandhandling.gateway.AbstractRetryScheduler")
    abstractRetrySchedulerLogger.addAppender(listAppender)

    MessageHandlerInterceptor messageHandlerInterceptor = new MessageHandlerInterceptor() {
      Boolean firstInvocation = true

      @Override
      Object handle(UnitOfWork unitOfWork, InterceptorChain interceptorChain) throws Exception {
        if (firstInvocation) {
          firstInvocation = false
          throw new IllegalArgumentException("transient exception")
        }

        return interceptorChain.proceed()
      }
    }
    Registration handlerInterceptorRegistration = commandBus.registerHandlerInterceptor(messageHandlerInterceptor)

    String myBookingOfferIdentifier = UUID.randomUUID()
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userIdentifier: "standard-customer@cargotracker.com",
        bookingOfferIdentifier: myBookingOfferIdentifier,
        routeSpecification: new RouteSpecificationData(
            originLocation: "NLRTM", destinationLocation: "HRRJK",
            departureEarliestTime: Instant.now(), departureLatestTime: Instant.now() + Duration.ofHours(1),
            arrivalLatestTime: Instant.now() + Duration.ofHours(2)
        ),
        commodityInfo: new CommodityInfoData(commodityType: CommodityType.DRY.name(), totalWeightInKilograms: 1000),
        containerDimensionType: "DIMENSION_ISO_22"
    )
    Map requestMetadataMap = WebMetaDataFixtures.metaDataMapForWebBookingChannel()

    when:
    OperationResponse<CreateBookingOfferCommandResponse> createBookingOfferCommandOperationResponse =
        createBookingOfferCommandPortIn.createBookingOfferCommand(new OperationRequest<>(payload: createBookingOfferCommandRequest, metaData: requestMetadataMap))

    CreateBookingOfferCommandResponse createBookingOfferCommandResponsePayload = createBookingOfferCommandOperationResponse.payload
    Map createBookingOfferCommandResponseMetadata = createBookingOfferCommandOperationResponse.metaData
    List<ILoggingEvent> loggingEventList = listAppender.list

    then:
    createBookingOfferCommandResponseMetadata.isEmpty()
    createBookingOfferCommandResponsePayload.bookingOfferId.identifier == myBookingOfferIdentifier

    loggingEventList.size() == 1
    loggingEventList[0].level == Level.INFO
    loggingEventList[0].formattedMessage.contains("Processing of Command [CreateBookingOfferCommand] resulted in an exception. Will retry 2 more time(s)...")

    cleanup:
    handlerInterceptorRegistration.close()

    // detach and stop appender
    abstractRetrySchedulerLogger.detachAppender(listAppender)
    listAppender.stop()
  }

  void "should stop retrying after maxRetryCount is exceeded"() {
    given:
    Integer maxRetryCount = SpringBootConfig.MAX_RETRY_COUNT_DEFAULT

    // Create and start logback list appender, and add it to the logger
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    Logger abstractRetrySchedulerLogger = (Logger) LoggerFactory.getLogger("org.axonframework.commandhandling.gateway.AbstractRetryScheduler")
    abstractRetrySchedulerLogger.addAppender(listAppender)

    MessageHandlerInterceptor messageHandlerInterceptor = new MessageHandlerInterceptor() {
      Integer myRetryCount = 0
      Integer myMaxRetryCount = SpringBootConfig.MAX_RETRY_COUNT_DEFAULT

      @Override
      Object handle(UnitOfWork unitOfWork, InterceptorChain interceptorChain) throws Exception {
        if (myRetryCount <= myMaxRetryCount) {
          myRetryCount++
          throw new IllegalArgumentException("transient exception")
        }

        return interceptorChain.proceed()
      }
    }
    Registration handlerInterceptorRegistration = commandBus.registerHandlerInterceptor(messageHandlerInterceptor)

    String bookingOfferIdentifier = UUID.randomUUID()
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userIdentifier: "standard-customer@cargotracker.com",
        bookingOfferIdentifier: bookingOfferIdentifier,
        routeSpecification: new RouteSpecificationData(
            originLocation: "NLRTM", destinationLocation: "HRRJK",
            departureEarliestTime: Instant.now(), departureLatestTime: Instant.now() + Duration.ofHours(1),
            arrivalLatestTime: Instant.now() + Duration.ofHours(2)
        ),
        commodityInfo: new CommodityInfoData(commodityType: CommodityType.DRY.name(), totalWeightInKilograms: 1000),
        containerDimensionType: "DIMENSION_ISO_22"
    )
    Map requestMetadataMap = WebMetaDataFixtures.metaDataMapForWebBookingChannel()

    when:
    createBookingOfferCommandPortIn.createBookingOfferCommand(new OperationRequest<>(payload: createBookingOfferCommandRequest, metaData: requestMetadataMap))

    then:
    thrown(RemoteHandlerException)

    verifyAll(listAppender.list, List<ILoggingEvent>, { List<ILoggingEvent> loggingEventList ->
      loggingEventList.size() == maxRetryCount + 1

      loggingEventList[0].level == Level.INFO
      loggingEventList[0].formattedMessage.contains("Processing of Command [CreateBookingOfferCommand] resulted in an exception. Will retry 2 more time(s)...")

      loggingEventList[maxRetryCount].level == Level.INFO
      loggingEventList[maxRetryCount].formattedMessage.contains("Processing of Command [CreateBookingOfferCommand] resulted in an exception 4 times. Giving up permanently.")
    })

    cleanup:
    handlerInterceptorRegistration.close()

    // detach and stop appender
    abstractRetrySchedulerLogger.detachAppender(listAppender)
    listAppender.stop()
  }
}
