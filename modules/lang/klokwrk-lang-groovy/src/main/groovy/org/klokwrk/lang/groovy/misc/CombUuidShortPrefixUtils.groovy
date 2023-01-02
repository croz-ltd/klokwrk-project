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

import com.github.f4b6a3.uuid.factory.nonstandard.ShortPrefixCombFactory
import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.contracts.base.ContractsBase

import java.security.SecureRandom
import java.time.Clock
import java.time.Instant

/**
 * Various helper methods for working with random (version 4, variant 2) COMB UUIDs with short prefix (https://github.com/f4b6a3/uuid-creator/wiki/2.3.-Short-Prefix-COMB).
 * <p/>
 * Some implementation notes about {@code SecureRandom} usage:
 * <ul>
 * <li>
 *   By default, {@code uuid-creator} caches random function instances. It looks like this is not necessary, as suggested by
 *   https://stackoverflow.com/questions/27622625/securerandom-with-nativeprng-vs-sha1prng/27638413#27638413 . Therefore, we are not using uuid-creator's {@code new DefaultRandomFunction()}, but
 *   rather plain {@code new SecureRandom()}. This should not affect performance, and should increase randomness.
 * </li>
 * <li>
 *   Looks like plain {@code new SecureRandom()} should be more than enough to get appropriate randomness in platform independent way as suggested by
 *   https://tersesystems.com/blog/2015/12/17/the-right-way-to-use-securerandom/
 * </li>
 * <li>
 *   If it turns out that caching random function is beneficial, we can use {@code new DefaultRandomFunction()} at the application service level as a singleton, and provide it here as a parameter.
 * </li>
 * </ul>
 */
@CompileStatic
class CombUuidShortPrefixUtils {
  static final Integer IN_PAST_MINUTES_BOUND_DEFAULT = 10
  static final Integer IN_FUTURE_MINUTES_BOUND_DEFAULT = 10

  static final Integer ONE_MINUTE_MILLIS = 60000
  static final Integer THE_64_K = 2 ** 16 as Integer

  /**
   * Produces COMB UUID with short prefix for the current time or for provided {@code clock} parameter.
   */
  static UUID makeCombShortPrefix(Clock clock = Clock.systemUTC()) {
    ContractsBase.requireTrueBase(clock != null)

    return new ShortPrefixCombFactory(new SecureRandom(), clock).create()
  }

  /**
   * Derives COMB UUID short prefix as hexa string for the current time or for provided {@code clock} parameter.
   */
  static String deriveCombShortPrefixHexaString(Clock clock = Clock.systemUTC()) {
    ContractsBase.requireTrueBase(clock != null)

    Long currentTimeMinute = Instant.now(clock).toEpochMilli().intdiv(ONE_MINUTE_MILLIS) % THE_64_K
    return Long.toHexString(currentTimeMinute)
  }

  /**
   * Checks if provided UUID instance is time-bounded as expected.
   * <p/>
   * Method assumes that provided UUID is COMB UUID with short prefix. Otherwise it will return {@code false}.
   * <p/>
   * By default time-bounds are checked against the current time. Base time can be changed via {@code clock} parameter.
   * <p/>
   * By default, time bounds are 10 minutes in the past and 10 minutes in the future. This can be changed via {@code inPastMinutesBound} and {@code inFutureMinutesBound} parameters.
   * <p/>
   * Method will return {@code false} if provided {@code uuid} instance is {@code null} or if it is not random UUID (version 4, variant 2).
   *
   * @see #checkIfCombShortPrefixStringIsBounded(java.lang.String)
   */
  static boolean checkIfCombShortPrefixIsBounded(
      UUID uuid, Clock clock = Clock.systemUTC(), Integer inPastMinutesBound = IN_PAST_MINUTES_BOUND_DEFAULT, Integer inFutureMinutesBound = IN_FUTURE_MINUTES_BOUND_DEFAULT)
  {
    ContractsBase.requireTrueBase(clock != null)
    ContractsBase.requireTrueBase(inPastMinutesBound !=  null)
    ContractsBase.requireTrueBase(inPastMinutesBound >= 0)
    ContractsBase.requireTrueBase(inFutureMinutesBound != null)
    ContractsBase.requireTrueBase(inFutureMinutesBound >= 0)

    if (!uuid) {
      return false
    }

    if (!RandomUuidUtils.checkIfRandomUuid(uuid)) {
      return false
    }

    long boundedMinuteToCheck = (uuid.mostSignificantBits & 0xffff000000000000L) >>> 48
    long currentTimeMinute = Instant.now(clock).toEpochMilli().intdiv(ONE_MINUTE_MILLIS)
    long leftBound = (currentTimeMinute - inPastMinutesBound) % THE_64_K
    long rightBound = (currentTimeMinute + inFutureMinutesBound) % THE_64_K

    boolean isValid
    // For allowed bounded minute ranges without wrapping, i.e. [7504, 7505, 7506, 7507, 7508, 7509, 7510, 7511, 7512, 7513, 7514, 7515, 7516, 7517, 7518, 7519, 7520, 7521, 7522, 7523, 7524]
    // In this example current time bounded minute in in the center of array, left bound is first element and right bound is the last.
    if (leftBound <= rightBound) {
      isValid = (boundedMinuteToCheck >= leftBound) && (boundedMinuteToCheck <= rightBound)
    }
    // For allowed bounded minute ranges with wrapping, i.e. [65522, 65523, 65524, 65525, 65526, 65527, 65528, 65529, 65530, 65531, 65532, 65533, 65534, 65535, 0, 1, 2, 3, 4, 5, 6]
    // In this example current time bounded minute in in the center of array, left bound is first element and right bound is the last.
    // If it helps to make boolean logic below clearer, do note that (0 <= boundedMinuteToCheck <= (THE_64_K - 1)) is always satisfied (this is true for both branches of the "if" block).
    else {
      isValid = (boundedMinuteToCheck >= leftBound) || (boundedMinuteToCheck <= rightBound)
    }

    return isValid
  }

  /**
   * Checks if provided UUID string is time-bounded as expected.
   * <p/>
   * Method will return {@code false} for {@code null} and empty strings, and for any other input that cannot be parsed with {@code UUID.fromString(String)}.
   * <p/>
   * All other parameters are handled in the same was as in {@link #checkIfCombShortPrefixIsBounded(java.util.UUID)}.
   *
   * @see #checkIfCombShortPrefixIsBounded(java.util.UUID)
   */
  @SuppressWarnings("CodeNarc.CatchException")
  static boolean checkIfCombShortPrefixStringIsBounded(
      String uuidStringToCheck, Clock clock = Clock.systemUTC(), Integer inPastMinutesBound = IN_PAST_MINUTES_BOUND_DEFAULT, Integer inFutureMinutesBound = IN_FUTURE_MINUTES_BOUND_DEFAULT)
  {
    if (!uuidStringToCheck?.trim()) {
      return false
    }

    UUID uuid
    try {
      uuid = UUID.fromString(uuidStringToCheck)
    }
    catch (Exception ignore) {
      return false
    }

    return checkIfCombShortPrefixIsBounded(uuid, clock, inPastMinutesBound, inFutureMinutesBound)
  }
}
