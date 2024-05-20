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
package org.klokwrk.lib.xlang.groovy.base.misc

import spock.lang.Specification

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class InstantUtilsSpecification extends Specification {
  void "roundUpInstantToTheHour - should return null for null parameter"() {
    when:
    Instant roundedInstant = InstantUtils.roundUpInstantToTheHour(null)

    then:
    roundedInstant == null
  }

  void "roundUpInstantToTheHour - should not round up for already rounded instant"() {
    given:
    Clock clock = Clock.fixed(Instant.parse("2021-12-07T12:00:00Z"), ZoneOffset.UTC)

    when:
    Instant roundedInstant = InstantUtils.roundUpInstantToTheHour(Instant.now(clock))

    then:
    roundedInstant == Instant.now(clock)
  }

  void "roundUpInstantToTheHour - should round up instant"() {
    given:
    Clock clock = Clock.fixed(Instant.parse("2021-12-07T12:01:01Z"), ZoneOffset.UTC)

    when:
    Instant roundedInstant = InstantUtils.roundUpInstantToTheHour(Instant.now(clock))

    then:
    roundedInstant == Instant.now(clock).truncatedTo(ChronoUnit.HOURS) + Duration.ofHours(1)
  }
}
