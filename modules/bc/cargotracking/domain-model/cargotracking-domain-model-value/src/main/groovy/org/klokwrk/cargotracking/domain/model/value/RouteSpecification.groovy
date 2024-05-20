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
import org.klokwrk.lib.xlang.groovy.base.misc.InstantUtils
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkImmutable

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

import static org.hamcrest.Matchers.notNullValue

/**
 * Represents route specification.
 * <p/>
 * Contains data which is core input for searching suitable and available routes for a cargo.
 */
@KwrkImmutable
@CompileStatic
class RouteSpecification implements PostMapConstructorCheckable {
  /**
   * Location from which the cargo should be picked up.
   */
  Location originLocation

  /**
   * Location to which cargo should be shipped.
   */
  Location destinationLocation

  /**
   * Time of creation of this instance.
   * <p/>
   * During construction it is used as an equivalent of current system time for time based calculations.
   */
  Instant creationTime

  /**
   * The earliest time when cargo can be departed.
   * <p/>
   * This instant should be rounded to the hour (minutes, seconds and nanos set to 0).
   * <p/>
   * This instant indicates the earliest time in which customer can prepare a cargo ready to be shipped.
   */
  Instant departureEarliestTime

  /**
   * The latest time when cargo has to be departed.
   * <p/>
   * This instant should be rounded to the hour (minutes, seconds and nanos set to 0).
   * <p/>
   * This time indicates the latest time when cargo should be shipped or otherwise packaged commodities can go bad.
   */
  Instant departureLatestTime

  /**
   * The latest time when cargo has to arrive at the destination.
   * <p/>
   * This instant should be rounded to the hour (minutes, seconds and nanos set to 0).
   */
  Instant arrivalLatestTime

  /**
   * Creates RouteSpecification instance and adjust input values as necessary.
   * <p/>
   * If {@code departureEarliestTime} contains non-zero minutes, seconds or nanos, {@code departureEarliestTime} is rounded up to the next hour.
   * <p/>
   * If {@code departureLatestTime} contains non-zero minutes, seconds or nanos, {@code departureLatestTime} is rounded up to the next hour.
   */
  @SuppressWarnings("CodeNarc.ParameterCount")
  static RouteSpecification make(
      Location originLocation, Location destinationLocation, Instant departureEarliestTime, Instant departureLatestTime, Instant arrivalLatestTime, Clock creationTimeClock = Clock.systemUTC())
  {
    Instant departureEarliestTimeToUse = InstantUtils.roundUpInstantToTheHour(departureEarliestTime)
    Instant departureLatestTimeToUse = InstantUtils.roundUpInstantToTheHour(departureLatestTime)
    Instant arrivalLatestTimeToUse = InstantUtils.roundUpInstantToTheHour(arrivalLatestTime)

    RouteSpecification newRouteSpecification = new RouteSpecification(
        originLocation: originLocation, destinationLocation: destinationLocation, creationTime: Instant.now(creationTimeClock),
        departureEarliestTime: departureEarliestTimeToUse, departureLatestTime: departureLatestTimeToUse, arrivalLatestTime: arrivalLatestTimeToUse
    )

    return newRouteSpecification
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    // Here we are comply to the validation ordering as explained in ADR-0013.
    requireMatch(originLocation, notNullValue())
    requireMatch(destinationLocation, notNullValue())

    requireMatch(creationTime, notNullValue())
    requireMatch(departureEarliestTime, notNullValue())
    requireMatch(departureLatestTime, notNullValue())
    requireMatch(arrivalLatestTime, notNullValue())

    requireKnownLocation(originLocation, "routeSpecification.unknownOriginLocation")
    requireKnownLocation(destinationLocation, "routeSpecification.unknownDestinationLocation")
    requireDifferentOriginAndDestination(originLocation, destinationLocation)
    requireCanRouteCargoFromOriginToDestination(originLocation, destinationLocation)

    requireInstantInFuture(departureEarliestTime, creationTime, "routeSpecification.departureEarliestTime.notInFuture")
    requireInstantInFuture(departureLatestTime, creationTime, "routeSpecification.departureLatestTime.notInFuture")
    requireDepartureEarliestTimeBeforeOrEqualToDepartureLatestTime(departureEarliestTime, departureLatestTime)

    requireInstantInHours(departureEarliestTime, "routeSpecification.departureEarliestTime.notInHours")
    requireInstantInHours(departureLatestTime, "routeSpecification.departureLatestTime.notInHours")

    requireInstantInFuture(arrivalLatestTime, creationTime, "routeSpecification.arrivalLatestTime.notInFuture")
    requireInstantInHours(arrivalLatestTime, "routeSpecification.arrivalLatestTime.notInHours")
    requireArrivalLatestTimeAfterDepartureLatestTime(arrivalLatestTime, departureLatestTime)
  }

  private void requireKnownLocation(Location location, String violationCodeKey) {
    if (location == Location.UNKNOWN_LOCATION) {
      throw new DomainException(ViolationInfo.makeForBadRequestWithCustomCodeKey(violationCodeKey))
    }
  }

  private void requireDifferentOriginAndDestination(Location originLocation, Location destinationLocation) {
    if (originLocation == destinationLocation) {
      throw new DomainException(ViolationInfo.makeForBadRequestWithCustomCodeKey("routeSpecification.originAndDestinationLocationAreEqual"))
    }
  }

  private void requireCanRouteCargoFromOriginToDestination(Location originLocation, Location destinationLocation) {
    if (!(originLocation.portCapabilities.isSeaContainerPort() && destinationLocation.portCapabilities.isSeaContainerPort())) {
      throw new DomainException(ViolationInfo.makeForBadRequestWithCustomCodeKey("routeSpecification.cannotRouteCargoFromOriginToDestination"))
    }
  }

  private void requireInstantInFuture(Instant instantToCheck, Instant currentTimeInstant, String violationCodeKey) {
    if (currentTimeInstant >= instantToCheck) {
      throw new DomainException(ViolationInfo.makeForBadRequestWithCustomCodeKey(violationCodeKey))
    }
  }

  private void requireDepartureEarliestTimeBeforeOrEqualToDepartureLatestTime(Instant departureEarliestTime, Instant departureLatestTime) {
    if (departureEarliestTime > departureLatestTime) {
      throw new DomainException(ViolationInfo.makeForBadRequestWithCustomCodeKey("routeSpecification.departureEarliestTime.afterDepartureLatestTime"))
    }
  }

  private void requireInstantInHours(Instant instantToCheck, String violationCodeKey) {
    Integer nanoSeconds = LocalDateTime.ofInstant(instantToCheck, ZoneOffset.UTC).nano
    Integer seconds = LocalDateTime.ofInstant(instantToCheck, ZoneOffset.UTC).second
    Integer minutes = LocalDateTime.ofInstant(instantToCheck, ZoneOffset.UTC).minute

    if (nanoSeconds + seconds + minutes != 0) {
      throw new DomainException(ViolationInfo.makeForBadRequestWithCustomCodeKey(violationCodeKey))
    }
  }

  private void requireArrivalLatestTimeAfterDepartureLatestTime(Instant arrivalLatestTime, Instant departureLatestTime) {
    if (arrivalLatestTime <= departureLatestTime) {
      throw new DomainException(ViolationInfo.makeForBadRequestWithCustomCodeKey("routeSpecification.arrivalLatestTime.beforeDepartureLatestTime"))
    }
  }
}
