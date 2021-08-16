/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.axonframework.commandhandling.CommandBus
import org.axonframework.common.Registration
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoPortIn
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoRequest
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoResponse
import org.klokwrk.cargotracker.booking.commandside.infrastructure.springbootconfig.SpringBootConfig
import org.klokwrk.cargotracker.booking.commandside.test.base.AbstractCommandSideIntegrationSpecification
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.metadata.WebMetaDataFixtures
import org.klokwrk.cargotracker.lib.boundary.api.exception.RemoteHandlerException
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractCargoBookingApplicationServiceIntegrationSpecification extends AbstractCommandSideIntegrationSpecification {
  @Autowired
  BookCargoPortIn bookCargoPortIn

  @Autowired
  CommandBus commandBus

  void "should work for correct request"() {
    given:
    String myAggregateIdentifier = UUID.randomUUID()
    BookCargoRequest bookCargoRequest = new BookCargoRequest(aggregateIdentifier: myAggregateIdentifier, originLocation: "HRZAG", destinationLocation: "HRRJK")
    Map requestMetadataMap = WebMetaDataFixtures.metaDataMapForWebBookingChannel()

    when:
    OperationResponse<BookCargoResponse> bookCargoOperationResponse = bookCargoPortIn.bookCargo(new OperationRequest<>(payload: bookCargoRequest, metaData: requestMetadataMap))
    BookCargoResponse bookCargoResponsePayload = bookCargoOperationResponse.payload
    Map bookCargoResponseMetadata = bookCargoOperationResponse.metaData

    then:
    bookCargoResponseMetadata.isEmpty()
    verifyAll(bookCargoResponsePayload) {
      aggregateIdentifier == myAggregateIdentifier
      originLocation.name == "Zagreb"
      destinationLocation.name == "Rijeka"
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

    Boolean firstInvocation = true
    Registration handlerInterceptorRegistration = commandBus.registerHandlerInterceptor({ UnitOfWork unitOfWork, InterceptorChain interceptorChain ->
      if (firstInvocation) {
        firstInvocation = false
        throw new IllegalArgumentException("transient exception")
      }

      return interceptorChain.proceed()
    })

    String myAggregateIdentifier = UUID.randomUUID()
    BookCargoRequest bookCargoRequest = new BookCargoRequest(aggregateIdentifier: myAggregateIdentifier, originLocation: "HRZAG", destinationLocation: "HRRJK")
    Map requestMetadataMap = WebMetaDataFixtures.metaDataMapForWebBookingChannel()

    when:
    OperationResponse<BookCargoResponse> bookCargoOperationResponse = bookCargoPortIn.bookCargo(new OperationRequest<>(payload: bookCargoRequest, metaData: requestMetadataMap))
    BookCargoResponse bookCargoResponsePayload = bookCargoOperationResponse.payload
    Map bookCargoResponseMetadata = bookCargoOperationResponse.metaData
    List<ILoggingEvent> loggingEventList = listAppender.list

    then:
    bookCargoResponseMetadata.isEmpty()
    bookCargoResponsePayload.aggregateIdentifier == myAggregateIdentifier

    loggingEventList.size() == 1
    loggingEventList[0].level == Level.INFO
    loggingEventList[0].formattedMessage.contains("Processing of Command [BookCargoCommand] resulted in an exception. Will retry 2 more time(s)...")

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

    Integer retryCount = 0
    Registration handlerInterceptorRegistration = commandBus.registerHandlerInterceptor({ UnitOfWork unitOfWork, InterceptorChain interceptorChain ->
      if (retryCount <= maxRetryCount) {
        retryCount++
        throw new IllegalArgumentException("transient exception")
      }

      return interceptorChain.proceed()
    })

    String myAggregateIdentifier = UUID.randomUUID()
    BookCargoRequest bookCargoRequest = new BookCargoRequest(aggregateIdentifier: myAggregateIdentifier, originLocation: "HRZAG", destinationLocation: "HRRJK")
    Map requestMetadataMap = WebMetaDataFixtures.metaDataMapForWebBookingChannel()

    when:
    bookCargoPortIn.bookCargo(new OperationRequest<>(payload: bookCargoRequest, metaData: requestMetadataMap))

    then:
    thrown(RemoteHandlerException)

    verifyAll(listAppender.list, List<ILoggingEvent>, { List<ILoggingEvent> loggingEventList ->
      loggingEventList.size() == maxRetryCount + 1

      loggingEventList[0].level == Level.INFO
      loggingEventList[0].formattedMessage.contains("Processing of Command [BookCargoCommand] resulted in an exception. Will retry 2 more time(s)...")

      loggingEventList[maxRetryCount].level == Level.INFO
      loggingEventList[maxRetryCount].formattedMessage.contains("Processing of Command [BookCargoCommand] resulted in an exception 4 times. Giving up permanently.")
    })

    cleanup:
    handlerInterceptorRegistration.close()

    // detach and stop appender
    abstractRetrySchedulerLogger.detachAppender(listAppender)
    listAppender.stop()
  }
}
