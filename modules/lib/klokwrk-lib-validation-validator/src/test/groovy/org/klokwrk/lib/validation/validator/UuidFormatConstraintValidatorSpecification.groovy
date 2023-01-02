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
package org.klokwrk.lib.validation.validator

import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.cfg.ConstraintMapping
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator
import org.klokwrk.lib.validation.constraint.UuidFormatConstraint
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

class UuidFormatConstraintValidatorSpecification extends Specification {
  @Shared
  Validator validator

  static class UuidFormatConstraintTestObject {
    @UuidFormatConstraint
    String uuidString
  }

  void setupSpec() {
    validator = configureValidator()
  }

  private Validator configureValidator(Locale defaultLocale = Locale.default) {
    HibernateValidatorConfiguration configuration = Validation
        .byProvider(HibernateValidator)
        .configure()
        .messageInterpolator(
            new ResourceBundleMessageInterpolator(
                new PlatformResourceBundleLocator("klokwrkValidationConstraintMessages"), [new Locale("hr"), new Locale("en")] as Set<Locale>, defaultLocale, new DefaultLocaleResolver(), true
            )
        )

    ConstraintMapping constraintMapping = configuration.createConstraintMapping()
    constraintMapping.constraintDefinition(UuidFormatConstraint).validatedBy(UuidFormatConstraintValidator)

    ValidatorFactory validatorFactory = configuration.addMapping(constraintMapping).buildValidatorFactory()
    return validatorFactory.getValidator()
  }

  void "should pass for null string"() {
    given:
    UuidFormatConstraintTestObject uuidFormatConstraintTestObject = new UuidFormatConstraintTestObject(uuidString: null)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(uuidFormatConstraintTestObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should pass for empty string"() {
    given:
    UuidFormatConstraintTestObject uuidFormatConstraintTestObject = new UuidFormatConstraintTestObject(uuidString: uuidStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(uuidFormatConstraintTestObject)

    then:
    constraintViolations.isEmpty()

    where:
    uuidStringParam | _
    ""              | _
    "    "          | _
  }

  void "should pass for string in uuid format"() {
    given:
    UuidFormatConstraintTestObject uuidFormatConstraintTestObject = new UuidFormatConstraintTestObject(uuidString: uuidStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(uuidFormatConstraintTestObject)

    then:
    constraintViolations.isEmpty()

    where:
    uuidStringParam                        | _
    "4dd180d4-c554-4235-85be-3dfed42c316d" | _
    "00000000-0000-0000-0000-000000000000" | _
  }

  void "should fail for invalid uuid string"() {
    given:
    UuidFormatConstraintTestObject uuidFormatConstraintTestObject = new UuidFormatConstraintTestObject(uuidString: uuidStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(uuidFormatConstraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.invalidValue == uuidStringParam
      it.propertyPath.toString() == "uuidString"
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.UuidFormatConstraint.message}"
    }

    where:
    uuidStringParam                           | _
    "4dd180d4-c554-4235-85be-3dfed42c316d   " | _
    "   00000000-0000-0000-0000-000000000000" | _
    "123"                                     | _
  }

  void "should correctly interpolate messages for various locales"() {
    given:
    Validator myValidator = configureValidator(localeParam)
    UuidFormatConstraintTestObject uuidFormatConstraintTestObject = new UuidFormatConstraintTestObject(uuidString: uuidStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(uuidFormatConstraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.UuidFormatConstraint.message}"
      it.message == messageParam
    }

    where:
    uuidStringParam                           | localeParam      | messageParam
    "   00000000-0000-0000-0000-000000000000" | new Locale("en") | "Invalid UUID."
    "   00000000-0000-0000-0000-000000000000" | new Locale("hr") | "Neispravan UUID."
  }
}
