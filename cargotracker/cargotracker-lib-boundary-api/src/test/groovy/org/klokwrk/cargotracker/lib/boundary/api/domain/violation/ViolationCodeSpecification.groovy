package org.klokwrk.cargotracker.lib.boundary.api.domain.violation

import spock.lang.Specification

class ViolationCodeSpecification extends Specification {
  void "map constructor should work for valid parameters"() {
    when:
    ViolationCode violationCode = new ViolationCode(
        code: "someCode", codeMessage: "someCodeMessage", resolvableMessageKey: "someResolvableMessageKey", resolvableMessageParameters: resolvableMessageParametersParam
    )

    then:
    violationCode

    where:
    resolvableMessageParametersParam | _
    []                               | _
    ["paramOne", "paramTwo"]         | _
  }

  void "map constructor should fail for invalid parameters"() {
    when:
    new ViolationCode(
        code: codeParam, codeMessage: codeMessageParam, resolvableMessageKey: resolvableMessageKeyParam, resolvableMessageParameters: resolvableMessageParametersParam
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messageParam)

    where:
    codeParam       | codeMessageParam  | resolvableMessageKeyParam  | resolvableMessageParametersParam | messageParam
    null            | "someCodeMessage" | "someResolvableMessageKey" | []                               | "item: code, expected: not(blankOrNullString())"
    ""              | "someCodeMessage" | "someResolvableMessageKey" | []                               | "item: code, expected: not(blankOrNullString())"
    " "             | "someCodeMessage" | "someResolvableMessageKey" | []                               | "item: code, expected: not(blankOrNullString())"

    "someCodeParam" | null              | "someResolvableMessageKey" | []                               | "item: codeMessage, expected: not(blankOrNullString())"
    "someCodeParam" | ""                | "someResolvableMessageKey" | []                               | "item: codeMessage, expected: not(blankOrNullString())"
    "someCodeParam" | " "               | "someResolvableMessageKey" | []                               | "item: codeMessage, expected: not(blankOrNullString())"

    "someCodeParam" | "someCodeMessage" | null                       | []                               | "item: resolvableMessageKey, expected: not(blankOrNullString())"
    "someCodeParam" | "someCodeMessage" | ""                         | []                               | "item: resolvableMessageKey, expected: not(blankOrNullString())"
    "someCodeParam" | "someCodeMessage" | " "                        | []                               | "item: resolvableMessageKey, expected: not(blankOrNullString())"

    "someCodeParam" | "someCodeMessage" | "someResolvableMessageKey" | null                             | "item: resolvableMessageParameters, expected: notNullValue()"
  }

  void "make factory method should work as expected when optional parameter is not provided"() {
    when:
    ViolationCode violationCode = ViolationCode.make("someString", "someCodeMessage", "someResolvableMessageKey")

    then:
    violationCode
    violationCode.resolvableMessageParameters == []
  }

  void "make factory method should work as expected when optional parameter is provided"() {
    when:
    ViolationCode violationCode = ViolationCode.make("someString", "someCodeMessage", "someResolvableMessageKey", ["paramOne", "paramTwo"])

    then:
    violationCode
    violationCode.resolvableMessageParameters == ["paramOne", "paramTwo"]
  }

  void "make factory method should fail for invalid value of optional parameter"() {
    when:
    ViolationCode.make("someString", "someCodeMessage", "someResolvableMessageKey", null)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("[item: resolvableMessageParameters, expected: notNullValue(), actual: null]")
  }
}
