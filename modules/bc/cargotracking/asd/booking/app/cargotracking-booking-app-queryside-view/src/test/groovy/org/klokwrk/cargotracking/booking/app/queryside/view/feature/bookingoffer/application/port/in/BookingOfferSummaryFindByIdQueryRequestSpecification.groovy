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
package org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in

import org.klokwrk.lib.hi.validation.springboot.ValidationConfigurationProperties
import org.klokwrk.lib.hi.validation.springboot.ValidationService
import org.klokwrk.lib.lo.validation.constraint.RandomUuidFormatConstraint
import org.klokwrk.lib.lo.validation.constraint.TrimmedStringConstraint
import spock.lang.Shared
import spock.lang.Specification

import jakarta.validation.ConstraintViolationException
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Null
import jakarta.validation.constraints.Size

class BookingOfferSummaryFindByIdQueryRequestSpecification extends Specification {
  @Shared
  ValidationService validationService

  void setupSpec() {
    validationService = new ValidationService(new ValidationConfigurationProperties())
    validationService.afterPropertiesSet()
  }

  void "should pass validation for valid data"() {
    given:
    BookingOfferSummaryFindByIdQueryRequest bookingOfferSummaryFindByIdQueryRequest =
        new BookingOfferSummaryFindByIdQueryRequest(bookingOfferId: "00000000-0000-4000-8000-000000000000", userId: "someUserId")

    when:
    validationService.validate(bookingOfferSummaryFindByIdQueryRequest)

    then:
    notThrown(ConstraintViolationException)
  }

  void "should not pass validation for invalid data"() {
    given:
    BookingOfferSummaryFindByIdQueryRequest bookingOfferSummaryFindByIdQueryRequest =
        new BookingOfferSummaryFindByIdQueryRequest(bookingOfferId: bookingOfferIdParam, userId: userIdParam)

    when:
    validationService.validate(bookingOfferSummaryFindByIdQueryRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    bookingOfferIdParam                    | userIdParam    | constraintTypeParam
    null                                   | "someUserId"   | NotBlank
    ""                                     | "someUserId"   | NotBlank
    "  "                                   | "someUserId"   | NotBlank
    "123"                                  | "someUserId"   | Size
    "00000000=0000=0000=0000=000000000000" | "someUserId"   | RandomUuidFormatConstraint

    "00000000-0000-0000-0000-000000000000" | null           | NotBlank
    "00000000-0000-0000-0000-000000000000" | ""             | NotBlank
    "00000000-0000-0000-0000-000000000000" | "  "           | NotBlank
    "00000000-0000-0000-0000-000000000000" | " someUserId"  | TrimmedStringConstraint
    "00000000-0000-0000-0000-000000000000" | "someUserId "  | TrimmedStringConstraint
    "00000000-0000-0000-0000-000000000000" | " someUserId " | TrimmedStringConstraint
  }

  void "should not pass validation for unexpected data"() {
    given:
    BookingOfferSummaryFindByIdQueryRequest bookingOfferSummaryFindByIdQueryRequest =
        new BookingOfferSummaryFindByIdQueryRequest(bookingOfferId: UUID.randomUUID(), userId: "someUserId", customerId: "someCustomerId")

    when:
    validationService.validate(bookingOfferSummaryFindByIdQueryRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "customerId"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == Null
  }
}
