/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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
  void "should create expected message code list for fully specified MessageSourceResolvableSpecification"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory",
        messageType: "messageType",
        messageSubType: "messageSubType",
        severity: "severity",
        propertyPath: "some.property.path"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 12
      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.severity.some.property.path"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory.some.property.path"
      messageCodeList[4] == "controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[5] == "controllerMethodName.messageCategory.some.property.path"
      messageCodeList[6] == "default.messageCategory.messageType.messageSubType.severity.some.property.path"
      messageCodeList[7] == "default.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[8] == "default.messageCategory.severity.some.property.path"
      messageCodeList[9] == "default.messageCategory.some.property.path"
      messageCodeList[10] == "default.severity.some.property.path"
      messageCodeList[11] == "default.severity"
    }
  }

  void "should create expected message code list for empty MessageSourceResolvableSpecification"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification()

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 1
      messageCodeList[0] == "default.warning"
    }
  }

  void "should create expected message code list for missing controllerSimpleName [controllerSimpleName: '#controllerSimpleNameParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: controllerSimpleNameParam,
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory",
        messageType: "messageType",
        messageSubType: "messageSubType",
        severity: "severity",
        propertyPath: "some.property.path"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 10
      messageCodeList[0] == "controllerMethodName.messageCategory.messageType.messageSubType.severity.some.property.path"
      messageCodeList[1] == "controllerMethodName.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[2] == "controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[3] == "controllerMethodName.messageCategory.some.property.path"
      messageCodeList[4] == "default.messageCategory.messageType.messageSubType.severity.some.property.path"
      messageCodeList[5] == "default.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[6] == "default.messageCategory.severity.some.property.path"
      messageCodeList[7] == "default.messageCategory.some.property.path"
      messageCodeList[8] == "default.severity.some.property.path"
      messageCodeList[9] == "default.severity"
    }

    where:
    controllerSimpleNameParam | _
    null                      | _
    ""                        | _
    "   "                     | _
  }

  void "should create expected message code list for missing controllerMethodName [controllerMethodName: '#controllerMethodNameParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: controllerMethodNameParam,
        messageCategory: "messageCategory",
        messageType: "messageType",
        messageSubType: "messageSubType",
        severity: "severity",
        propertyPath: "some.property.path"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 12
      messageCodeList[0] == "controllerSimpleName.messageCategory.messageType.messageSubType.severity.some.property.path"
      messageCodeList[1] == "controllerSimpleName.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[2] == "controllerSimpleName.messageCategory.severity.some.property.path"
      messageCodeList[3] == "controllerSimpleName.messageCategory.some.property.path"
      messageCodeList[4] == "messageCategory.severity.some.property.path"
      messageCodeList[5] == "messageCategory.some.property.path"
      messageCodeList[6] == "default.messageCategory.messageType.messageSubType.severity.some.property.path"
      messageCodeList[7] == "default.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[8] == "default.messageCategory.severity.some.property.path"
      messageCodeList[9] == "default.messageCategory.some.property.path"
      messageCodeList[10] == "default.severity.some.property.path"
      messageCodeList[11] == "default.severity"
    }

    where:
    controllerMethodNameParam | _
    null                      | _
    ""                        | _
    "   "                     | _
  }

  void "should create expected message code list for missing messageCategory [messageCategory: '#messageCategoryParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: messageCategoryParam,
        messageType: "messageType",
        messageSubType: "messageSubType",
        severity: "severity",
        propertyPath: "some.property.path"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 11
      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageType.messageSubType.severity.some.property.path"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageType.messageSubType.some.property.path"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.severity.some.property.path"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.some.property.path"
      messageCodeList[4] == "controllerMethodName.severity.some.property.path"
      messageCodeList[5] == "controllerMethodName.some.property.path"
      messageCodeList[6] == "default.messageType.messageSubType.severity.some.property.path"
      messageCodeList[7] == "default.messageType.messageSubType.some.property.path"
      messageCodeList[8] == "default.severity.some.property.path"
      messageCodeList[9] == "default.some.property.path"
      messageCodeList[10] == "default.severity"
    }

    where:
    messageCategoryParam | _
    null                 | _
    ""                   | _
    "   "                | _
  }

  void "should create expected message code list for missing messageType [messageType: '#messageTypeParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory",
        messageType: messageTypeParam,
        messageSubType: "messageSubType",
        severity: "severity",
        propertyPath: "some.property.path"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 12
      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageSubType.severity.some.property.path"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageSubType.some.property.path"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory.some.property.path"
      messageCodeList[4] == "controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[5] == "controllerMethodName.messageCategory.some.property.path"
      messageCodeList[6] == "default.messageCategory.messageSubType.severity.some.property.path"
      messageCodeList[7] == "default.messageCategory.messageSubType.some.property.path"
      messageCodeList[8] == "default.messageCategory.severity.some.property.path"
      messageCodeList[9] == "default.messageCategory.some.property.path"
      messageCodeList[10] == "default.severity.some.property.path"
      messageCodeList[11] == "default.severity"
    }

    where:
    messageTypeParam | _
    null             | _
    ""               | _
    "   "            | _
  }

  void "should create expected message code list for missing messageSubType [messageSubType: '#messageSubTypeParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory",
        messageType: "messageType",
        messageSubType: messageSubTypeParam,
        severity: "severity",
        propertyPath: "some.property.path"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 12
      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.severity.some.property.path"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.some.property.path"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory.some.property.path"
      messageCodeList[4] == "controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[5] == "controllerMethodName.messageCategory.some.property.path"
      messageCodeList[6] == "default.messageCategory.messageType.severity.some.property.path"
      messageCodeList[7] == "default.messageCategory.messageType.some.property.path"
      messageCodeList[8] == "default.messageCategory.severity.some.property.path"
      messageCodeList[9] == "default.messageCategory.some.property.path"
      messageCodeList[10] == "default.severity.some.property.path"
      messageCodeList[11] == "default.severity"
    }

    where:
    messageSubTypeParam | _
    null                | _
    ""                  | _
    "   "               | _
  }

  void "should create expected message code list for missing severity [severity: '#severityParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory",
        messageType: "messageType",
        messageSubType: "messageSubType",
        severity: severityParam,
        propertyPath: "some.property.path"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 12
      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.warning.some.property.path"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.warning.some.property.path"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory.some.property.path"
      messageCodeList[4] == "controllerMethodName.messageCategory.warning.some.property.path"
      messageCodeList[5] == "controllerMethodName.messageCategory.some.property.path"
      messageCodeList[6] == "default.messageCategory.messageType.messageSubType.warning.some.property.path"
      messageCodeList[7] == "default.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[8] == "default.messageCategory.warning.some.property.path"
      messageCodeList[9] == "default.messageCategory.some.property.path"
      messageCodeList[10] == "default.warning.some.property.path"
      messageCodeList[11] == "default.warning"
    }

    where:
    severityParam | _
    null          | _
    ""            | _
    "   "         | _
  }

  void "should create expected message code list for missing propertyPath [propertyPath: '#propertyPathParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory",
        messageType: "messageType",
        messageSubType: "messageSubType",
        severity: "severity",
        propertyPath: propertyPathParam
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 11
      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.severity"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.severity"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory"
      messageCodeList[4] == "controllerMethodName.messageCategory.severity"
      messageCodeList[5] == "controllerMethodName.messageCategory"
      messageCodeList[6] == "default.messageCategory.messageType.messageSubType.severity"
      messageCodeList[7] == "default.messageCategory.messageType.messageSubType"
      messageCodeList[8] == "default.messageCategory.severity"
      messageCodeList[9] == "default.messageCategory"
      messageCodeList[10] == "default.severity"
    }

    where:
    propertyPathParam | _
    null              | _
    ""                | _
    "   "             | _
  }
}
