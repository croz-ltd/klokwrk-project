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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in

import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.CargoRequestData
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.RouteSpecificationRequestData
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.lib.validation.constraint.NotBlankWhenNullableConstraint
import org.klokwrk.lib.validation.constraint.NotNullElementsConstraint
import org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint
import org.klokwrk.lib.validation.constraint.TrimmedStringConstraint
import org.klokwrk.lib.validation.constraint.UnLoCodeFormatConstraint
import org.klokwrk.lib.validation.constraint.ValueOfEnumConstraint
import org.klokwrk.lib.validation.constraint.uom.QuantityMinConstraint
import org.klokwrk.lib.validation.constraint.uom.QuantityRangeConstraint
import org.klokwrk.lib.validation.springboot.ValidationConfigurationProperties
import org.klokwrk.lib.validation.springboot.ValidationService
import spock.lang.Shared
import spock.lang.Specification

import jakarta.validation.ConstraintViolationException
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

class CreateBookingOfferCommandRequestSpecification extends Specification {
  static String validBookingOfferId = "00000000-0000-4000-8000-000000000000"
  static RouteSpecificationRequestData validRouteSpecificationRequestData = new RouteSpecificationRequestData(
      originLocation: "AAAAA", destinationLocation: "AAAAA",
      departureEarliestTime: Instant.now(), departureLatestTime: Instant.now(),
      arrivalLatestTime: Instant.now()
  )
  static String validContainerDimensionTypeRequestData = "DIMENSION_ISO_22"
  static CargoRequestData validCargoRequestData = new CargoRequestData(
      commodityType: CommodityType.DRY.name(),
      commodityWeight: 1000.kg, commodityRequestedStorageTemperature: null, containerDimensionType: validContainerDimensionTypeRequestData
  )
  static Collection<CargoRequestData> validCargoRequestDataCollection = [validCargoRequestData]

  @Shared
  ValidationService validationService

  void setupSpec() {
    validationService = new ValidationService(new ValidationConfigurationProperties())
    validationService.afterPropertiesSet()
  }

