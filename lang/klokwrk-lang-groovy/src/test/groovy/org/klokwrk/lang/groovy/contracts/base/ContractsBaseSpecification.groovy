package org.klokwrk.lang.groovy.contracts.base

import spock.lang.Specification

class ContractsBaseSpecification extends Specification {

  @SuppressWarnings("ComparisonOfTwoConstants")
  void "requireTrueBase - should work for truthy condition"() {
    when:
    ContractsBase.requireTrueBase(condition)

    then:
    true

    where:
    condition        | _
    true             | _
    123 as Boolean   | _
    "123" as Boolean | _
    1 < 10           | _
    valid()          | _
  }

  private Boolean valid() {
    return true
  }

  @SuppressWarnings("ComparisonOfTwoConstants")
  void "requireTrueBase - should work for non truthy condition as expected"() {
    when:
    ContractsBase.requireTrueBase(condition)

    then:
    AssertionError requireFailedError = thrown()
    requireFailedError.message == "${ ContractsBase.REQUIRE_TRUE_MESSAGE_DEFAULT }."

    where:
    condition     | _
    false         | _
    0 as Boolean  | _
    "" as Boolean | _
    1 > 10        | _
    invalid()     | _
  }

  private Boolean invalid() {
    return false
  }

  void "requireTrueBase - should work for non truthy condition with null or empty message  - [message: '#message']"() {
    when:
    ContractsBase.requireTrueBase(false, null)

    then:
    AssertionError requireFailedError = thrown()
    requireFailedError.message == "${ ContractsBase.REQUIRE_TRUE_MESSAGE_DEFAULT }."

    where:
    message | _
    null    | _
    ""      | _
    "   "   | _
  }

  @SuppressWarnings("ComparisonOfTwoConstants")
  void "requireTrueBase - should work for non truthy condition with custom message - [message: '#message']"() {
    when:
    ContractsBase.requireTrueBase(condition, message)

    then:
    AssertionError requireFailedError = thrown()
    requireFailedError.message == message

    where:
    condition     | message
    false         | "false"
    0 as Boolean  | "0"
    "" as Boolean | "empty string"
    1 > 10        | "1 > 10"
    invalid()     | "invalid()"
  }
}
