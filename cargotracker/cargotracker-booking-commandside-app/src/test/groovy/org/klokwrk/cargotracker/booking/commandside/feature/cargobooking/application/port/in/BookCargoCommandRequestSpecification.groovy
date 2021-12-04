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
package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in

import org.klokwrk.lib.validation.constraint.NotBlankWhenNullableConstraint
import org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint
import org.klokwrk.lib.validation.constraint.UnLoCodeFormatConstraint
import org.klokwrk.lib.validation.springboot.ValidationConfigurationProperties
import org.klokwrk.lib.validation.springboot.ValidationService
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

class BookCargoCommandRequestSpecification extends Specification {
  @Shared
  ValidationService validationService

  void setupSpec() {
    validationService = new ValidationService(new ValidationConfigurationProperties())
    validationService.afterPropertiesSet()
  }

  void "should pass validation for valid data"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: cargoIdentifierParam,
        routeSpecification: new RouteSpecificationData(originLocation: originLocationParam, destinationLocation: destinationLocationParam)
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    cargoIdentifierParam                   | originLocationParam | destinationLocationParam
    null                                   | "AAAAA"             | "AAAAA"
    "00000000-0000-4000-8000-000000000000" | "AAAAA"             | "AAAAA"
  }

  void "should not pass validation for invalid cargoIdentifier"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: cargoIdentifierParam,
        routeSpecification: new RouteSpecificationData(originLocation: "AAAAA", destinationLocation: "AAAAA")
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == propertyPathParam
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    cargoIdentifierParam                   | propertyPathParam | constraintTypeParam
    ""                                     | "cargoIdentifier" | NotBlankWhenNullableConstraint
    "123"                                  | "cargoIdentifier" | Size
    "00000000=0000=0000=0000=000000000000" | "cargoIdentifier" | RandomUuidFormatConstraint
  }

  void "should not pass validation for null routeSpecification"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: "00000000-0000-4000-8000-000000000000",
        routeSpecification: null
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "routeSpecification"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == NotNull
  }

  void "should not pass validation for invalid nested data"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: cargoIdentifierParam,
        routeSpecification: new RouteSpecificationData(originLocation: originLocationParam, destinationLocation: destinationLocationParam)
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == propertyPathParam
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    cargoIdentifierParam                   | originLocationParam | destinationLocationParam | propertyPathParam                        | constraintTypeParam
    ""                                     | "AAAAA"             | "AAAAA"                  | "cargoIdentifier"                        | NotBlankWhenNullableConstraint
    "123"                                  | "AAAAA"             | "AAAAA"                  | "cargoIdentifier"                        | Size
    "00000000=0000=0000=0000=000000000000" | "AAAAA"             | "AAAAA"                  | "cargoIdentifier"                        | RandomUuidFormatConstraint

    null                                   | null                | "AAAAA"                  | "routeSpecification.originLocation"      | NotBlank
    null                                   | "A"                 | "AAAAA"                  | "routeSpecification.originLocation"      | Size
    null                                   | "1===5"             | "AAAAA"                  | "routeSpecification.originLocation"      | UnLoCodeFormatConstraint

    null                                   | "AAAAA"             | null                     | "routeSpecification.destinationLocation" | NotBlank
    null                                   | "AAAAA"             | "A"                      | "routeSpecification.destinationLocation" | Size
    null                                   | "AAAAA"             | "1===5"                  | "routeSpecification.destinationLocation" | UnLoCodeFormatConstraint
  }
}
