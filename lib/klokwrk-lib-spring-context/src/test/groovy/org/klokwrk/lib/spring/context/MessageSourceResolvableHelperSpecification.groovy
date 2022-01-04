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
package org.klokwrk.lib.spring.context

import spock.lang.Specification

class MessageSourceResolvableHelperSpecification extends Specification {
  void "should create expected message code list for violation code message of domain failure"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory", // always fixed to "failure"
        messageType: "messageType",         // always fixed to "domain"
        messageSubType: "messageSubType",
        severity: "severity",
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfDomainFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 12

      messageCodeList[0] == "controllerSimpleName.controllerMethodName.failure.domain.severity.messageSubType"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.failure.domain.messageSubType"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.failure.domain.severity"

      messageCodeList[3] == "controllerMethodName.failure.domain.severity.messageSubType"
      messageCodeList[4] == "controllerMethodName.failure.domain.messageSubType"
      messageCodeList[5] == "controllerMethodName.failure.domain.severity"

      messageCodeList[6] == "default.failure.domain.severity.messageSubType"
      messageCodeList[7] == "default.failure.domain.messageSubType"
      messageCodeList[8] == "default.failure.domain.severity"
      messageCodeList[9] == "default.failure.domain"
      messageCodeList[10] == "default.failure.severity"
      messageCodeList[11] == "default.severity"
    }
  }

  void "should create expected message code list for violation code message of domain failure with messageSubTypeDetails"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory", // always fixed to "failure"
        messageType: "messageType",         // always fixed to "domain"
        messageSubType: "messageSubType",
        messageSubTypeDetails: "messageSubTypeDetails",
        severity: "severity",
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfDomainFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 18

      messageCodeList[0] == "controllerSimpleName.controllerMethodName.failure.domain.severity.messageSubType.messageSubTypeDetails"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.failure.domain.severity.messageSubType"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.failure.domain.messageSubType.messageSubTypeDetails"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.failure.domain.messageSubType"
      messageCodeList[4] == "controllerSimpleName.controllerMethodName.failure.domain.severity"

      messageCodeList[5] == "controllerMethodName.failure.domain.severity.messageSubType.messageSubTypeDetails"
      messageCodeList[6] == "controllerMethodName.failure.domain.severity.messageSubType"
      messageCodeList[7] == "controllerMethodName.failure.domain.messageSubType.messageSubTypeDetails"
      messageCodeList[8] == "controllerMethodName.failure.domain.messageSubType"
      messageCodeList[9] == "controllerMethodName.failure.domain.severity"

      messageCodeList[10] == "default.failure.domain.severity.messageSubType.messageSubTypeDetails"
      messageCodeList[11] == "default.failure.domain.severity.messageSubType"
      messageCodeList[12] == "default.failure.domain.messageSubType.messageSubTypeDetails"
      messageCodeList[13] == "default.failure.domain.messageSubType"
      messageCodeList[14] == "default.failure.domain.severity"
      messageCodeList[15] == "default.failure.domain"
      messageCodeList[16] == "default.failure.severity"
      messageCodeList[17] == "default.severity"
    }
  }

  void "should create expected message code list for violation code message of domain failure for empty MessageSourceResolvableSpecification"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification()

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfDomainFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 6
      messageCodeList[0] == "failure.domain.warning"
      messageCodeList[1] == "failure.domain"
      messageCodeList[2] == "default.failure.domain.warning"
      messageCodeList[3] == "default.failure.domain"
      messageCodeList[4] == "default.failure.warning"
      messageCodeList[5] == "default.warning"
    }
  }

  void "should create expected message code list for violation code message of domain failure for missing controllerSimpleName [controllerSimpleName: '#controllerSimpleNameParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: controllerSimpleNameParam,
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory", // always fixed to "failure"
        messageType: "messageType",         // always fixed to "domain"
        messageSubType: "messageSubType",
        severity: "severity"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfDomainFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 9

      messageCodeList[0] == "controllerMethodName.failure.domain.severity.messageSubType"
      messageCodeList[1] == "controllerMethodName.failure.domain.messageSubType"
      messageCodeList[2] == "controllerMethodName.failure.domain.severity"

      messageCodeList[3] == "default.failure.domain.severity.messageSubType"
      messageCodeList[4] == "default.failure.domain.messageSubType"
      messageCodeList[5] == "default.failure.domain.severity"
      messageCodeList[6] == "default.failure.domain"
      messageCodeList[7] == "default.failure.severity"
      messageCodeList[8] == "default.severity"
    }

    where:
    controllerSimpleNameParam | _
    null                      | _
    ""                        | _
    "   "                     | _
  }

  void "should create expected message code list for violation code message of infrastructure_web failure"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory", // always fixed to "failure"
        messageType: "messageType",         // always fixed to "infrastructure_web"
        messageSubType: "messageSubType",
        severity: "severity",
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfInfrastructureWebFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 12

      messageCodeList[0] == "controllerSimpleName.controllerMethodName.failure.infrastructure_web.severity.messageSubType"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.failure.infrastructure_web.messageSubType"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.failure.infrastructure_web.severity"

      messageCodeList[3] == "controllerMethodName.failure.infrastructure_web.severity.messageSubType"
      messageCodeList[4] == "controllerMethodName.failure.infrastructure_web.messageSubType"
      messageCodeList[5] == "controllerMethodName.failure.infrastructure_web.severity"

      messageCodeList[6] == "default.failure.infrastructure_web.severity.messageSubType"
      messageCodeList[7] == "default.failure.infrastructure_web.messageSubType"
      messageCodeList[8] == "default.failure.infrastructure_web.severity"
      messageCodeList[9] == "default.failure.infrastructure_web"
      messageCodeList[10] == "default.failure.severity"
      messageCodeList[11] == "default.severity"
    }
  }

  void "should create expected message code list for violation code message of infrastructure_web failure for empty MessageSourceResolvableSpecification"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification()

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfInfrastructureWebFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 6
      messageCodeList[0] == "failure.infrastructure_web.warning"
      messageCodeList[1] == "failure.infrastructure_web"
      messageCodeList[2] == "default.failure.infrastructure_web.warning"
      messageCodeList[3] == "default.failure.infrastructure_web"
      messageCodeList[4] == "default.failure.warning"
      messageCodeList[5] == "default.warning"
    }
  }

  void "should create expected message code list for violation code message of infrastructure_web failure for missing controllerSimpleName [controllerSimpleName: '#controllerSimpleNameParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: controllerSimpleNameParam,
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory", // always fixed to "failure"
        messageType: "messageType",         // always fixed to "infrastructure_web"
        messageSubType: "messageSubType",
        severity: "severity"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfInfrastructureWebFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 9

      messageCodeList[0] == "controllerMethodName.failure.infrastructure_web.severity.messageSubType"
      messageCodeList[1] == "controllerMethodName.failure.infrastructure_web.messageSubType"
      messageCodeList[2] == "controllerMethodName.failure.infrastructure_web.severity"

      messageCodeList[3] == "default.failure.infrastructure_web.severity.messageSubType"
      messageCodeList[4] == "default.failure.infrastructure_web.messageSubType"
      messageCodeList[5] == "default.failure.infrastructure_web.severity"
      messageCodeList[6] == "default.failure.infrastructure_web"
      messageCodeList[7] == "default.failure.severity"
      messageCodeList[8] == "default.severity"
    }

    where:
    controllerSimpleNameParam | _
    null                      | _
    ""                        | _
    "   "                     | _
  }

  void "should create expected message code list for violation code message of unknown failure"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory", // always fixed to "failure"
        messageType: "messageType",         // always fixed to "unknown"
        messageSubType: "messageSubType",
        severity: "severity",               // always fixed to "error"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfUnknownFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 8

      messageCodeList[0] == "controllerSimpleName.controllerMethodName.failure.unknown.messageSubType"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.failure.unknown"

      messageCodeList[2] == "controllerMethodName.failure.unknown.messageSubType"
      messageCodeList[3] == "controllerMethodName.failure.unknown"

      messageCodeList[4] == "default.failure.unknown.messageSubType"
      messageCodeList[5] == "default.failure.unknown"
      messageCodeList[6] == "default.failure.error"
      messageCodeList[7] == "default.error"
    }
  }

  void "should create expected message code list for violation code message of unknown failure for empty MessageSourceResolvableSpecification"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification()

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfUnknownFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 4

      messageCodeList[0] == "failure.unknown"
      messageCodeList[1] == "default.failure.unknown"
      messageCodeList[2] == "default.failure.error"
      messageCodeList[3] == "default.error"
    }
  }

  void "should create expected message code list for violation code message of unknown failure for missing controllerSimpleName [controllerSimpleName: '#controllerSimpleNameParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: controllerSimpleNameParam,
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory", // always fixed to "failure"
        messageType: "messageType",         // always fixed to "unknown"
        messageSubType: "messageSubType",
        severity: "severity"                // always fixed to "error"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfUnknownFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 6

      messageCodeList[0] == "controllerMethodName.failure.unknown.messageSubType"
      messageCodeList[1] == "controllerMethodName.failure.unknown"

      messageCodeList[2] == "default.failure.unknown.messageSubType"
      messageCodeList[3] == "default.failure.unknown"
      messageCodeList[4] == "default.failure.error"
      messageCodeList[5] == "default.error"
    }

    where:
    controllerSimpleNameParam | _
    null                      | _
    ""                        | _
    "   "                     | _
  }

  void "should create expected message code list for violation code message of validation failure"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory", // always fixed to "failure"
        messageType: "messageType",         // always fixed to "validation"
        messageSubType: "messageSubType",
        severity: "severity",               // always fixed to "warning"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfValidationFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 6

      messageCodeList[0] == "controllerSimpleName.controllerMethodName.failure.validation.messageSubType"
      messageCodeList[1] == "controllerMethodName.failure.validation.messageSubType"

      messageCodeList[2] == "default.failure.validation.messageSubType"
      messageCodeList[3] == "default.failure.validation"
      messageCodeList[4] == "default.failure.warning"
      messageCodeList[5] == "default.warning"
    }
  }

  void "should create expected message code list for violation code message of validation failure for empty MessageSourceResolvableSpecification"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification()

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfValidationFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 4

      messageCodeList[0] == "failure.validation"
      messageCodeList[1] == "default.failure.validation"
      messageCodeList[2] == "default.failure.warning"
      messageCodeList[3] == "default.warning"
    }
  }

  void "should create expected message code list for violation code message of validation failure for missing controllerSimpleName [controllerSimpleName: '#controllerSimpleNameParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: controllerSimpleNameParam,
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory", // always fixed to "failure"
        messageType: "messageType",         // always fixed to "validation"
        messageSubType: "messageSubType",
        severity: "severity"                // always fixed to "warning"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfValidationFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 5

      messageCodeList[0] == "controllerMethodName.failure.validation.messageSubType"

      messageCodeList[1] == "default.failure.validation.messageSubType"
      messageCodeList[2] == "default.failure.validation"
      messageCodeList[3] == "default.failure.warning"
      messageCodeList[4] == "default.warning"
    }

    where:
    controllerSimpleNameParam | _
    null                      | _
    ""                        | _
    "   "                     | _
  }

  void "should create expected message code list for constraint violation message of validation failure"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory", // always fixed to "failure"
        messageType: "messageType",         // always fixed to "validation"
        messageSubType: "messageSubType",
        severity: "severity",               // always fixed to "warning"
        constraintViolationType: "constraintViolationType",
        constraintViolationPropertyPath: "constraintViolationPropertyPath"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeListForConstraintViolationMessageOfValidationFailure(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 12

      messageCodeList[0] == "controllerSimpleName.controllerMethodName.failure.validation.messageSubType.constraintViolationPropertyPath.constraintViolationType"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.failure.validation.messageSubType.constraintViolationType"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.failure.validation.constraintViolationType"

      messageCodeList[3] == "controllerMethodName.failure.validation.messageSubType.constraintViolationPropertyPath.constraintViolationType"
      messageCodeList[4] == "controllerMethodName.failure.validation.messageSubType.constraintViolationType"
      messageCodeList[5] == "controllerMethodName.failure.validation.constraintViolationType"

      messageCodeList[6] == "default.failure.validation.messageSubType.constraintViolationPropertyPath.constraintViolationType"
      messageCodeList[7] == "default.failure.validation.messageSubType.constraintViolationType"
      messageCodeList[8] == "default.failure.validation.constraintViolationType"
      messageCodeList[9] == "default.failure.validation"
      messageCodeList[10] == "default.failure.warning"
      messageCodeList[11] == "default.warning"
    }
  }

  void "removeLeadingDot - should return null for null param"() {
    expect:
    MessageSourceResolvableHelper.removeLeadingDot(null) == null
  }

  void "removeLeadingDot - should return empty list for empty list param"() {
    given:
    List<String> inputList = []

    expect:
    MessageSourceResolvableHelper.removeLeadingDot(inputList) == inputList
  }

  void "removeLeadingDot - should work with list containing null or empty strings"() {
    given:
    List<String> inputList = [".abc", "", "   ", null, "123", ".bcd"]
    List<String> withoutLeadingDotsList = MessageSourceResolvableHelper.removeLeadingDot(inputList)

    expect:
    withoutLeadingDotsList.size() == 6

    withoutLeadingDotsList[0] == "abc"
    withoutLeadingDotsList[1] == ""
    withoutLeadingDotsList[2] == "   "
    withoutLeadingDotsList[3] == null
    withoutLeadingDotsList[4] == "123"
    withoutLeadingDotsList[5] == "bcd"
  }

  void "removeStandaloneStrings - should return null for null param"() {
    expect:
    MessageSourceResolvableHelper.removeStandaloneStrings(null) == null
  }

  void "removeStandaloneStrings - should return empty list for empty list param"() {
    given:
    List<String> inputList = []

    expect:
    MessageSourceResolvableHelper.removeStandaloneStrings(inputList) == inputList
  }

  void "removeStandaloneStrings - should work with list containing null or empty strings"() {
    given:
    List<String> inputList = [".abc", "", "   ", null, "123", ".bcd"]
    List<String> withoutLeadingDotsList = MessageSourceResolvableHelper.removeStandaloneStrings(inputList)

    expect:
    withoutLeadingDotsList.size() == 2

    withoutLeadingDotsList[0] == ".abc"
    withoutLeadingDotsList[1] == ".bcd"
  }
}
