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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in

import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.lib.validation.constraint.NotBlankWhenNullableConstraint
import org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint
import org.klokwrk.lib.validation.constraint.UnLoCodeFormatConstraint
import org.klokwrk.lib.validation.constraint.ValueOfEnumConstraint
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

class CreateBookingOfferCommandRequestSpecification extends Specification {
  static String validBookingOfferIdentifier = "00000000-0000-4000-8000-000000000000"
  static RouteSpecificationData validRouteSpecificationData = new RouteSpecificationData(
      originLocation: "AAAAA", destinationLocation: "AAAAA",
      departureEarliestTime: Instant.now(), departureLatestTime: Instant.now(),
      arrivalLatestTime: Instant.now()
  )
  static CommodityInfoData validCommodityInfoData = new CommodityInfoData(commodityType: CommodityType.DRY, totalWeightInKilograms: 1000, requestedStorageTemperatureInCelsius: null)
  static String validContainerDimensionTypeData = "DIMENSION_ISO_22"

  @Shared
  ValidationService validationService

  void setupSpec() {
    validationService = new ValidationService(new ValidationConfigurationProperties())
    validationService.afterPropertiesSet()
  }

  void "should pass validation for valid data"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        bookingOfferIdentifier: bookingOfferIdentifierParam,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: validCommodityInfoData,
        containerDimensionType: validContainerDimensionTypeData
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    bookingOfferIdentifierParam            | _
    null                                   | _
    "00000000-0000-4000-8000-000000000000" | _
  }

  void "should pass validation for weightInKilograms data in CommodityInfoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: new CommodityInfoData(commodityType: CommodityType.DRY, totalWeightInKilograms: weightInKilogramsParam, requestedStorageTemperatureInCelsius: null),
        containerDimensionType: validContainerDimensionTypeData
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

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
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: new CommodityInfoData(commodityType: CommodityType.CHILLED, totalWeightInKilograms: 1000, requestedStorageTemperatureInCelsius: storageTemperatureInCelsiusParam),
        containerDimensionType: validContainerDimensionTypeData
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    storageTemperatureInCelsiusParam | _
    -30                              | _
    30                               | _
  }

  void "should not pass validation for invalid bookingOfferIdentifier"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        bookingOfferIdentifier: bookingOfferIdentifierParam,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: validCommodityInfoData,
        containerDimensionType: validContainerDimensionTypeData
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == propertyPathParam
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    bookingOfferIdentifierParam            | propertyPathParam        | constraintTypeParam
    ""                                     | "bookingOfferIdentifier" | NotBlankWhenNullableConstraint
    "123"                                  | "bookingOfferIdentifier" | Size
    "00000000=0000=0000=0000=000000000000" | "bookingOfferIdentifier" | RandomUuidFormatConstraint
  }

  void "should not pass validation for null routeSpecification"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: null,
        commodityInfo: validCommodityInfoData,
        containerDimensionType: validContainerDimensionTypeData
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "routeSpecification"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == NotNull
  }

  void "should not pass validation for invalid locations data"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: new RouteSpecificationData(
            originLocation: originLocationParam, destinationLocation: destinationLocationParam,
            departureEarliestTime: Instant.now(), departureLatestTime: Instant.now(),
            arrivalLatestTime: Instant.now()
        ),
        commodityInfo: validCommodityInfoData,
        containerDimensionType: validContainerDimensionTypeData
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

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
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: new RouteSpecificationData(
            originLocation: "AAAAA", destinationLocation: "AAAAA",
            departureEarliestTime: departureEarliestTimeParam, departureLatestTime: departureLatestTimeParam,
            arrivalLatestTime: Instant.now()
        ),
        commodityInfo: validCommodityInfoData,
        containerDimensionType: validContainerDimensionTypeData
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

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
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: new RouteSpecificationData(
            originLocation: "AAAAA", destinationLocation: "AAAAA",
            departureEarliestTime: Instant.now(), departureLatestTime: Instant.now(),
            arrivalLatestTime: null
        ),
        commodityInfo: validCommodityInfoData,
        containerDimensionType: validContainerDimensionTypeData
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "routeSpecification.arrivalLatestTime"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == NotNull
  }

  void "should not pass validation for invalid commodityType data in CommodityInfoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: new CommodityInfoData(commodityType: null, totalWeightInKilograms: 1000, requestedStorageTemperatureInCelsius: null),
        containerDimensionType: validContainerDimensionTypeData
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "commodityInfo.commodityType"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == NotNull
  }

  void "should not pass validation for invalid totalWeightInKilograms data in CommodityInfoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: new CommodityInfoData(commodityType: CommodityType.DRY, totalWeightInKilograms: weightInKilogramsParam, requestedStorageTemperatureInCelsius: null),
        containerDimensionType: validContainerDimensionTypeData
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "commodityInfo.totalWeightInKilograms"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    weightInKilogramsParam | constraintTypeParam
    null                   | NotNull
    0                      | Min
  }

  void "should not pass validation for invalid requestedStorageTemperatureInCelsius data in CommodityInfoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: new CommodityInfoData(commodityType: CommodityType.CHILLED, totalWeightInKilograms: 1000, requestedStorageTemperatureInCelsius: storageTemperatureInCelsiusParam),
        containerDimensionType: validContainerDimensionTypeData
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "commodityInfo.requestedStorageTemperatureInCelsius"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    storageTemperatureInCelsiusParam | constraintTypeParam
    -31                              | Min
    31                               | Max
  }

  void "should not pass validation for invalid containerDimensionType"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: validCommodityInfoData,
        containerDimensionType: conatinerDimensionTypeParam
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "containerDimensionType"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    conatinerDimensionTypeParam | constraintTypeParam
    null                        | NotBlank
    ""                          | NotBlank
    "  "                        | NotBlank

    " DIMENSION_ISO_12"         | ValueOfEnumConstraint
    "DIMENSION_ISO_12 "         | ValueOfEnumConstraint
    " DIMENSION_ISO_12 "        | ValueOfEnumConstraint

    "invalid"                   | ValueOfEnumConstraint
  }
}
