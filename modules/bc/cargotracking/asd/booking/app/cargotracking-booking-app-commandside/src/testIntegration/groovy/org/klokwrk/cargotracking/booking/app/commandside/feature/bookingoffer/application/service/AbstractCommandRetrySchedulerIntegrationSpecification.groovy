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
package org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.axonframework.commandhandling.CommandBus
import org.axonframework.common.Registration
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandPortIn
import org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequest
import org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandResponse
import org.klokwrk.cargotracking.booking.app.commandside.infrastructure.springbootconfig.SpringBootConfig
import org.klokwrk.cargotracking.booking.app.commandside.test.base.AbstractCommandSideIntegrationSpecification
import org.klokwrk.cargotracking.booking.lib.boundary.web.metadata.WebMetaDataFixtureBuilder
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.lib.boundary.api.application.exception.RemoteHandlerException
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.lib.xlang.groovy.base.misc.CombUuidShortPrefixUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import java.time.Duration
import java.time.Instant

abstract class AbstractCommandRetrySchedulerIntegrationSpecification extends AbstractCommandSideIntegrationSpecification {
  @Autowired
  CreateBookingOfferCommandPortIn createBookingOfferCommandPortIn

  @Autowired
  CommandBus commandBus

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

    String myBookingOfferId = CombUuidShortPrefixUtils.makeCombShortPrefix()
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "standard-customer@cargotracker.com",
        bookingOfferId: myBookingOfferId,
        routeSpecification: new org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.data.RouteSpecificationRequestData(
            originLocation: "NLRTM", destinationLocation: "HRRJK",
            departureEarliestTime: Instant.now(), departureLatestTime: Instant.now() + Duration.ofHours(1),
            arrivalLatestTime: Instant.now() + Duration.ofHours(2)
        ),
        cargos: [new org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.data.CargoRequestData(commodityType: CommodityType.DRY.name(), commodityWeight: 1000.kg, containerDimensionType: "DIMENSION_ISO_22") ]
    )
    Map requestMetadataMap = WebMetaDataFixtureBuilder.webMetaData_booking_default().build()

    when:
    OperationResponse<CreateBookingOfferCommandResponse> createBookingOfferCommandOperationResponse =
        createBookingOfferCommandPortIn.createBookingOfferCommand(new OperationRequest<>(payload: createBookingOfferCommandRequest, metaData: requestMetadataMap))

    CreateBookingOfferCommandResponse createBookingOfferCommandResponsePayload = createBookingOfferCommandOperationResponse.payload
    Map createBookingOfferCommandResponseMetadata = createBookingOfferCommandOperationResponse.metaData
    List<ILoggingEvent> loggingEventList = listAppender.list

    then:
    createBookingOfferCommandResponseMetadata.isEmpty()
    createBookingOfferCommandResponsePayload.bookingOfferId.identifier == myBookingOfferId

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

    String bookingOfferId = CombUuidShortPrefixUtils.makeCombShortPrefix()
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "standard-customer@cargotracker.com",
        bookingOfferId: bookingOfferId,
        routeSpecification: new org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.data.RouteSpecificationRequestData(
            originLocation: "NLRTM", destinationLocation: "HRRJK",
            departureEarliestTime: Instant.now(), departureLatestTime: Instant.now() + Duration.ofHours(1),
            arrivalLatestTime: Instant.now() + Duration.ofHours(2)
        ),
        cargos: [new org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.data.CargoRequestData(commodityType: CommodityType.DRY.name(), commodityWeight: 1000.kg, containerDimensionType: "DIMENSION_ISO_22")]
    )
    Map requestMetadataMap = WebMetaDataFixtureBuilder.webMetaData_booking_default().build()

    when:
    createBookingOfferCommandPortIn.createBookingOfferCommand(new OperationRequest<>(payload: createBookingOfferCommandRequest, metaData: requestMetadataMap))

    then:
    thrown(RemoteHandlerException)

    verifyAll(listAppender.list, List<ILoggingEvent>, { List<ILoggingEvent> loggingEventList ->
      loggingEventList.size() == maxRetryCount + 1

      loggingEventList[0].level == Level.INFO
      loggingEventList[0].formattedMessage.contains("Processing of Command [CreateBookingOfferCommand] resulted in an exception. Will retry 2 more time(s)...")

      loggingEventList[maxRetryCount].level == Level.WARN
      loggingEventList[maxRetryCount].formattedMessage.contains("Processing of Command [CreateBookingOfferCommand] resulted in an exception 4 times. Giving up permanently.")
    })

    cleanup:
    handlerInterceptorRegistration.close()

    // detach and stop appender
    abstractRetrySchedulerLogger.detachAppender(listAppender)
    listAppender.stop()
  }
}