  void "should pass validation for valid bookingOfferId"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "userId",
        bookingOfferId: bookingOfferIdParam,
        routeSpecification: validRouteSpecificationRequestData,
        cargos: validCargoRequestDataCollection
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    bookingOfferIdParam                    | _
    null                                   | _
    "00000000-0000-4000-8000-000000000000" | _
  }

  void "should pass validation for valid commodityWeight data in CargoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "userId",
        bookingOfferId: validBookingOfferId,
        routeSpecification: validRouteSpecificationRequestData,
        cargos: [new CargoRequestData(validCargoRequestData.properties).tap({ commodityWeight = commodityWeightParam })]
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    commodityWeightParam | _
    1.kg                 | _
    2.kg                 | _
    1_000_000.kg         | _
    1.t                  | _
    100.lb               | _
  }

  void "should pass validation for valid commodityRequestedStorageTemperature data in CargoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "userId",
        bookingOfferId: validBookingOfferId,
        routeSpecification: validRouteSpecificationRequestData,
        cargos: [new CargoRequestData(
            commodityType: CommodityType.CHILLED.name(),
            commodityWeight: 1000.kg,
            commodityRequestedStorageTemperature: commodityRequestedStorageTemperatureParam,
            containerDimensionType: validContainerDimensionTypeRequestData
        )]
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    commodityRequestedStorageTemperatureParam | _
    -30.degC                                  | _
    -20.12.degC                               | _
    -15.degC                                  | _
    5.degF                                    | _
    0.degC                                    | _
    10.51.degC                                | _
    20.degC                                   | _
    68.degF                                   | _
    30.degC                                   | _
  }

  void "should not pass validation for invalid userId"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: userIdParam,
        bookingOfferId: validBookingOfferId,
        routeSpecification: validRouteSpecificationRequestData,
        cargos: validCargoRequestDataCollection
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "userId"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    userIdParam | constraintTypeParam
    null        | NotBlank
    ""          | NotBlank
    "   "       | NotBlank
    " userId"   | TrimmedStringConstraint
    "userId "   | TrimmedStringConstraint
    " userId "  | TrimmedStringConstraint
  }

  void "should not pass validation for invalid bookingOfferId"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "userId",
        bookingOfferId: bookingOfferIdParam,
        routeSpecification: validRouteSpecificationRequestData,
        cargos: validCargoRequestDataCollection
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == propertyPathParam
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    bookingOfferIdParam                    | propertyPathParam | constraintTypeParam
    ""                                     | "bookingOfferId"  | NotBlankWhenNullableConstraint
    "123"                                  | "bookingOfferId"  | Size
    " 0000000-0000-0000-0000-00000000000 " | "bookingOfferId"  | TrimmedStringConstraint
    "00000000=0000=0000=0000=000000000000" | "bookingOfferId"  | RandomUuidFormatConstraint
  }

  void "should not pass validation for null routeSpecification"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "userId",
        bookingOfferId: validBookingOfferId,
        routeSpecification: null,
        cargos: validCargoRequestDataCollection
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
        userId: "userId",
        bookingOfferId: validBookingOfferId,
        routeSpecification: new RouteSpecificationRequestData(
            originLocation: originLocationParam, destinationLocation: destinationLocationParam,
            departureEarliestTime: Instant.now(), departureLatestTime: Instant.now(),
            arrivalLatestTime: Instant.now()
        ),
        cargos: validCargoRequestDataCollection
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
    " AAAA"             | "AAAAA"                  | "routeSpecification.originLocation"      | TrimmedStringConstraint
    "1===5"             | "AAAAA"                  | "routeSpecification.originLocation"      | UnLoCodeFormatConstraint

    "AAAAA"             | null                     | "routeSpecification.destinationLocation" | NotBlank
    "AAAAA"             | "A"                      | "routeSpecification.destinationLocation" | Size
    "AAAAA"             | "AAAAAA"                 | "routeSpecification.destinationLocation" | Size
    "AAAAA"             | " AAAA"                  | "routeSpecification.destinationLocation" | TrimmedStringConstraint
    "AAAAA"             | "1===5"                  | "routeSpecification.destinationLocation" | UnLoCodeFormatConstraint
  }

  void "should not pass validation for invalid departures instant data"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "userId",
        bookingOfferId: validBookingOfferId,
        routeSpecification: new RouteSpecificationRequestData(
            originLocation: "AAAAA", destinationLocation: "AAAAA",
            departureEarliestTime: departureEarliestTimeParam, departureLatestTime: departureLatestTimeParam,
            arrivalLatestTime: Instant.now()
        ),
        cargos: validCargoRequestDataCollection
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
        userId: "userId",
        bookingOfferId: validBookingOfferId,
        routeSpecification: new RouteSpecificationRequestData(
            originLocation: "AAAAA", destinationLocation: "AAAAA",
            departureEarliestTime: Instant.now(), departureLatestTime: Instant.now(),
            arrivalLatestTime: null
        ),
        cargos: validCargoRequestDataCollection
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "routeSpecification.arrivalLatestTime"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == NotNull
  }

  void "should not pass validation for invalid cargos"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "userId",
        bookingOfferId: validBookingOfferId,
        routeSpecification: validRouteSpecificationRequestData,
        cargos: cargosParam
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "cargos"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    cargosParam | constraintTypeParam
    null        | NotEmpty
    []          | NotEmpty
    [null]      | NotNullElementsConstraint
  }

  void "should not pass validation for invalid commodityType data in CargoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "userId",
        bookingOfferId: validBookingOfferId,
        routeSpecification: validRouteSpecificationRequestData,
        cargos: [new CargoRequestData(validCargoRequestData.properties).tap({ commodityType = commodityTypeParam })]
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "cargos[0].commodityType"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    commodityTypeParam | constraintTypeParam
    null               | NotBlank
    ""                 | NotBlank
    "  "               | NotBlank

    " DRY"             | TrimmedStringConstraint
    "DRY "             | TrimmedStringConstraint
    " DRY "            | TrimmedStringConstraint

    "invalid"          | ValueOfEnumConstraint
  }

  void "should not pass validation for invalid commodityWeight data in CargoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "userId",
        bookingOfferId: validBookingOfferId,
        routeSpecification: validRouteSpecificationRequestData,
        cargos: [new CargoRequestData(validCargoRequestData.properties).tap({ commodityWeight = commodityWeightParam })]
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "cargos[0].commodityWeight"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    commodityWeightParam | constraintTypeParam
    null                 | NotNull
    0.kg                 | QuantityMinConstraint
    0.t                  | QuantityMinConstraint
    0.lb                 | QuantityMinConstraint
  }

  void "should not pass validation for invalid commodityRequestedStorageTemperature data in CargoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "userId",
        bookingOfferId: validBookingOfferId,
        routeSpecification: validRouteSpecificationRequestData,
        cargos: [
            new CargoRequestData(
                commodityType: CommodityType.CHILLED.name(),
                commodityWeight: 1000.kg,
                commodityRequestedStorageTemperature: commodityRequestedStorageTemperatureParam,
                containerDimensionType: validContainerDimensionTypeRequestData
            )
        ]
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "cargos[0].commodityRequestedStorageTemperature"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    commodityRequestedStorageTemperatureParam | constraintTypeParam
    -31.degC                                  | QuantityRangeConstraint
    -23.8.degF                                | QuantityRangeConstraint
    31.degC                                   | QuantityRangeConstraint
    87.8.degF                                 | QuantityRangeConstraint
  }

  void "should not pass validation for invalid containerDimensionType data in CargoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "userId",
        bookingOfferId: validBookingOfferId,
        routeSpecification: validRouteSpecificationRequestData,
        cargos: [new CargoRequestData(validCargoRequestData.properties).tap({ containerDimensionType = containerDimensionTypeParam })]
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "cargos[0].containerDimensionType"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    containerDimensionTypeParam | constraintTypeParam
    null                        | NotBlank
    ""                          | NotBlank
    "  "                        | NotBlank

    " DIMENSION_ISO_12"         | TrimmedStringConstraint
    "DIMENSION_ISO_12 "         | TrimmedStringConstraint
    " DIMENSION_ISO_12 "        | TrimmedStringConstraint

    "invalid"                   | ValueOfEnumConstraint
  }
}
