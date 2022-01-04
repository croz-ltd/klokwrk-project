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
package org.klokwrk.cargotracker.booking.queryside.feature.cargoinfo.application.port.in

import org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint
import org.klokwrk.lib.validation.springboot.ValidationConfigurationProperties
import org.klokwrk.lib.validation.springboot.ValidationService
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class CargoSummaryQueryRequestSpecification extends Specification {
  @Shared
  ValidationService validationService

  void setupSpec() {
    validationService = new ValidationService(new ValidationConfigurationProperties())
    validationService.afterPropertiesSet()
  }

  void "should pass validation for valid data"() {
    given:
    CargoSummaryQueryRequest cargoSummaryQueryRequest = new CargoSummaryQueryRequest(cargoIdentifier: cargoIdentifierParam)

    when:
    validationService.validate(cargoSummaryQueryRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    cargoIdentifierParam                   | _
    "00000000-0000-4000-8000-000000000000" | _
  }

  void "should not pass validation for invalid data"() {
    given:
    CargoSummaryQueryRequest cargoSummaryQueryRequest = new CargoSummaryQueryRequest(cargoIdentifier: cargoIdentifierParam)

    when:
    validationService.validate(cargoSummaryQueryRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == propertyPathParam
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    cargoIdentifierParam                   | propertyPathParam | constraintTypeParam
    ""                                     | "cargoIdentifier" | NotBlank
    "123"                                  | "cargoIdentifier" | Size
    "00000000=0000=0000=0000=000000000000" | "cargoIdentifier" | RandomUuidFormatConstraint
  }
}
