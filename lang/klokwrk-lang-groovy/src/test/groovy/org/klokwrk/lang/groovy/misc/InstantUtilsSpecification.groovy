package org.klokwrk.lang.groovy.misc

import spock.lang.Specification

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class InstantUtilsSpecification extends Specification {
  void "roundUpInstantToTheHour - should throw for null input"() {
    when:
    InstantUtils.roundUpInstantToTheHour(null)

    then:
    thrown(AssertionError)
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
