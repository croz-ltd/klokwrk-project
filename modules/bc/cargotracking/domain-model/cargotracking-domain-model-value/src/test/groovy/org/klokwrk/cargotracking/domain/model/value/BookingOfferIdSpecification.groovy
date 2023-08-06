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
package org.klokwrk.cargotracking.domain.model.value

import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.lib.xlang.groovy.base.misc.CombUuidShortPrefixUtils
import org.klokwrk.lib.xlang.groovy.base.misc.RandomUuidUtils
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class BookingOfferIdSpecification extends Specification {
  void "map constructor should work for valid arguments"() {
    when:
    new BookingOfferId(identifier: identifierParam)

    then:
    noExceptionThrown()

    where:
    identifierParam                                          | _
    "${ shortPrefix() }0000-0000-4000-8000-000000000000" | _
    "${ shortPrefix() }0000-0000-4000-8000-000000000000" | _
    "${ shortPrefix() }0000-0000-4000-8000-000000000000" | _
    "${ shortPrefix() }0000-0000-4000-8000-000000000000" | _

    "${ shortPrefix() }0000-0000-4000-9000-000000000001" | _
    "${ shortPrefix() }1111-1111-4111-A111-111111111111" | _
  }

  /**
   * Returns hexa String representing 2 byte (4 hexa digits) COMB short prefix for current time.
   */
  private static String shortPrefix() {
    return CombUuidShortPrefixUtils.deriveCombShortPrefixHexaString()
  }

  void "map constructor should work for valid auxiliary argument of clock is provided"() {
    given:
    // The time whose bounded minute is 0. As default tolerance is +- 10 minutes, we can have short COMB UUID prefixes (of 4 hex digits) from fff6 to 000A
    Clock clock = Clock.fixed(Instant.parse("2022-03-18T03:44:00Z"), ZoneOffset.UTC)

    when:
    //noinspection GroovyConstructorNamedArguments
    new BookingOfferId(identifier: identifierParam, clock: clock)

    then:
    noExceptionThrown()

    where:
    combShortPrefixParam | identifierParam
    "0000"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "0001"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "000A"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "FFF6"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"

    "0001"               | "${ combShortPrefixParam }0000-0000-4000-9000-000000000001"
    "0001"               | "${ combShortPrefixParam }1111-1111-4111-A111-111111111111"
  }

  void "map constructor should work for valid arguments when auxiliary arguments of inPastMinutesBound and inFutureMinutesBound are provided"() {
    given:
    // The time whose bounded minute is 0. As default tolerance is +- 10 minutes, we can have short COMB UUID prefixes (of 4 hex digits) from fff6 to 000A
    Clock clock = Clock.fixed(Instant.parse("2022-03-18T03:44:00Z"), ZoneOffset.UTC)

    when:
    //noinspection GroovyConstructorNamedArguments
    new BookingOfferId(identifier: identifierParam, clock: clock, inPastMinutesBound: 3, inFutureMinutesBound: 5)

    then:
    noExceptionThrown()

    where:
    combShortPrefixParam | identifierParam
    "0000"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"

    "0001"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "0002"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "0003"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "0004"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "0005"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"

    "FFFF"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "FFFE"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "FFFD"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
  }

  void "map constructor should work for valid arguments when invalid auxiliary argument of clock is provided - defaults are"() {
    when:
    //noinspection GroovyConstructorNamedArguments
    new BookingOfferId(identifier: identifierParam, clock: new Object())

    then:
    noExceptionThrown()

    where:
    identifierParam                                          | _
    "${ shortPrefix() }0000-0000-4000-8000-000000000000" | _
    "${ shortPrefix() }0000-0000-4000-8000-000000000000" | _
    "${ shortPrefix() }0000-0000-4000-8000-000000000000" | _
    "${ shortPrefix() }0000-0000-4000-8000-000000000000" | _

    "${ shortPrefix() }0000-0000-4000-9000-000000000001" | _
    "${ shortPrefix() }1111-1111-4111-A111-111111111111" | _
  }

  void "map constructor should work for valid arguments when invalid auxiliary arguments of inPastMinutesBound and inFutureMinutesBound are provided - defaults are used"() {
    given:
    // The time whose bounded minute is 0. As default tolerance is +- 10 minutes, we can have short COMB UUID prefixes (of 4 hex digits) from fff6 to 000A
    Clock clock = Clock.fixed(Instant.parse("2022-03-18T03:44:00Z"), ZoneOffset.UTC)

    when:
    //noinspection GroovyConstructorNamedArguments
    new BookingOfferId(identifier: identifierParam, clock: clock, inPastMinutesBound: new Object(), inFutureMinutesBound: new Object())

    then:
    noExceptionThrown()

    where:
    combShortPrefixParam | identifierParam
    "0000"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "0001"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "000A"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "FFFF"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "FFF6"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
  }

  void "map constructor should fail for invalid arguments - null or blank identifier"() {
    when:
    new BookingOfferId(identifier: identifierParam)

    then:
    AssertionError assertionError = thrown(AssertionError)
    assertionError.message.contains(errorMessagePartParam)

    where:
    identifierParam                        | errorMessagePartParam
    null                                   | "not(blankOrNullString())"
    ""                                     | "not(blankOrNullString())"
    "   "                                  | "not(blankOrNullString())"
  }

  void "map constructor should fail for invalid arguments - invalid uuid"() {
    given:
    // The time whose bounded minute is 0. As default tolerance is +- 10 minutes, we can have short COMB UUID prefixes (of 4 hex digits) from fff6 to 000A
    Clock clock = Clock.fixed(Instant.parse("2022-03-18T03:44:00Z"), ZoneOffset.UTC)

    when:
    //noinspection GroovyConstructorNamedArguments
    new BookingOfferId(identifier: identifierParam, clock: clock)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.resolvableMessageKey == resolvableMessageKeyParam

    where:
    identifierParam                        | resolvableMessageKeyParam
    "1"                                    | "bookingOfferId.identifier.notRandomUuid"
    "Z"                                    | "bookingOfferId.identifier.notRandomUuid"
    " ${ UUID.randomUUID() }"              | "bookingOfferId.identifier.notRandomUuid"
    "${ UUID.randomUUID() } "              | "bookingOfferId.identifier.notRandomUuid"
    " ${ UUID.randomUUID() } "             | "bookingOfferId.identifier.notRandomUuid"

    "00000000-0000-4000-0000-000000000000" | "bookingOfferId.identifier.notRandomUuid"
    "00000000-0000-4000-1000-000000000000" | "bookingOfferId.identifier.notRandomUuid"
    "00000000-0000-4000-7000-000000000000" | "bookingOfferId.identifier.notRandomUuid"
    "00000000-0000-4000-C000-000000000000" | "bookingOfferId.identifier.notRandomUuid"

    "10000000-0000-4000-8000-000000000000" | "bookingOfferId.identifier.shortPrefixCombUuidNotInAllowedTimeRangeBounds"
    "FFF50000-0000-4000-8000-000000000000" | "bookingOfferId.identifier.shortPrefixCombUuidNotInAllowedTimeRangeBounds"
    "000B0000-0000-4000-8000-000000000000" | "bookingOfferId.identifier.shortPrefixCombUuidNotInAllowedTimeRangeBounds"
  }

  void "make() should produce valid BookingOfferId for valid arguments"() {
    when:
    String uuidCombWithShortPrefix = CombUuidShortPrefixUtils.makeCombShortPrefix()
    BookingOfferId bookingOfferId = BookingOfferId.make(uuidCombWithShortPrefix)

    then:
    noExceptionThrown()
    RandomUuidUtils.checkIfRandomUuidString(bookingOfferId.identifier)
    CombUuidShortPrefixUtils.checkIfCombShortPrefixStringIsBounded(bookingOfferId.identifier)
  }

  void "make() should produce valid BookingOfferId for valid arguments - provided clock"() {
    // The time whose bounded minute is 0. As default tolerance is +- 10 minutes, we can have short COMB UUID prefixes (of 4 hex digits) from fff6 to 000A
    Clock clock = Clock.fixed(Instant.parse("2022-03-18T03:44:00Z"), ZoneOffset.UTC)

    when:
    //noinspection GroovyConstructorNamedArguments
    BookingOfferId bookingOfferId = BookingOfferId.make(identifierParam, clock)

    then:
    noExceptionThrown()
    RandomUuidUtils.checkIfRandomUuidString(bookingOfferId.identifier)
    CombUuidShortPrefixUtils.checkIfCombShortPrefixStringIsBounded(bookingOfferId.identifier, clock)

    where:
    combShortPrefixParam | identifierParam
    "0000"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "0001"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "000A"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"
    "FFF6"               | "${ combShortPrefixParam }0000-0000-4000-8000-000000000000"

    "0001"               | "${ combShortPrefixParam }0000-0000-4000-9000-000000000001"
    "0001"               | "${ combShortPrefixParam }1111-1111-4111-A111-111111111111"
  }

  void "makeWithGeneratedIdentifier() should produce valid BookingOfferId"() {
    when:
    BookingOfferId bookingOfferId = BookingOfferId.makeWithGeneratedIdentifier()

    then:
    noExceptionThrown()
    RandomUuidUtils.checkIfRandomUuidString(bookingOfferId.identifier)
    CombUuidShortPrefixUtils.checkIfCombShortPrefixStringIsBounded(bookingOfferId.identifier)
  }

  void "makeWithGeneratedIdentifier() should fail for invalid parameters"() {
    when:
    BookingOfferId.makeWithGeneratedIdentifier(clockParam, inPastMinutesBoundParam, inFutureMinutesBoundParam)

    then:
    AssertionError assertionError = thrown(AssertionError)
    assertionError.message.contains(errorMessagePartParam)

    where:
    clockParam        | inPastMinutesBoundParam | inFutureMinutesBoundParam | errorMessagePartParam
    null              | 10                      | 10                        | "item: clock, expected: notNullValue()"
    Clock.systemUTC() | null                    | 10                        | "item: inPastMinutesBound, expected: notNullValue()"
    Clock.systemUTC() | 10                      | null                      | "item: inFutureMinutesBound, expected: notNullValue()"
  }

  void "makeWithGeneratedIdentifierIfNeeded() should produce valid BookingOfferId for valid parameter"() {
    when:
    // The time whose bounded minute is 0. As default tolerance is +- 10 minutes, we can have short COMB UUID prefixes (of 4 hex digits) from fff6 to 000A
    Clock clock = Clock.fixed(Instant.parse("2022-03-18T03:44:00Z"), ZoneOffset.UTC)
    BookingOfferId.makeWithGeneratedIdentifierIfNeeded(uuidStringParam, clock)

    then:
    noExceptionThrown()

    where:
    uuidStringParam                        | _
    null                                   | _
    ""                                     | _
    "  "                                   | _

    "00000000-0000-4000-8000-000000000000" | _
    "00000000-0000-4000-9000-000000000001" | _

    "00010000-0000-4000-8000-000000000000" | _
    "FFFF0000-0000-4000-8000-000000000000" | _
    "FFF60000-0000-4000-8000-000000000000" | _
    "000A0000-0000-4000-8000-000000000000" | _

    "00011111-1111-4111-A111-111111111111" | _
  }

  void "makeWithGeneratedIdentifierIfNeeded() with system clock should fail for invalid parameter"() {
    when:
    // The time whose bounded minute is 0. As default tolerance is +- 10 minutes, we can have short COMB UUID prefixes (of 4 hex digits) from fff6 to 000A
    Clock clock = Clock.fixed(Instant.parse("2022-03-18T03:44:00Z"), ZoneOffset.UTC)
    BookingOfferId.makeWithGeneratedIdentifierIfNeeded(uuidStringParam, clock)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.resolvableMessageKey == resolvableMessageKeyParam

    where:
    uuidStringParam                        | resolvableMessageKeyParam
    "1"                                    | "bookingOfferId.identifier.notRandomUuid"
    "Z"                                    | "bookingOfferId.identifier.notRandomUuid"

    "00000000-0000-0000-0000-000000000000" | "bookingOfferId.identifier.notRandomUuid"
    "00000000-0000-4000-0000-000000000000" | "bookingOfferId.identifier.notRandomUuid"
    "00000000-0000-4000-1000-000000000000" | "bookingOfferId.identifier.notRandomUuid"
    "00000000-0000-4000-7000-000000000000" | "bookingOfferId.identifier.notRandomUuid"
    "00000000-0000-4000-C000-000000000000" | "bookingOfferId.identifier.notRandomUuid"

    "${ UUID.randomUUID() }"               | "bookingOfferId.identifier.shortPrefixCombUuidNotInAllowedTimeRangeBounds"
    "000B0000-0000-4000-8000-000000000000" | "bookingOfferId.identifier.shortPrefixCombUuidNotInAllowedTimeRangeBounds"
    "FFF50000-0000-4000-8000-000000000000" | "bookingOfferId.identifier.shortPrefixCombUuidNotInAllowedTimeRangeBounds"
  }
}
