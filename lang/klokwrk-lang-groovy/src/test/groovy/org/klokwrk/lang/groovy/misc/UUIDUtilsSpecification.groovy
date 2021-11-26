package org.klokwrk.lang.groovy.misc

import spock.lang.Specification

class UUIDUtilsSpecification extends Specification {

  @SuppressWarnings("GroovyPointlessBoolean")
  void "checkIfRandomUuid - should return true for random UUID string"() {
    expect:
    UUIDUtils.checkIfRandomUuid(uuidStringParam) == true

    where:
    uuidStringParam                        | _
    "${ UUID.randomUUID() }"               | _
    "00000000-0000-4000-8000-000000000000" | _
    "00000000-0000-4000-9000-000000000000" | _
    "00000000-0000-4000-A000-000000000000" | _
    "00000000-0000-4000-B000-000000000000" | _
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "checkIfRandomUuid - should return false for non random UUID string"() {
    expect:
    UUIDUtils.checkIfRandomUuid(uuidStringParam) == false

    where:
    uuidStringParam                          | _
    null                                     | _
    ""                                       | _
    "   "                                    | _
    "123"                                    | _
    "00000000-0000-0000-0000-000000000000"   | _
    "00000000-0000-4000-0000-000000000000"   | _
    "00000000-0000-4000-1000-000000000000"   | _
    "00000000-0000-4000-7000-000000000000"   | _
    "00000000-0000-4000-C000-000000000000"   | _
    " 00000000-0000-4000-8000-000000000000"  | _
    "00000000-0000-4000-8000-000000000000 "  | _
    " 00000000-0000-4000-8000-000000000000 " | _
  }
}
