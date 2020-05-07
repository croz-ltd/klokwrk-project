package org.klokwrk.lib.spring.context

import spock.lang.Specification
import spock.lang.Unroll

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
        propertyPath: "propertyPath"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 12
      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.severity.propertyPath"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.propertyPath"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.severity.propertyPath"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory.propertyPath"
      messageCodeList[4] == "controllerMethodName.messageCategory.severity.propertyPath"
      messageCodeList[5] == "controllerMethodName.messageCategory.propertyPath"
      messageCodeList[6] == "default.messageCategory.messageType.messageSubType.severity.propertyPath"
      messageCodeList[7] == "default.messageCategory.messageType.messageSubType.propertyPath"
      messageCodeList[8] == "default.messageCategory.severity.propertyPath"
      messageCodeList[9] == "default.messageCategory.propertyPath"
      messageCodeList[10] == "default.severity.propertyPath"
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

  @Unroll
  void "should create expected message code list for missing controllerSimpleName [controllerSimpleName: '#controllerSimpleNameParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: controllerSimpleNameParam,
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory",
        messageType: "messageType",
        messageSubType: "messageSubType",
        severity: "severity",
        propertyPath: "propertyPath"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 10
      messageCodeList[0] == "controllerMethodName.messageCategory.messageType.messageSubType.severity.propertyPath"
      messageCodeList[1] == "controllerMethodName.messageCategory.messageType.messageSubType.propertyPath"
      messageCodeList[2] == "controllerMethodName.messageCategory.severity.propertyPath"
      messageCodeList[3] == "controllerMethodName.messageCategory.propertyPath"
      messageCodeList[4] == "default.messageCategory.messageType.messageSubType.severity.propertyPath"
      messageCodeList[5] == "default.messageCategory.messageType.messageSubType.propertyPath"
      messageCodeList[6] == "default.messageCategory.severity.propertyPath"
      messageCodeList[7] == "default.messageCategory.propertyPath"
      messageCodeList[8] == "default.severity.propertyPath"
      messageCodeList[9] == "default.severity"
    }

    where:
    controllerSimpleNameParam | _
    null                      | _
    ""                        | _
    "   "                     | _
  }

  @Unroll
  void "should create expected message code list for missing controllerMethodName [controllerMethodName: '#controllerMethodNameParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: controllerMethodNameParam,
        messageCategory: "messageCategory",
        messageType: "messageType",
        messageSubType: "messageSubType",
        severity: "severity",
        propertyPath: "propertyPath"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 12
      messageCodeList[0] == "controllerSimpleName.messageCategory.messageType.messageSubType.severity.propertyPath"
      messageCodeList[1] == "controllerSimpleName.messageCategory.messageType.messageSubType.propertyPath"
      messageCodeList[2] == "controllerSimpleName.messageCategory.severity.propertyPath"
      messageCodeList[3] == "controllerSimpleName.messageCategory.propertyPath"
      messageCodeList[4] == "messageCategory.severity.propertyPath"
      messageCodeList[5] == "messageCategory.propertyPath"
      messageCodeList[6] == "default.messageCategory.messageType.messageSubType.severity.propertyPath"
      messageCodeList[7] == "default.messageCategory.messageType.messageSubType.propertyPath"
      messageCodeList[8] == "default.messageCategory.severity.propertyPath"
      messageCodeList[9] == "default.messageCategory.propertyPath"
      messageCodeList[10] == "default.severity.propertyPath"
      messageCodeList[11] == "default.severity"
    }

    where:
    controllerMethodNameParam | _
    null                      | _
    ""                        | _
    "   "                     | _
  }

  @Unroll
  void "should create expected message code list for missing messageCategory [messageCategory: '#messageCategoryParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: messageCategoryParam,
        messageType: "messageType",
        messageSubType: "messageSubType",
        severity: "severity",
        propertyPath: "propertyPath"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 11
      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageType.messageSubType.severity.propertyPath"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageType.messageSubType.propertyPath"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.severity.propertyPath"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.propertyPath"
      messageCodeList[4] == "controllerMethodName.severity.propertyPath"
      messageCodeList[5] == "controllerMethodName.propertyPath"
      messageCodeList[6] == "default.messageType.messageSubType.severity.propertyPath"
      messageCodeList[7] == "default.messageType.messageSubType.propertyPath"
      messageCodeList[8] == "default.severity.propertyPath"
      messageCodeList[9] == "default.propertyPath"
      messageCodeList[10] == "default.severity"
    }

    where:
    messageCategoryParam | _
    null                 | _
    ""                   | _
    "   "                | _
  }

  @Unroll
  void "should create expected message code list for missing messageType [messageType: '#messageTypeParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory",
        messageType: messageTypeParam,
        messageSubType: "messageSubType",
        severity: "severity",
        propertyPath: "propertyPath"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 12
      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageSubType.severity.propertyPath"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageSubType.propertyPath"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.severity.propertyPath"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory.propertyPath"
      messageCodeList[4] == "controllerMethodName.messageCategory.severity.propertyPath"
      messageCodeList[5] == "controllerMethodName.messageCategory.propertyPath"
      messageCodeList[6] == "default.messageCategory.messageSubType.severity.propertyPath"
      messageCodeList[7] == "default.messageCategory.messageSubType.propertyPath"
      messageCodeList[8] == "default.messageCategory.severity.propertyPath"
      messageCodeList[9] == "default.messageCategory.propertyPath"
      messageCodeList[10] == "default.severity.propertyPath"
      messageCodeList[11] == "default.severity"
    }

    where:
    messageTypeParam | _
    null             | _
    ""               | _
    "   "            | _
  }

  @Unroll
  void "should create expected message code list for missing messageSubType [messageSubType: '#messageSubTypeParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory",
        messageType: "messageType",
        messageSubType: messageSubTypeParam,
        severity: "severity",
        propertyPath: "propertyPath"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 12
      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.severity.propertyPath"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.propertyPath"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.severity.propertyPath"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory.propertyPath"
      messageCodeList[4] == "controllerMethodName.messageCategory.severity.propertyPath"
      messageCodeList[5] == "controllerMethodName.messageCategory.propertyPath"
      messageCodeList[6] == "default.messageCategory.messageType.severity.propertyPath"
      messageCodeList[7] == "default.messageCategory.messageType.propertyPath"
      messageCodeList[8] == "default.messageCategory.severity.propertyPath"
      messageCodeList[9] == "default.messageCategory.propertyPath"
      messageCodeList[10] == "default.severity.propertyPath"
      messageCodeList[11] == "default.severity"
    }

    where:
    messageSubTypeParam | _
    null                | _
    ""                  | _
    "   "               | _
  }

  @Unroll
  void "should create expected message code list for missing severity [severity: '#severityParam']"() {
    given:
    MessageSourceResolvableSpecification messageSourceResolvableSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: "controllerSimpleName",
        controllerMethodName: "controllerMethodName",
        messageCategory: "messageCategory",
        messageType: "messageType",
        messageSubType: "messageSubType",
        severity: severityParam,
        propertyPath: "propertyPath"
    )

    when:
    List<String> messageCodeList = MessageSourceResolvableHelper.createMessageCodeList(messageSourceResolvableSpecification)

    then:
    verifyAll {
      messageCodeList.size() == 12
      messageCodeList[0] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.warning.propertyPath"
      messageCodeList[1] == "controllerSimpleName.controllerMethodName.messageCategory.messageType.messageSubType.propertyPath"
      messageCodeList[2] == "controllerSimpleName.controllerMethodName.messageCategory.warning.propertyPath"
      messageCodeList[3] == "controllerSimpleName.controllerMethodName.messageCategory.propertyPath"
      messageCodeList[4] == "controllerMethodName.messageCategory.warning.propertyPath"
      messageCodeList[5] == "controllerMethodName.messageCategory.propertyPath"
      messageCodeList[6] == "default.messageCategory.messageType.messageSubType.warning.propertyPath"
      messageCodeList[7] == "default.messageCategory.messageType.messageSubType.propertyPath"
      messageCodeList[8] == "default.messageCategory.warning.propertyPath"
      messageCodeList[9] == "default.messageCategory.propertyPath"
      messageCodeList[10] == "default.warning.propertyPath"
      messageCodeList[11] == "default.warning"
    }

    where:
    severityParam | _
    null          | _
    ""            | _
    "   "         | _
  }

  @Unroll
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
