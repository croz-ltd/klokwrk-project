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

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.gateway.CommandGateway
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.command.BookCargoCommand
import org.klokwrk.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoPortIn
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoRequest
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoResponse
import org.klokwrk.cargotracker.lib.axon.cqrs.commandgateway.CommandGatewayAdapter
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.springframework.stereotype.Service

import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Validator

import static org.hamcrest.Matchers.notNullValue

@Service
@CompileStatic
class CargoBookingApplicationService implements BookCargoPortIn {
  private final CargoBookingFactoryService cargoBookingFactoryService
  private final CommandGatewayAdapter commandGatewayAdapter
  private final Validator validator

  CargoBookingApplicationService(Validator validator, CommandGateway commandGateway, CargoBookingFactoryService cargoBookingFactoryService) {
    this.validator = validator
    this.commandGatewayAdapter = new CommandGatewayAdapter(commandGateway)
    this.cargoBookingFactoryService = cargoBookingFactoryService
  }

  @Override
  OperationResponse<BookCargoResponse> bookCargo(OperationRequest<BookCargoRequest> bookCargoOperationRequest) {
    requireMatch(bookCargoOperationRequest, notNullValue())
    validateOperationRequest(bookCargoOperationRequest)

    BookCargoCommand bookCargoCommand = cargoBookingFactoryService.createBookCargoCommand(bookCargoOperationRequest.payload)
    CargoAggregate cargoAggregate = commandGatewayAdapter.sendAndWait(bookCargoCommand, bookCargoOperationRequest.metaData)

    return new OperationResponse(payload: cargoBookingFactoryService.createBookCargoResponse(cargoAggregate))
  }

  private void validateOperationRequest(OperationRequest<?> operationRequest) {
    Set<ConstraintViolation<?>> constraintViolationSet = validator.validate(operationRequest.payload)
    if (!constraintViolationSet.isEmpty()) {
      throw new ConstraintViolationException(constraintViolationSet)
    }
  }
}
