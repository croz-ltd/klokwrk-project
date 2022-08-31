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
import org.klokwrk.lib.validation.constraint.NotNullElementsConstraint
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

class NotNullElementsConstraintSpecification extends Specification {
  @Shared
  Validator validator

  static class ConstraintTestObject {
    @NotNullElementsConstraint
    List testList

    @NotNullElementsConstraint
    Map testMap
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
    constraintMapping
        .constraintDefinition(NotNullElementsConstraint)
        .validatedBy(NotNullElementsConstraintForCollectionValidator)
        .validatedBy(NotNullElementsConstraintForMapValidator)

    ValidatorFactory validatorFactory = configuration.addMapping(constraintMapping).buildValidatorFactory()
    return validatorFactory.getValidator()
  }

  void "should pass for null values"() {
    given:
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testList: null, testMap: null)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should pass for empty values"() {
    given:
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testList: [], testMap: [:])

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should pass for collection without null elements"() {
    given:
    List<String> testList = ["123", "456"]
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testList: testList)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should pass for map with non-null keys and values"() {
    given:
    Map<String, String> testMap = [key1: "123", key2: "456", key3: "789"]
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testMap: testMap)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should fail for collection with null elements"() {
    given:
    List<String> testList = testListParam
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testList: testList)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.invalidValue == testListParam
      it.propertyPath.toString() == "testList"
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.NotNullElementsConstraint.message}"
    }

    where:
    testListParam                     | _
    [null]                            | _
    [null, null]                      | _
    [null, "123"]                     | _
    ["123", null]                     | _
    ["123", null, "456"]              | _
    ["123", null, "456", null]        | _
    ["123", null, "456", null, "789"] | _
  }

  void "should fail for map with null values"() {
    given:
    Map<String, String> testMap = testMapParam
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testMap: testMap)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.invalidValue == testMapParam
      it.propertyPath.toString() == "testMap"
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.NotNullElementsConstraint.message}"
    }

    where:
    testMapParam                           | _
    [key1: null]                           | _
    [key1: null, key2: null]               | _
    [key1: null, key2: "123"]              | _
    [key1: "123", key2: null]              | _
    [key1: "123", key2: null, key3: "456"] | _
  }

  void "should fail for map with null keys"() {
    given:
    Map<String, String> testMap = testMapParam
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testMap: testMap)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.invalidValue == testMapParam
      it.propertyPath.toString() == "testMap"
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.NotNullElementsConstraint.message}"
    }

    where:
    testMapParam                | _
    [(null): "123"]             | _
    [(null): "123", key2: null] | _
    [key1: null, (null): "123"] | _
  }

  void "should correctly interpolate messages for various locales"() {
    given:
    Validator myValidator = configureValidator(localeParam)
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testList: [null])

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(constraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.NotNullElementsConstraint.message}"
      it.message == messageParam
    }

    where:
    localeParam      | messageParam
    new Locale("en") | "Must not contain null elements."
    new Locale("hr") | "Ne smije sadržavati null elemente."
  }
}
