/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationInfo
import org.klokwrk.lib.xlang.groovy.base.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lib.xlang.groovy.base.misc.CombUuidShortPrefixUtils
import org.klokwrk.lib.xlang.groovy.base.misc.RandomUuidUtils
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkImmutable

import java.time.Clock

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue

/**
 * Represents an identifier of {@code BookingOfferAggregate} without any business meaning.
 * <p/>
 * This identifier is used for the technical purposes of identifying an aggregate in the CQRS/ES system.
 * <p/>
 * This implementation uses random (version 4, variant 2) COMB UUID with the short prefix as a measure against degrading performance with UUIDs in primary keys or unique indexes:
 * <ul>
 *   <li>https://github.com/f4b6a3/uuid-creator/wiki/2.3.-Short-Prefix-COMB</li>
 *   <li>https://www.2ndquadrant.com/en/blog/sequential-uuid-generators/</li>
 *   <li>https://www.percona.com/blog/2019/11/22/uuids-are-popular-but-bad-for-performance-lets-discuss/</li>
 * </ul>
 * <p/>
 * <b>In cases when the caller supplies the UUID identifier, it must also be in random COMB UUID short prefix format within appropriate time-based bounds.</b> That requirement is checked with
 * {@link CombUuidShortPrefixUtils#checkIfCombShortPrefixStringIsBounded(java.lang.String)}.
 * <p/>
 * <b>Notes about factory methods:</b><br/>
 * All factory methods accept several optional parameters. The {@code Clock} parameter is not used only for testing. It can be pretty helpful in real business scenarios.
 * <p/>
 * For example, if we are retrying previously failed batch jobs. In such cases, the current time is not appropriate as a base for validating COMB UUID bounds because all validations will probably
 * fail. Instead, the application service should decide the correct clock to use, possibly based on (metadata) parameters sent by the client or deducted from the client's request.
 * <p/>
 * Note that we should not use similar clock adjustments for real-time interactive requests from human users.
 * <p/>
 * Clock adjustments could decrease the locality of short prefix COMB UUID primary keys in the database's pages. However, all requests from batches should be local between themself, so typical write
 * and read patterns should not be slower, at least not in a measurable amount.
 *
 * @see CombUuidShortPrefixUtils
 * @see CombUuidShortPrefixUtils#checkIfCombShortPrefixStringIsBounded(java.lang.String)
 */
@KwrkImmutable
@CompileStatic
class BookingOfferId implements PostMapConstructorCheckable {
  String identifier

  /**
   * Factory method for creating {@code BookingOfferId} instance.
   * <p/>
   * In general, using this factory method should be avoided. Using the other two factory methods is more explicit depending on the concrete scenario.
   *
   * @param uuidString String in random COMB UUID short prefix format.
   *
   * @see #makeWithGeneratedIdentifier()
   * @see #makeWithGeneratedIdentifierIfNeeded(java.lang.String)
   */
  static BookingOfferId make(
      String uuidString, Clock clock = Clock.systemUTC(),
      Integer inPastMinutesBound = CombUuidShortPrefixUtils.IN_PAST_MINUTES_BOUND_DEFAULT,
      Integer inFutureMinutesBound = CombUuidShortPrefixUtils.IN_FUTURE_MINUTES_BOUND_DEFAULT)
  {
    requireMatch(clock, notNullValue())
    requireMatch(inPastMinutesBound, notNullValue())
    requireMatch(inFutureMinutesBound, notNullValue())

    //noinspection GroovyConstructorNamedArguments
    return new BookingOfferId(identifier: uuidString, clock: clock, inPastMinutesBound: inPastMinutesBound, inFutureMinutesBound: inFutureMinutesBound)
  }

