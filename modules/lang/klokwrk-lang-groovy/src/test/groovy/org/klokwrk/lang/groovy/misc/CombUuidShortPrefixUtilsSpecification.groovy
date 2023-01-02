/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lang.groovy.misc

import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class CombUuidShortPrefixUtilsSpecification extends Specification {
  void "makeCombShortPrefix() should work"() {
    when:
    UUID uuid = CombUuidShortPrefixUtils.makeCombShortPrefix()

    then:
    uuid
    RandomUuidUtils.checkIfRandomUuid(uuid)
    CombUuidShortPrefixUtils.checkIfCombShortPrefixStringIsBounded(uuid.toString(), Clock.systemUTC(), 0, 1)
  }

  void "makeCombShortPrefix() should work with provided clock"() {
    given:
    // The time whose bounded minute is 0. As default tolerance is +- 10 minutes, we can have short COMB UUID prefixes (of 4 hex digits) from fff6 to 000A
    Clock clock = Clock.fixed(Instant.parse("2022-03-18T03:44:00Z"), ZoneOffset.UTC)

    when:
    UUID uuid = CombUuidShortPrefixUtils.makeCombShortPrefix(clock)

    then:
    uuid
    RandomUuidUtils.checkIfRandomUuid(uuid)
    CombUuidShortPrefixUtils.checkIfCombShortPrefixIsBounded(uuid, clock, 0, 1)
  }

  void "makeCombShortPrefix() should fail for invalid parameters"() {
    when:
    CombUuidShortPrefixUtils.makeCombShortPrefix(null)

    then:
    thrown(AssertionError)
  }

  void "deriveCombShortPrefixHexaString() should work"() {
    given:
    Long currentTimeMinute = Instant.now().toEpochMilli().intdiv(60_000) % (2 ** 16)
    String expectedHexaString = Long.toHexString(currentTimeMinute)

    when:
    String actualHexaString = CombUuidShortPrefixUtils.deriveCombShortPrefixHexaString()

    then:
    actualHexaString == expectedHexaString
  }

  void "deriveCombShortPrefixHexaString() should work with provided clock"() {
    given:
    // The time whose bounded minute is 0. As default tolerance is +- 10 minutes, we can have short COMB UUID prefixes (of 4 hex digits) from fff6 to 000A
    Clock clock = Clock.fixed(Instant.parse("2022-03-18T03:44:00Z"), ZoneOffset.UTC)

    Long currentTimeMinute = Instant.now(clock).toEpochMilli().intdiv(60_000) % (2 ** 16)
    String expectedHexaString = Long.toHexString(currentTimeMinute)

    when:
    String actualHexaString = CombUuidShortPrefixUtils.deriveCombShortPrefixHexaString(clock)

    then:
    actualHexaString == expectedHexaString
  }

  void "deriveCombShortPrefixHexaString() should fail for invalid parameters"() {
    when:
    CombUuidShortPrefixUtils.deriveCombShortPrefixHexaString(null)

    then:
    thrown(AssertionError)
  }

  void "checkIfCombShortPrefixIsBounded() should work"() {
    when:
    Boolean result = CombUuidShortPrefixUtils.checkIfCombShortPrefixIsBounded(combShortPrefixParam)

    then:
    result == resultParam

    where:
    combShortPrefixParam                                    | resultParam
    null                                                    | false
    UUID.fromString("00000000-0000-0000-0000-000000000000") | false
    CombUuidShortPrefixUtils.makeCombShortPrefix()          | true
  }

  void "checkIfCombShortPrefixIsBounded() should work with provided clock and bounded minute wrapping"() {
    given:
    // The time whose bounded minute is 0. As default tolerance is +- 10 minutes, we can have short COMB UUID prefixes (of 4 hex digits) from fff6 to 000A
    Clock clock = Clock.fixed(Instant.parse("2022-03-18T03:44:00Z"), ZoneOffset.UTC)
    UUID combShortPrefixToCheck = UUID.fromString(combShortPrefixStringParam)

    when:
    Boolean result = CombUuidShortPrefixUtils.checkIfCombShortPrefixIsBounded(combShortPrefixToCheck, clock)

    then:
    result == resultParam

    where:
    combShortPrefixStringParam             | resultParam
    "00000000-0000-4000-8000-000000000000" | true
    "000A0000-0000-4000-8000-000000000000" | true
    "FFF60000-0000-4000-8000-000000000000" | true

    "000B0000-0000-4000-8000-000000000000" | false
    "FFF50000-0000-4000-8000-000000000000" | false
  }

  void "checkIfCombShortPrefixIsBounded() should work with provided clock and without bounded minute wrapping"() {
    given:
    // The time whose bounded minute is 100 (0064). As default tolerance is +- 10 minutes, we can have short COMB UUID prefixes (of 4 hex digits) from 005A to 006E
    Clock clock = Clock.fixed(Instant.parse("2022-03-18T05:24:00Z"), ZoneOffset.UTC)
    UUID combShortPrefixToCheck = UUID.fromString(combShortPrefixStringParam)

    when:
    Boolean result = CombUuidShortPrefixUtils.checkIfCombShortPrefixIsBounded(combShortPrefixToCheck, clock)

    then:
    result == resultParam

    where:
    combShortPrefixStringParam             | resultParam
    "00640000-0000-4000-8000-000000000000" | true
    "006E0000-0000-4000-8000-000000000000" | true
    "005A0000-0000-4000-8000-000000000000" | true

    "006F0000-0000-4000-8000-000000000000" | false
    "00590000-0000-4000-8000-000000000000" | false
  }

  void "checkIfCombShortPrefixIsBounded() should work with provided inPastMinutesBound and inFutureMinutesBound"() {
    given:
    // The time whose bounded minute is 0. As default tolerance is +- 10 minutes, we can have short COMB UUID prefixes (of 4 hex digits) from fff6 to 000A
    Clock clock = Clock.fixed(Instant.parse("2022-03-18T03:44:00Z"), ZoneOffset.UTC)
    UUID combShortPrefixToCheck = UUID.fromString(combShortPrefixStringParam)

    when:
    Boolean result = CombUuidShortPrefixUtils.checkIfCombShortPrefixIsBounded(combShortPrefixToCheck, clock, 3, 5)

    then:
    result == resultParam

    where:
    combShortPrefixStringParam             | resultParam
    "00000000-0000-4000-8000-000000000000" | true
    "00050000-0000-4000-8000-000000000000" | true
    "FFFD0000-0000-4000-8000-000000000000" | true

    "00060000-0000-4000-8000-000000000000" | false
    "FFFC0000-0000-4000-8000-000000000000" | false
  }

  void "checkIfCombShortPrefixIsBounded() should fail for invalid parameters"() {
    when:
    CombUuidShortPrefixUtils.checkIfCombShortPrefixIsBounded(null, clockParam, inPastMinutesBoundParam, inFutureMinutesBoundParam)

    then:
    thrown(AssertionError)

    where:
    clockParam        | inPastMinutesBoundParam | inFutureMinutesBoundParam
    null              | 1                       | 1

    Clock.systemUTC() | null                    | 1
    Clock.systemUTC() | -1                      | 1

    Clock.systemUTC() | 1                       | null
    Clock.systemUTC() | 1                       | -1
  }

  void "checkIfCombShortPrefixStringIsBounded() should work"() {
    when:
    Boolean result = CombUuidShortPrefixUtils.checkIfCombShortPrefixStringIsBounded(combShortPrefixStringParam)

    then:
    result == resultParam

    where:
    combShortPrefixStringParam                                | resultParam
    null                                                      | false
    ""                                                        | false
    "  "                                                      | false
    "1"                                                       | false
    "Z"                                                       | false
    " 00000000-0000-0000-0000-000000000000"                   | false
    "00000000-0000-0000-0000-000000000000 "                   | false
    " 00000000-0000-0000-0000-000000000000 "                  | false
    "00000000-0000-0000-0000-000000000000"                    | false

    CombUuidShortPrefixUtils.makeCombShortPrefix().toString() | true
  }
}
