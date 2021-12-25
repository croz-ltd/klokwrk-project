package org.klokwrk.cargotracker.booking.domain.model.value

import spock.lang.Specification

class CargoIdSpecification extends Specification {
  void "map constructor should work for valid arguments"() {
    expect:
    new CargoId(identifier: identifierParam)

    where:
    identifierParam                        | _
    UUID.randomUUID()                      | _
    "00000000-0000-4000-8000-000000000000" | _
    "00000000-0000-4000-9000-000000000001" | _
    "11111111-1111-4111-A111-111111111111" | _
  }

  void "map constructor should fail for invalid arguments"() {
    when:
    new CargoId(identifier: identifierParam)

    then:
    AssertionError assertionError = thrown(AssertionError)
    assertionError.message.contains(errorMessagePartParam)

    where:
    identifierParam            | errorMessagePartParam
    null                       | "not(blankOrNullString())"
    ""                         | "not(blankOrNullString())"
    "   "                      | "not(blankOrNullString())"
    "1"                        | "checkIfRandomUuid(identifier)"
    "Z"                        | "checkIfRandomUuid(identifier)"
    " ${ UUID.randomUUID() }"  | "checkIfRandomUuid(identifier)"
    "${ UUID.randomUUID() } "  | "checkIfRandomUuid(identifier)"
    " ${ UUID.randomUUID() } " | "checkIfRandomUuid(identifier)"
  }

  void "createWithGeneratedIdentifier() should produce valid CargoId"() {
    when:
    CargoId.createWithGeneratedIdentifier()

    then:
    noExceptionThrown()
  }

  void "createWithGeneratedIdentifierIfNeeded() should produce valid CargoId for valid parameter"() {
    when:
    CargoId.createWithGeneratedIdentifierIfNeeded(uuidStringParam)

    then:
    noExceptionThrown()

    where:
    uuidStringParam                        | _
    null                                   | _
    ""                                     | _
    UUID.randomUUID().toString()           | _
    "00000000-0000-4000-8000-000000000000" | _
    "00000000-0000-4000-9000-000000000001" | _
    "11111111-1111-4111-A111-111111111111" | _
  }

  void "createWithGeneratedIdentifierIfNeeded() should fail for invalid parameter"() {
    when:
    CargoId.createWithGeneratedIdentifierIfNeeded(uuidStringParam)

    then:
    AssertionError assertionError = thrown(AssertionError)
    assertionError.message.contains(errorMessagePartParam)

    where:
    uuidStringParam                        | errorMessagePartParam
    "   "                                  | "not(blankOrNullString())"
    "1"                                    | "checkIfRandomUuid(identifier)"
    "Z"                                    | "checkIfRandomUuid(identifier)"
    "00000000-0000-0000-0000-000000000000" | "checkIfRandomUuid(identifier)"
    "00000000-0000-4000-0000-000000000000" | "checkIfRandomUuid(identifier)"
    "00000000-0000-4000-1000-000000000000" | "checkIfRandomUuid(identifier)"
    "00000000-0000-4000-7000-000000000000" | "checkIfRandomUuid(identifier)"
    "00000000-0000-4000-C000-000000000000" | "checkIfRandomUuid(identifier)"
  }
}
