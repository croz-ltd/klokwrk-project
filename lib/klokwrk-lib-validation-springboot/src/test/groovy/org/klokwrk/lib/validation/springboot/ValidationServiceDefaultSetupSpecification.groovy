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
package org.klokwrk.lib.validation.springboot

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@SpringBootTest
class ValidationServiceDefaultSetupSpecification extends Specification {
  @Autowired
  ValidationService validationService

  static class TestObject {
    @NotNull
    @Size(min = 1, max = 15)
    String stringProperty
  }

  void "default configuration should be applied"() {
    expect:
    validationService.enabled
    validationService.messageSourceBaseNames == ["klokwrkValidationConstraintMessages"] as String[]
    validationService.validatorImplementationPackagesToScan == ["org.klokwrk.lib.validation.validator"] as String[]
  }

  void "should not throw for valid object"() {
    given:
    TestObject testObject = new TestObject(stringProperty: "bla")

    when:
    validationService.validate(testObject)

    then:
    true
  }

  void "should throw for invalid object"() {
    given:
    TestObject testObject = new TestObject(stringProperty: null)

    when:
    validationService.validate(testObject)

    then:
    thrown(ConstraintViolationException)
  }
}
