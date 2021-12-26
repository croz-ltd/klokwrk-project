/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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

import groovy.transform.CompileStatic

import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@CompileStatic
class InstantUtils {
  /**
   * For instants with minutes, seconds or nanos different from 0, rounds up an instant to the next hour. Otherwise returns the same instant.
   * <p/>
   * For {@code null} parameter, returns {@code null}.
   */
  static Instant roundUpInstantToTheHour(Instant instantToRoundUp) {
    if (instantToRoundUp == null) {
      return instantToRoundUp
    }

    Instant truncatedToTheHour = instantToRoundUp.truncatedTo(ChronoUnit.HOURS)
    if (truncatedToTheHour.isBefore(instantToRoundUp)) {
      return truncatedToTheHour + Duration.ofHours(1)
    }

    return instantToRoundUp
  }
}
