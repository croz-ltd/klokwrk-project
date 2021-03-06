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
package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.adapter.in.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoPortIn
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoResponse
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

@CompileStatic
@RestController
@RequestMapping("/cargo-booking")
class CargoBookingWebController {
  private final BookCargoPortIn bookCargoPortIn

  CargoBookingWebController(BookCargoPortIn bookCargoPortIn) {
    this.bookCargoPortIn = bookCargoPortIn
  }

  @PostMapping("/book-cargo")
  OperationResponse<BookCargoResponse> bookCargo(@RequestBody BookCargoWebRequest bookCargoWebRequest, HttpServletRequest httpServletRequest) {
    OperationResponse<BookCargoResponse> bookCargoResponse = bookCargoPortIn.bookCargo(CargoBookingWebAssembler.toBookCargoOperationRequest(bookCargoWebRequest, httpServletRequest))
    return bookCargoResponse
  }
}
