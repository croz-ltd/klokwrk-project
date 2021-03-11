package org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in

import org.klokwrk.lib.validation.constraint.UuidFormatConstraint
import org.klokwrk.lib.validation.springboot.ValidationConfigurationProperties
import org.klokwrk.lib.validation.springboot.ValidationService
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.constraints.NotBlank

class FetchCargoSummaryQueryRequestSpecification extends Specification {
  @Shared
  ValidationService validationService

  void setupSpec() {
    validationService = new ValidationService(new ValidationConfigurationProperties())
    validationService.afterPropertiesSet()
  }

  void "should pass validation for valid data"() {
    given:
    FetchCargoSummaryQueryRequest fetchCargoSummaryQueryRequest = new FetchCargoSummaryQueryRequest(aggregateIdentifier: aggregateIdentifierParam)

    when:
    validationService.validate(fetchCargoSummaryQueryRequest)

    then:
    notThrown(ConstraintViolationException)

    where:
    aggregateIdentifierParam               | _
    "00000000-0000-0000-0000-000000000000" | _
  }

  void "should not pass validation for invalid data"() {
    given:
    FetchCargoSummaryQueryRequest fetchCargoSummaryQueryRequest = new FetchCargoSummaryQueryRequest(aggregateIdentifier: aggregateIdentifierParam)

    when:
    validationService.validate(fetchCargoSummaryQueryRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()

    constraintViolationException.constraintViolations.size() == 1
    constraintViolationException.constraintViolations[0].propertyPath.toString() == propertyPathParam
    constraintViolationException.constraintViolations[0].constraintDescriptor.annotation.annotationType() == constraintTypeParam

    where:
    aggregateIdentifierParam | propertyPathParam     | constraintTypeParam
    ""                       | "aggregateIdentifier" | NotBlank
    "123"                    | "aggregateIdentifier" | UuidFormatConstraint
  }
}