  /**
   * Factory method for creating {@code BookingOfferId} instance with internally generated identifier value in random COMB UUID short prefix format.
   */
  static BookingOfferId makeWithGeneratedIdentifier(
      Clock clock = Clock.systemUTC(),
      Integer inPastMinutesBound = CombUuidShortPrefixUtils.IN_PAST_MINUTES_BOUND_DEFAULT,
      Integer inFutureMinutesBound = CombUuidShortPrefixUtils.IN_FUTURE_MINUTES_BOUND_DEFAULT)
  {
    requireMatch(clock, notNullValue())
    requireMatch(inPastMinutesBound, notNullValue())
    requireMatch(inFutureMinutesBound, notNullValue())

    String uuidStringToUse = CombUuidShortPrefixUtils.makeCombShortPrefix(clock)
    return make(uuidStringToUse, clock, inPastMinutesBound, inFutureMinutesBound)
  }

  /**
   * Factory method for creating {@code BookingOfferId} instance with generated internal identifier value if needed.
   * <p/>
   * When supplied {@code uuidString} is {@code null} or empty string, method will generate internal identifier in COMB UUID short prefix format.
   *
   * @param uuidString String in COMB UUID short prefix format. If {@code null} or empty string, identifier will be generated internally.
   */
  static BookingOfferId makeWithGeneratedIdentifierIfNeeded(
      String uuidString, Clock clock = Clock.systemUTC(),
      Integer inPastMinutesBound = CombUuidShortPrefixUtils.IN_PAST_MINUTES_BOUND_DEFAULT,
      Integer inFutureMinutesBound = CombUuidShortPrefixUtils.IN_FUTURE_MINUTES_BOUND_DEFAULT)
  {
    requireMatch(clock, notNullValue())
    requireMatch(inPastMinutesBound, notNullValue())
    requireMatch(inFutureMinutesBound, notNullValue())

    String uuidStringToUse = uuidString?.trim() ?: CombUuidShortPrefixUtils.makeCombShortPrefix(clock)

    return make(uuidStringToUse, clock, inPastMinutesBound, inFutureMinutesBound)
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(identifier, not(blankOrNullString()))
    requireRandomUuidIdentifier(identifier)
    requireCorrectlyBoundedCombUuidShortPrefixIdentifier(identifier, constructorArguments)
  }

  private void requireRandomUuidIdentifier(String identifier) {
    if (!RandomUuidUtils.checkIfRandomUuidString(identifier)) {
      throw new DomainException(ViolationInfo.makeForBadRequestWithCustomCodeKey("bookingOfferId.identifier.notRandomUuid"))
    }
  }

  @SuppressWarnings("CodeNarc.DuplicateStringLiteral")
  private void requireCorrectlyBoundedCombUuidShortPrefixIdentifier(String identifier, Map<String, ?> constructorArguments) {
    Clock clock = Clock.systemUTC()
    if (constructorArguments["clock"] != null && Clock.isInstance(constructorArguments["clock"])) {
      clock = constructorArguments["clock"] as Clock
    }

    Integer inPastMinutesBound = CombUuidShortPrefixUtils.IN_PAST_MINUTES_BOUND_DEFAULT
    if (constructorArguments["inPastMinutesBound"] != null && Integer.isInstance(constructorArguments["inPastMinutesBound"])) {
      inPastMinutesBound = constructorArguments["inPastMinutesBound"] as Integer
    }

    Integer inFutureMinutesBound = CombUuidShortPrefixUtils.IN_FUTURE_MINUTES_BOUND_DEFAULT
    if (constructorArguments["inFutureMinutesBound"] != null && Integer.isInstance(constructorArguments["inFutureMinutesBound"])) {
      inFutureMinutesBound = constructorArguments["inFutureMinutesBound"] as Integer
    }

    if (!CombUuidShortPrefixUtils.checkIfCombShortPrefixStringIsBounded(identifier, clock, inPastMinutesBound, inFutureMinutesBound)) {
      throw new DomainException(ViolationInfo.makeForBadRequestWithCustomCodeKey("bookingOfferId.identifier.shortPrefixCombUuidNotInAllowedTimeRangeBounds"))
    }
  }
}
