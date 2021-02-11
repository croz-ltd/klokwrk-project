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
      messageCodeList.size() == 30

      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.severity.some.property.path"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.severity"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType"

      messageCodeList[4] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.severity.some.property.path"
      messageCodeList[5] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.some.property.path"
      messageCodeList[6] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.severity"
      messageCodeList[7] == "controllerSimpleName.controllerMethodName.messageCategory.messageType"

      messageCodeList[8] == "controllerSimpleName.controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[9] == "controllerSimpleName.controllerMethodName.messageCategory.some.property.path"
      messageCodeList[10] == "controllerSimpleName.controllerMethodName.messageCategory.severity"
      messageCodeList[11] == "controllerSimpleName.controllerMethodName.messageCategory"

      messageCodeList[12] == "controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[13] == "controllerMethodName.messageCategory.some.property.path"
      messageCodeList[14] == "controllerMethodName.messageCategory.severity"
      messageCodeList[15] == "controllerMethodName.messageCategory"

      messageCodeList[16] == "default.messageCategory.messageType.messageSubType.severity.some.property.path"
      messageCodeList[17] == "default.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[18] == "default.messageCategory.messageType.messageSubType.severity"
      messageCodeList[19] == "default.messageCategory.messageType.messageSubType"

      messageCodeList[20] == "default.messageCategory.messageType.severity.some.property.path"
      messageCodeList[21] == "default.messageCategory.messageType.some.property.path"
      messageCodeList[22] == "default.messageCategory.messageType.severity"
      messageCodeList[23] == "default.messageCategory.messageType"

      messageCodeList[24] == "default.messageCategory.severity.some.property.path"
      messageCodeList[25] == "default.messageCategory.some.property.path"
      messageCodeList[26] == "default.messageCategory.severity"
      messageCodeList[27] == "default.messageCategory"

      messageCodeList[28] == "default.severity.some.property.path"
      messageCodeList[29] == "default.severity"
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
      messageCodeList.size() == 26

      messageCodeList[0] == "controllerMethodName.messageCategory.messageType.messageSubType.severity.some.property.path"
      messageCodeList[1] == "controllerMethodName.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[2] == "controllerMethodName.messageCategory.messageType.messageSubType.severity"
      messageCodeList[3] == "controllerMethodName.messageCategory.messageType.messageSubType"

      messageCodeList[4] == "controllerMethodName.messageCategory.messageType.severity.some.property.path"
      messageCodeList[5] == "controllerMethodName.messageCategory.messageType.some.property.path"
      messageCodeList[6] == "controllerMethodName.messageCategory.messageType.severity"
      messageCodeList[7] == "controllerMethodName.messageCategory.messageType"

      messageCodeList[8] == "controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[9] == "controllerMethodName.messageCategory.some.property.path"
      messageCodeList[10] == "controllerMethodName.messageCategory.severity"
      messageCodeList[11] == "controllerMethodName.messageCategory"

      messageCodeList[12] == "default.messageCategory.messageType.messageSubType.severity.some.property.path"
      messageCodeList[13] == "default.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[14] == "default.messageCategory.messageType.messageSubType.severity"
      messageCodeList[15] == "default.messageCategory.messageType.messageSubType"

      messageCodeList[16] == "default.messageCategory.messageType.severity.some.property.path"
      messageCodeList[17] == "default.messageCategory.messageType.some.property.path"
      messageCodeList[18] == "default.messageCategory.messageType.severity"
      messageCodeList[19] == "default.messageCategory.messageType"

      messageCodeList[20] == "default.messageCategory.severity.some.property.path"
      messageCodeList[21] == "default.messageCategory.some.property.path"
      messageCodeList[22] == "default.messageCategory.severity"
      messageCodeList[23] == "default.messageCategory"

      messageCodeList[24] == "default.severity.some.property.path"
      messageCodeList[25] == "default.severity"
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
      messageCodeList.size() == 29

      messageCodeList[0] == "controllerSimpleName.messageCategory.messageType.messageSubType.severity.some.property.path"
      messageCodeList[1] == "controllerSimpleName.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[2] == "controllerSimpleName.messageCategory.messageType.messageSubType.severity"
      messageCodeList[3] == "controllerSimpleName.messageCategory.messageType.messageSubType"

      messageCodeList[4] == "controllerSimpleName.messageCategory.messageType.severity.some.property.path"
      messageCodeList[5] == "controllerSimpleName.messageCategory.messageType.some.property.path"
      messageCodeList[6] == "controllerSimpleName.messageCategory.messageType.severity"
      messageCodeList[7] == "controllerSimpleName.messageCategory.messageType"

      messageCodeList[8] == "controllerSimpleName.messageCategory.severity.some.property.path"
      messageCodeList[9] == "controllerSimpleName.messageCategory.some.property.path"
      messageCodeList[10] == "controllerSimpleName.messageCategory.severity"
      messageCodeList[11] == "controllerSimpleName.messageCategory"

      messageCodeList[12] == "messageCategory.severity.some.property.path"
      messageCodeList[13] == "messageCategory.some.property.path"
      messageCodeList[14] == "messageCategory.severity"

      messageCodeList[15] == "default.messageCategory.messageType.messageSubType.severity.some.property.path"
      messageCodeList[16] == "default.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[17] == "default.messageCategory.messageType.messageSubType.severity"
      messageCodeList[18] == "default.messageCategory.messageType.messageSubType"

      messageCodeList[19] == "default.messageCategory.messageType.severity.some.property.path"
      messageCodeList[20] == "default.messageCategory.messageType.some.property.path"
      messageCodeList[21] == "default.messageCategory.messageType.severity"
      messageCodeList[22] == "default.messageCategory.messageType"

      messageCodeList[23] == "default.messageCategory.severity.some.property.path"
      messageCodeList[24] == "default.messageCategory.some.property.path"
      messageCodeList[25] == "default.messageCategory.severity"
      messageCodeList[26] == "default.messageCategory"

      messageCodeList[27] == "default.severity.some.property.path"
      messageCodeList[28] == "default.severity"
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
      messageCodeList.size() == 26

      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageType.messageSubType.severity.some.property.path"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageType.messageSubType.some.property.path"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageType.messageSubType.severity"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageType.messageSubType"

      messageCodeList[4] == "controllerSimpleName.controllerMethodName.messageType.severity.some.property.path"
      messageCodeList[5] == "controllerSimpleName.controllerMethodName.messageType.some.property.path"
      messageCodeList[6] == "controllerSimpleName.controllerMethodName.messageType.severity"
      messageCodeList[7] == "controllerSimpleName.controllerMethodName.messageType"

      messageCodeList[8] == "controllerSimpleName.controllerMethodName.severity.some.property.path"
      messageCodeList[9] == "controllerSimpleName.controllerMethodName.some.property.path"
      messageCodeList[10] == "controllerSimpleName.controllerMethodName.severity"
      messageCodeList[11] == "controllerSimpleName.controllerMethodName"

      messageCodeList[12] == "controllerMethodName.severity.some.property.path"
      messageCodeList[13] == "controllerMethodName.some.property.path"
      messageCodeList[14] == "controllerMethodName.severity"

      messageCodeList[15] == "default.messageType.messageSubType.severity.some.property.path"
      messageCodeList[16] == "default.messageType.messageSubType.some.property.path"
      messageCodeList[17] == "default.messageType.messageSubType.severity"
      messageCodeList[18] == "default.messageType.messageSubType"

      messageCodeList[19] == "default.messageType.severity.some.property.path"
      messageCodeList[20] == "default.messageType.some.property.path"
      messageCodeList[21] == "default.messageType.severity"
      messageCodeList[22] == "default.messageType"

      messageCodeList[23] == "default.severity.some.property.path"
      messageCodeList[24] == "default.some.property.path"
      messageCodeList[25] == "default.severity"
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
      messageCodeList.size() == 22

      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageSubType.severity.some.property.path"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageSubType.some.property.path"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.messageSubType.severity"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory.messageSubType"

      messageCodeList[4] == "controllerSimpleName.controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[5] == "controllerSimpleName.controllerMethodName.messageCategory.some.property.path"
      messageCodeList[6] == "controllerSimpleName.controllerMethodName.messageCategory.severity"
      messageCodeList[7] == "controllerSimpleName.controllerMethodName.messageCategory"

      messageCodeList[8] == "controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[9] == "controllerMethodName.messageCategory.some.property.path"
      messageCodeList[10] == "controllerMethodName.messageCategory.severity"
      messageCodeList[11] == "controllerMethodName.messageCategory"

      messageCodeList[12] == "default.messageCategory.messageSubType.severity.some.property.path"
      messageCodeList[13] == "default.messageCategory.messageSubType.some.property.path"
      messageCodeList[14] == "default.messageCategory.messageSubType.severity"
      messageCodeList[15] == "default.messageCategory.messageSubType"

      messageCodeList[16] == "default.messageCategory.severity.some.property.path"
      messageCodeList[17] == "default.messageCategory.some.property.path"
      messageCodeList[18] == "default.messageCategory.severity"
      messageCodeList[19] == "default.messageCategory"

      messageCodeList[20] == "default.severity.some.property.path"
      messageCodeList[21] == "default.severity"
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
      messageCodeList.size() == 22

      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.severity.some.property.path"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.some.property.path"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.severity"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory.messageType"

      messageCodeList[4] == "controllerSimpleName.controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[5] == "controllerSimpleName.controllerMethodName.messageCategory.some.property.path"
      messageCodeList[6] == "controllerSimpleName.controllerMethodName.messageCategory.severity"
      messageCodeList[7] == "controllerSimpleName.controllerMethodName.messageCategory"

      messageCodeList[8] == "controllerMethodName.messageCategory.severity.some.property.path"
      messageCodeList[9] == "controllerMethodName.messageCategory.some.property.path"
      messageCodeList[10] == "controllerMethodName.messageCategory.severity"
      messageCodeList[11] == "controllerMethodName.messageCategory"

      messageCodeList[12] == "default.messageCategory.messageType.severity.some.property.path"
      messageCodeList[13] == "default.messageCategory.messageType.some.property.path"
      messageCodeList[14] == "default.messageCategory.messageType.severity"
      messageCodeList[15] == "default.messageCategory.messageType"

      messageCodeList[16] == "default.messageCategory.severity.some.property.path"
      messageCodeList[17] == "default.messageCategory.some.property.path"
      messageCodeList[18] == "default.messageCategory.severity"
      messageCodeList[19] == "default.messageCategory"

      messageCodeList[20] == "default.severity.some.property.path"
      messageCodeList[21] == "default.severity"
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
      messageCodeList.size() == 30

      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.warning.some.property.path"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.warning"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType"

      messageCodeList[4] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.warning.some.property.path"
      messageCodeList[5] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.some.property.path"
      messageCodeList[6] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.warning"
      messageCodeList[7] == "controllerSimpleName.controllerMethodName.messageCategory.messageType"

      messageCodeList[8] == "controllerSimpleName.controllerMethodName.messageCategory.warning.some.property.path"
      messageCodeList[9] == "controllerSimpleName.controllerMethodName.messageCategory.some.property.path"
      messageCodeList[10] == "controllerSimpleName.controllerMethodName.messageCategory.warning"
      messageCodeList[11] == "controllerSimpleName.controllerMethodName.messageCategory"

      messageCodeList[12] == "controllerMethodName.messageCategory.warning.some.property.path"
      messageCodeList[13] == "controllerMethodName.messageCategory.some.property.path"
      messageCodeList[14] == "controllerMethodName.messageCategory.warning"
      messageCodeList[15] == "controllerMethodName.messageCategory"

      messageCodeList[16] == "default.messageCategory.messageType.messageSubType.warning.some.property.path"
      messageCodeList[17] == "default.messageCategory.messageType.messageSubType.some.property.path"
      messageCodeList[18] == "default.messageCategory.messageType.messageSubType.warning"
      messageCodeList[19] == "default.messageCategory.messageType.messageSubType"

      messageCodeList[20] == "default.messageCategory.messageType.warning.some.property.path"
      messageCodeList[21] == "default.messageCategory.messageType.some.property.path"
      messageCodeList[22] == "default.messageCategory.messageType.warning"
      messageCodeList[23] == "default.messageCategory.messageType"

      messageCodeList[24] == "default.messageCategory.warning.some.property.path"
      messageCodeList[25] == "default.messageCategory.some.property.path"
      messageCodeList[26] == "default.messageCategory.warning"
      messageCodeList[27] == "default.messageCategory"

      messageCodeList[28] == "default.warning.some.property.path"
      messageCodeList[29] == "default.warning"
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
      messageCodeList.size() == 15

      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.severity"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.severity"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory.messageType"

      messageCodeList[4] == "controllerSimpleName.controllerMethodName.messageCategory.severity"
      messageCodeList[5] == "controllerSimpleName.controllerMethodName.messageCategory"
      messageCodeList[6] == "controllerMethodName.messageCategory.severity"
      messageCodeList[7] == "controllerMethodName.messageCategory"

      messageCodeList[8] == "default.messageCategory.messageType.messageSubType.severity"
      messageCodeList[9] == "default.messageCategory.messageType.messageSubType"
      messageCodeList[10] == "default.messageCategory.messageType.severity"
      messageCodeList[11] == "default.messageCategory.messageType"

      messageCodeList[12] == "default.messageCategory.severity"
      messageCodeList[13] == "default.messageCategory"

      messageCodeList[14] == "default.severity"
    }

    where:
    propertyPathParam | _
    null              | _
    ""                | _
    "   "             | _
  }
}
