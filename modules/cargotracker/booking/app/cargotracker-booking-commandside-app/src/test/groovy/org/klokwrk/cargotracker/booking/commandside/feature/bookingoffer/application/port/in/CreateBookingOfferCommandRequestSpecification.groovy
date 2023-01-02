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
import si.uom.NonSI
import spock.lang.Shared
import spock.lang.Specification
import systems.uom.common.USCustomary
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.validation.ConstraintViolationException
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
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
  static String validContainerDimensionTypeData = "DIMENSION_ISO_22"
  static CargoData validCargoData = new CargoData(
      commodityType: CommodityType.DRY.name(),
      commodityWeight: Quantities.getQuantity(1000, Units.KILOGRAM), commodityRequestedStorageTemperature: null, containerDimensionType: validContainerDimensionTypeData
  )
  static Collection<CargoData> validCargoDataCollection = [validCargoData]

  @Shared
  ValidationService validationService

  void setupSpec() {
    validationService = new ValidationService(new ValidationConfigurationProperties())
    validationService.afterPropertiesSet()
  }

  void "should pass validation for valid bookingOfferIdentifier"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userIdentifier: "userIdentifier",
        bookingOfferIdentifier: bookingOfferIdentifierParam,
        routeSpecification: validRouteSpecificationData,
        cargos: validCargoDataCollection
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

  void "should pass validation for valid commodityWeight data in CargoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userIdentifier: "userIdentifier",
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        cargos: [new CargoData(validCargoData.properties).tap({ commodityWeight = commodityWeightParam })]
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    commodityWeightParam                              | _
    Quantities.getQuantity(1, Units.KILOGRAM)         | _
    Quantities.getQuantity(2, Units.KILOGRAM)         | _
    Quantities.getQuantity(1_000_000, Units.KILOGRAM) | _
    Quantities.getQuantity(1, NonSI.TONNE)            | _
    Quantities.getQuantity(100, USCustomary.POUND)    | _
  }

  void "should pass validation for valid commodityRequestedStorageTemperature data in CargoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userIdentifier: "userIdentifier",
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        cargos: [new CargoData(
            commodityType: CommodityType.CHILLED.name(),
            commodityWeight: Quantities.getQuantity(1000, Units.KILOGRAM),
            commodityRequestedStorageTemperature: commodityRequestedStorageTemperatureParam,
            containerDimensionType: validContainerDimensionTypeData
        )]
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    commodityRequestedStorageTemperatureParam          | _
    Quantities.getQuantity(-30, Units.CELSIUS)         | _
    Quantities.getQuantity(-20.12, Units.CELSIUS)      | _
    Quantities.getQuantity(-15, Units.CELSIUS)         | _
    Quantities.getQuantity(5, USCustomary.FAHRENHEIT)  | _
    Quantities.getQuantity(0, Units.CELSIUS)           | _
    Quantities.getQuantity(10.51, Units.CELSIUS)       | _
    Quantities.getQuantity(20, Units.CELSIUS)          | _
    Quantities.getQuantity(68, USCustomary.FAHRENHEIT) | _
    Quantities.getQuantity(30, Units.CELSIUS)          | _
  }

  void "should not pass validation for invalid userIdentifier"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userIdentifier: userIdentifierParam,
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        cargos: validCargoDataCollection
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "userIdentifier"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    userIdentifierParam | constraintTypeParam
    null                | NotBlank
    ""                  | NotBlank
    "   "               | NotBlank
    " userIdentifier"   | TrimmedStringConstraint
    "userIdentifier "   | TrimmedStringConstraint
    " userIdentifier "  | TrimmedStringConstraint
  }

  void "should not pass validation for invalid bookingOfferIdentifier"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userIdentifier: "userIdentifier",
        bookingOfferIdentifier: bookingOfferIdentifierParam,
        routeSpecification: validRouteSpecificationData,
        cargos: validCargoDataCollection
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
    " 0000000-0000-0000-0000-00000000000 " | "bookingOfferIdentifier" | TrimmedStringConstraint
    "00000000=0000=0000=0000=000000000000" | "bookingOfferIdentifier" | RandomUuidFormatConstraint
  }

  void "should not pass validation for null routeSpecification"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userIdentifier: "userIdentifier",
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: null,
        cargos: validCargoDataCollection
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
        userIdentifier: "userIdentifier",
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: new RouteSpecificationData(
            originLocation: originLocationParam, destinationLocation: destinationLocationParam,
            departureEarliestTime: Instant.now(), departureLatestTime: Instant.now(),
            arrivalLatestTime: Instant.now()
        ),
        cargos: validCargoDataCollection
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
        userIdentifier: "userIdentifier",
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: new RouteSpecificationData(
            originLocation: "AAAAA", destinationLocation: "AAAAA",
            departureEarliestTime: departureEarliestTimeParam, departureLatestTime: departureLatestTimeParam,
            arrivalLatestTime: Instant.now()
        ),
        cargos: validCargoDataCollection
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
        userIdentifier: "userIdentifier",
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: new RouteSpecificationData(
            originLocation: "AAAAA", destinationLocation: "AAAAA",
            departureEarliestTime: Instant.now(), departureLatestTime: Instant.now(),
            arrivalLatestTime: null
        ),
        cargos: validCargoDataCollection
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
        userIdentifier: "userIdentifier",
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
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
        userIdentifier: "userIdentifier",
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        cargos: [new CargoData(validCargoData.properties).tap({ commodityType = commodityTypeParam })]
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
        userIdentifier: "userIdentifier",
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        cargos: [new CargoData(validCargoData.properties).tap({ commodityWeight = commodityWeightParam })]
    )

    when:
    validationService.validate(createBookingOfferCommandRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == "cargos[0].commodityWeight"
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    commodityWeightParam                         | constraintTypeParam
    null                                         | NotNull
    Quantities.getQuantity(0, Units.KILOGRAM)    | QuantityMinConstraint
    Quantities.getQuantity(0, NonSI.TONNE)       | QuantityMinConstraint
    Quantities.getQuantity(0, USCustomary.POUND) | QuantityMinConstraint
  }

  void "should not pass validation for invalid commodityRequestedStorageTemperature data in CargoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userIdentifier: "userIdentifier",
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        cargos: [
            new CargoData(
                commodityType: CommodityType.CHILLED.name(),
                commodityWeight: Quantities.getQuantity(1000, Units.KILOGRAM),
                commodityRequestedStorageTemperature: commodityRequestedStorageTemperatureParam,
                containerDimensionType: validContainerDimensionTypeData
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
    commodityRequestedStorageTemperatureParam             | constraintTypeParam
    Quantities.getQuantity(-31, Units.CELSIUS)            | QuantityRangeConstraint
    Quantities.getQuantity(-23.8, USCustomary.FAHRENHEIT) | QuantityRangeConstraint
    Quantities.getQuantity(31, Units.CELSIUS)             | QuantityRangeConstraint
    Quantities.getQuantity(87.8, USCustomary.FAHRENHEIT)  | QuantityRangeConstraint
  }

  void "should not pass validation for invalid containerDimensionType data in CargoData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userIdentifier: "userIdentifier",
        bookingOfferIdentifier: validBookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        cargos: [new CargoData(validCargoData.properties).tap({ containerDimensionType = containerDimensionTypeParam })]
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
