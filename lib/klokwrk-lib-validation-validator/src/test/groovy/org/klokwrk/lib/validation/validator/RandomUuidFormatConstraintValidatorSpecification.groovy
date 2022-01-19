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
package org.klokwrk.lib.validation.validator

import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.cfg.ConstraintMapping
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator
import org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

class RandomUuidFormatConstraintValidatorSpecification extends Specification {
  @Shared
  Validator validator

  static class RandomUuidFormatConstraintTestObject {
    @RandomUuidFormatConstraint
    String randomUuidString
  }

  static class RandomUuidFormatConstraintTestObjectWithCustomMessageTemplate {
    @RandomUuidFormatConstraint(message = "{custom.RandomUuidFormatConstraint.invalidMessage}")
    String randomUuidString
  }

  static class RandomUuidFormatConstraintTestObjectWithCustomMessage {
    @RandomUuidFormatConstraint(message = 'This is invalid random UUID - [validatedValue: ${validatedValue}].')
    String randomUuidString
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
    constraintMapping.constraintDefinition(RandomUuidFormatConstraint).validatedBy(RandomUuidFormatConstraintValidator)

    ValidatorFactory validatorFactory = configuration.addMapping(constraintMapping).buildValidatorFactory()
    return validatorFactory.getValidator()
  }

  void "should pass for null string"() {
    given:
    RandomUuidFormatConstraintTestObject randomUuidFormatConstraintTestObject = new RandomUuidFormatConstraintTestObject(randomUuidString: null)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(randomUuidFormatConstraintTestObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should pass for empty string"() {
    given:
    RandomUuidFormatConstraintTestObject randomUuidFormatConstraintTestObject = new RandomUuidFormatConstraintTestObject(randomUuidString: randomUuidStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(randomUuidFormatConstraintTestObject)

    then:
    constraintViolations.isEmpty()

    where:
    randomUuidStringParam | _
    ""                    | _
    "    "                | _
  }

  void "should pass for string in uuid format"() {
    given:
    RandomUuidFormatConstraintTestObject randomUuidFormatConstraintTestObject = new RandomUuidFormatConstraintTestObject(randomUuidString: randomUuidStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(randomUuidFormatConstraintTestObject)

    then:
    constraintViolations.isEmpty()

    where:
    randomUuidStringParam                  | _
    "4dd180d4-c554-4235-85be-3dfed42c316d" | _
    "00000000-0000-4000-B000-000000000000" | _
  }

  void "should fail for non parsable uuid string"() {
    given:
    RandomUuidFormatConstraintTestObject randomUuidFormatConstraintTestObject = new RandomUuidFormatConstraintTestObject(randomUuidString: randomUuidStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(randomUuidFormatConstraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.invalidValue == randomUuidStringParam
      it.propertyPath.toString() == "randomUuidString"
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint.invalidUuidFormatMessage}"
    }

    where:
    randomUuidStringParam                     | _
    "4dd180d4-c554-4235-85be-3dfed42c316d   " | _
    "   00000000-0000-0000-0000-000000000000" | _
    "123"                                     | _
  }

  void "should fail for non random uuid string"() {
    given:
    RandomUuidFormatConstraintTestObject randomUuidFormatConstraintTestObject = new RandomUuidFormatConstraintTestObject(randomUuidString: randomUuidStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(randomUuidFormatConstraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.invalidValue == randomUuidStringParam
      it.propertyPath.toString() == "randomUuidString"
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint.invalidRandomUuidFormatMessage}"
    }

    where:
    randomUuidStringParam                  | _
    "4dd180d4-c554-0235-05be-3dfed42c316d" | _
    "00000000-0000-0000-0000-000000000000" | _
    "00000000-0000-4000-0000-000000000000" | _
    "00000000-0000-4000-7000-000000000000" | _
    "00000000-0000-4000-C000-000000000000" | _
    "00000000-0000-0000-8000-000000000000" | _
    "00000000-0000-0000-B000-000000000000" | _
  }

  void "should correctly interpolate messages for various locales when non-parsable UUID string is validated"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    RandomUuidFormatConstraintTestObject randomUuidFormatConstraintTestObject = new RandomUuidFormatConstraintTestObject(randomUuidString: randomUuidStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(randomUuidFormatConstraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint.invalidUuidFormatMessage}"
      it.message == messageParam
    }

    where:
    randomUuidStringParam | localeParam      | messageParam
    "invalid uuid"        | new Locale("en") | "Invalid UUID."
    "invalid uuid"        | new Locale("hr") | "Neispravan UUID."
  }

  void "should correctly interpolate default messages for various locales when non-random UUID string is validated"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    RandomUuidFormatConstraintTestObject randomUuidFormatConstraintTestObject = new RandomUuidFormatConstraintTestObject(randomUuidString: randomUuidStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(randomUuidFormatConstraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint.invalidRandomUuidFormatMessage}"
      it.message == messageParam
    }

    where:
    randomUuidStringParam                  | localeParam      | messageParam
    "00000000-0000-0000-0000-000000000000" | new Locale("en") | "Invalid random UUID."
    "00000000-0000-0000-0000-000000000000" | new Locale("hr") | "Neispravan slučajno generirani UUID."
  }

  void "should correctly interpolate custom message template with expressions for different locales when non-parsable or non-random UUID string is validated"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintTestMessages", localeParam)
    RandomUuidFormatConstraintTestObjectWithCustomMessageTemplate testObject = new RandomUuidFormatConstraintTestObjectWithCustomMessageTemplate(randomUuidString: randomUuidStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.messageTemplate == "{custom.RandomUuidFormatConstraint.invalidMessage}"
      it.message == messageParam
    }

    where:
    randomUuidStringParam                  | localeParam      | messageParam
    "invalid uuid"                         | new Locale("en") | "The value '$randomUuidStringParam' is an invalid random UUID."
    "00000000-0000-0000-0000-000000000000" | new Locale("en") | "The value '$randomUuidStringParam' is an invalid random UUID."

    "invalid uuid"                         | new Locale("hr") | "Vrijednost '$randomUuidStringParam' je neispravan slučajno generirani UUID."
    "00000000-0000-0000-0000-000000000000" | new Locale("hr") | "Vrijednost '$randomUuidStringParam' je neispravan slučajno generirani UUID."
  }

  void "should correctly interpolate custom message with expressions when non-parsable or non-random UUID string is validated"() {
    given:
    RandomUuidFormatConstraintTestObjectWithCustomMessage testObject = new RandomUuidFormatConstraintTestObjectWithCustomMessage(randomUuidString: randomUuidStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.messageTemplate == 'This is invalid random UUID - [validatedValue: ${validatedValue}].'
      it.message == messageParam
    }

    where:
    randomUuidStringParam                  | messageParam
    "invalid uuid"                         | "This is invalid random UUID - [validatedValue: $randomUuidStringParam]."
    "00000000-0000-0000-0000-000000000000" | "This is invalid random UUID - [validatedValue: $randomUuidStringParam]."
  }
}
