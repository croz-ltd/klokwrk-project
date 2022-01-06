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
package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.service

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.gateway.CommandGateway
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.CreateBookingOfferCommandPortIn
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoCommandRequest
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoCommandResponse
import org.klokwrk.cargotracker.booking.domain.model.aggregate.BookingOfferAggregate
import org.klokwrk.cargotracker.booking.domain.model.command.CreateBookingOfferCommand
import org.klokwrk.cargotracker.lib.axon.cqrs.command.CommandGatewayAdapter
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.lib.validation.springboot.ValidationService
import org.springframework.stereotype.Service

import static org.hamcrest.Matchers.notNullValue

@Service
@CompileStatic
class CargoBookingApplicationService implements CreateBookingOfferCommandPortIn {
  private final CargoBookingFactoryService cargoBookingFactoryService
  private final CommandGatewayAdapter commandGatewayAdapter
  private final ValidationService validationService

  CargoBookingApplicationService(ValidationService validationService, CommandGateway commandGateway, CargoBookingFactoryService cargoBookingFactoryService) {
    this.validationService = validationService
    this.commandGatewayAdapter = new CommandGatewayAdapter(commandGateway)
    this.cargoBookingFactoryService = cargoBookingFactoryService
  }

  @Override
  OperationResponse<BookCargoCommandResponse> createBookingOfferCommand(OperationRequest<BookCargoCommandRequest> bookCargoCommandOperationRequest) {
    requireMatch(bookCargoCommandOperationRequest, notNullValue())
    validationService.validate(bookCargoCommandOperationRequest.payload)

    CreateBookingOfferCommand createBookingOfferCommand = cargoBookingFactoryService.makeCreateBookingOfferCommand(bookCargoCommandOperationRequest.payload)
    BookingOfferAggregate bookingOfferAggregate = commandGatewayAdapter.sendAndWait(createBookingOfferCommand, bookCargoCommandOperationRequest.metaData)

    return new OperationResponse(payload: cargoBookingFactoryService.createBookCargoCommandResponse(bookingOfferAggregate))
  }
}
