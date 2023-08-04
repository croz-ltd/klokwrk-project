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
package org.klokwrk.lib.lo.validation.validator

import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.cfg.ConstraintMapping
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator
import org.klokwrk.lib.validation.constraint.TrimmedStringConstraint
import spock.lang.Shared
import spock.lang.Specification

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory

class TrimmedStringConstraintValidatorSpecification extends Specification {
  @Shared
  Validator validator

  static class TrimmedStringConstraintTestObject {
    @TrimmedStringConstraint
    String someString
  }

  void setupSpec() {
    validator = configureValidator("klokwrkValidationConstraintMessages")
  }

  private Validator configureValidator(String resourceBundleName, Locale defaultLocale = Locale.default) {
    HibernateValidatorConfiguration configuration = Validation
        .byProvider(HibernateValidator)
        .configure()
        .messageInterpolator(
            new ResourceBundleMessageInterpolator(
                new PlatformResourceBundleLocator(resourceBundleName), [new Locale("hr"), new Locale("en")] as Set<Locale>, defaultLocale, new DefaultLocaleResolver(), true
            )
        )

    ConstraintMapping constraintMapping = configuration.createConstraintMapping()
    constraintMapping.constraintDefinition(TrimmedStringConstraint).validatedBy(TrimmedStringConstraintValidator)

    ValidatorFactory validatorFactory = configuration.addMapping(constraintMapping).buildValidatorFactory()
    return validatorFactory.getValidator()
  }

  void "should pass for null string"() {
    given:
    TrimmedStringConstraintTestObject testObject = new TrimmedStringConstraintTestObject(someString: null)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should pass for empty string"() {
    given:
    TrimmedStringConstraintTestObject testObject = new TrimmedStringConstraintTestObject(someString: someStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    someStringParam | _
    ""              | _
    "    "          | _
  }

  void "should pass for trimmed string"() {
    given:
    TrimmedStringConstraintTestObject testObject = new TrimmedStringConstraintTestObject(someString: "string value")

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should fail for non-trimmed string"() {
    given:
    TrimmedStringConstraintTestObject testObject = new TrimmedStringConstraintTestObject(someString: someStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.invalidValue == someStringParam
      it.propertyPath.toString() == "someString"
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.TrimmedStringConstraint.message}"
    }

    where:
    someStringParam      | _
    "string value "      | _
    "string value   "    | _
    " string value"      | _
    "   string value"    | _
    " string value "     | _
    "   string value   " | _
  }

  void "should correctly interpolate messages for various locales when non-parsable UUID string is validated"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    TrimmedStringConstraintTestObject testObject = new TrimmedStringConstraintTestObject(someString: someStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.TrimmedStringConstraint.message}"
      it.message == messageParam
    }

    where:
    someStringParam        | localeParam      | messageParam
    " non-trimmed string " | new Locale("en") | "Contains spaces at the start or at the end."
    " non-trimmed string " | new Locale("hr") | "Sadrži praznine na početku ili na kraju."
  }
}
