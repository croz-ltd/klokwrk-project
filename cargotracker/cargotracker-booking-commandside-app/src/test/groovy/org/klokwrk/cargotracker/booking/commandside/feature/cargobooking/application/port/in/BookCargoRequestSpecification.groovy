package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in

import org.klokwrk.lib.validation.constraint.NotBlankWhenNullableConstraint
import org.klokwrk.lib.validation.constraint.UnLoCodeFormatConstraint
import org.klokwrk.lib.validation.constraint.UuidFormatConstraint
import org.klokwrk.lib.validation.springboot.ValidationConfigurationProperties
import org.klokwrk.lib.validation.springboot.ValidationService
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.constraints.NotBlank

class BookCargoRequestSpecification extends Specification {
  @Shared
  ValidationService validationService

  void setupSpec() {
    validationService = new ValidationService(new ValidationConfigurationProperties())
    validationService.afterPropertiesSet()
  }

  void "should pass validation for valid data"() {
    given:
    BookCargoRequest bookCargoRequest = new BookCargoRequest(aggregateIdentifier: aggregateIdentifierParam, originLocation: originLocationParam, destinationLocation: destinationLocationParam)

    when:
    validationService.validate(bookCargoRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    aggregateIdentifierParam               | originLocationParam | destinationLocationParam
    null                                   | "AAAAA"             | "AAAAA"
    "00000000-0000-0000-0000-000000000000" | "AAAAA"             | "AAAAA"
  }

  void "should not pass validation for invalid data"() {
    given:
    BookCargoRequest bookCargoRequest = new BookCargoRequest(aggregateIdentifier: aggregateIdentifierParam, originLocation: originLocationParam, destinationLocation: destinationLocationParam)

    when:
    validationService.validate(bookCargoRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == propertyPathParam
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    aggregateIdentifierParam | originLocationParam | destinationLocationParam | propertyPathParam     | constraintTypeParam
    ""                       | "AAAAA"             | "AAAAA"                  | "aggregateIdentifier" | NotBlankWhenNullableConstraint
    "123"                    | "AAAAA"             | "AAAAA"                  | "aggregateIdentifier" | UuidFormatConstraint

    null                     | null                | "AAAAA"                  | "originLocation"      | NotBlank
    null                     | "A"                 | "AAAAA"                  | "originLocation"      | UnLoCodeFormatConstraint

    null                     | "AAAAA"             | null                     | "destinationLocation" | NotBlank
    null                     | "AAAAA"             | "A"                      | "destinationLocation" | UnLoCodeFormatConstraint
  }
}
