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
