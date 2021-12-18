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

import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.lib.validation.constraint.NotBlankWhenNullableConstraint
import org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint
import org.klokwrk.lib.validation.constraint.UnLoCodeFormatConstraint
import org.klokwrk.lib.validation.springboot.ValidationConfigurationProperties
import org.klokwrk.lib.validation.springboot.ValidationService
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import java.time.Instant

class BookCargoCommandRequestSpecification extends Specification {
  static String validCargoIdentifier = "00000000-0000-4000-8000-000000000000"
  static RouteSpecificationData validRouteSpecificationData = new RouteSpecificationData(
      originLocation: "AAAAA", destinationLocation: "AAAAA",
      departureEarliestTime: Instant.now(), departureLatestTime: Instant.now(),
      arrivalLatestTime: Instant.now()
  )
  static CommodityInfoData validCommodityInfoData = new CommodityInfoData(type: CommodityType.DRY, weightInKilograms: 1000, storageTemperatureInCelsius: null)

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
        routeSpecification: validRouteSpecificationData,
        commodityInfo: validCommodityInfoData
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    cargoIdentifierParam                   | _
    null                                   | _
    "00000000-0000-4000-8000-000000000000" | _
  }

  void "should pass validation for weightInKilograms data in CommodityInfoData"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: validCargoIdentifier,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: new CommodityInfoData(type: CommodityType.DRY, weightInKilograms: weightInKilogramsParam, storageTemperatureInCelsius: null)
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    weightInKilogramsParam | _
    1                      | _
    2                      | _
    1_000_000              | _
  }

  void "should pass validation for storageTemperatureInCelsius data in CommodityInfoData"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: validCargoIdentifier,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: new CommodityInfoData(type: CommodityType.CHILLED, weightInKilograms: 1000, storageTemperatureInCelsius: storageTemperatureInCelsiusParam)
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    storageTemperatureInCelsiusParam | _
    -35                              | _
    35                               | _
  }

  void "should not pass validation for invalid cargoIdentifier"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: cargoIdentifierParam,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: validCommodityInfoData
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
        cargoIdentifier: validCargoIdentifier,
        routeSpecification: null,
        commodityInfo: validCommodityInfoData
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "routeSpecification"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == NotNull
  }

  void "should not pass validation for invalid locations data"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: validCargoIdentifier,
        routeSpecification: new RouteSpecificationData(
            originLocation: originLocationParam, destinationLocation: destinationLocationParam,
            departureEarliestTime: Instant.now(), departureLatestTime: Instant.now(),
            arrivalLatestTime: Instant.now()
        ),
        commodityInfo: validCommodityInfoData
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == propertyPathParam
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    originLocationParam | destinationLocationParam | propertyPathParam                        | constraintTypeParam
    null                | "AAAAA"                  | "routeSpecification.originLocation"      | NotBlank
    "A"                 | "AAAAA"                  | "routeSpecification.originLocation"      | Size
    "AAAAAA"            | "AAAAA"                  | "routeSpecification.originLocation"      | Size
    "1===5"             | "AAAAA"                  | "routeSpecification.originLocation"      | UnLoCodeFormatConstraint

    "AAAAA"             | null                     | "routeSpecification.destinationLocation" | NotBlank
    "AAAAA"             | "A"                      | "routeSpecification.destinationLocation" | Size
    "AAAAA"             | "AAAAAA"                 | "routeSpecification.destinationLocation" | Size
    "AAAAA"             | "1===5"                  | "routeSpecification.destinationLocation" | UnLoCodeFormatConstraint
  }

  void "should not pass validation for invalid departures instant data"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: validCargoIdentifier,
        routeSpecification: new RouteSpecificationData(
            originLocation: "AAAAA", destinationLocation: "AAAAA",
            departureEarliestTime: departureEarliestTimeParam, departureLatestTime: departureLatestTimeParam,
            arrivalLatestTime: Instant.now()
        ),
        commodityInfo: validCommodityInfoData
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == propertyPathParam
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    departureEarliestTimeParam | departureLatestTimeParam | propertyPathParam                          | constraintTypeParam
    null                       | Instant.now()            | "routeSpecification.departureEarliestTime" | NotNull
    Instant.now()              | null                     | "routeSpecification.departureLatestTime"   | NotNull
  }

  void "should not pass validation for invalid arrival instant data"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: validCargoIdentifier,
        routeSpecification: new RouteSpecificationData(
            originLocation: "AAAAA", destinationLocation: "AAAAA",
            departureEarliestTime: Instant.now(), departureLatestTime: Instant.now(),
            arrivalLatestTime: null
        ),
        commodityInfo: validCommodityInfoData
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "routeSpecification.arrivalLatestTime"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == NotNull
  }

  void "should not pass validation for invalid type data in CommodityInfoData"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: validCargoIdentifier,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: new CommodityInfoData(type: null, weightInKilograms: 1000, storageTemperatureInCelsius: null)
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "commodityInfo.type"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == NotNull
  }

  void "should not pass validation for invalid weightInKilograms data in CommodityInfoData"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: validCargoIdentifier,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: new CommodityInfoData(type: CommodityType.DRY, weightInKilograms: weightInKilogramsParam, storageTemperatureInCelsius: null)
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "commodityInfo.weightInKilograms"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    weightInKilogramsParam | constraintTypeParam
    null                   | NotNull
    0                      | Min
  }

  void "should not pass validation for invalid storageTemperatureInCelsius data in CommodityInfoData"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: validCargoIdentifier,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: new CommodityInfoData(type: CommodityType.CHILLED, weightInKilograms: 1000, storageTemperatureInCelsius: storageTemperatureInCelsiusParam)
    )

    when:
    validationService.validate(bookCargoCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "commodityInfo.storageTemperatureInCelsius"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    storageTemperatureInCelsiusParam | constraintTypeParam
    -36                              | Min
    36                               | Max
  }
}
