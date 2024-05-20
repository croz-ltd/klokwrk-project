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
import org.klokwrk.lib.xlang.groovy.base.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkImmutable

import java.math.RoundingMode
import java.util.regex.Pattern

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.hasLength
import static org.hamcrest.Matchers.matchesPattern
import static org.hamcrest.Matchers.not

/**
 * Represents an 12-character encoding of coordinates for the UN/LOCODE location.
 * <p/>
 * Useful reference: https://service.unece.org/trade/locode/Service/LocodeColumn.htm - Section: 1.10 Column "Coordinates"
 */
@KwrkImmutable
@CompileStatic
class UnLoCodeCoordinates implements PostMapConstructorCheckable {
  static final Pattern COORDINATES_PATTERN = Pattern.compile(/^[0-9]{4}[NS] [0-9]{5}[WE]$/)
  static final UnLoCodeCoordinates UNKNOWN_UN_LO_CODE_COORDINATES = new UnLoCodeCoordinates(coordinatesEncoded: "9999N 99999W")

  private static final Integer COORDINATES_ENCODED_STRING_LENGTH = 12
  private static final Integer MINUTE_MAX = 60
  private static final Integer LATITUDE_DEGREE_MAX = 90
  private static final Integer LONGITUDE_DEGREE_MAX = 180

  String coordinatesEncoded

  private final transient BigDecimal[] internalCoordinatesCache = new BigDecimal[] { null, null }

  /**
   * Checks provided {@code unLoCodeCoordinates} for value validity and consistency.
   * <p/>
   * Method assumes that provided {@code unLoCodeCoordinates} satisfies {@link UnLoCodeCoordinates#COORDINATES_PATTERN} regex.
   */
  static void checkCoordinatesValues(String unLoCodeCoordinates) {
    checkLatitudeValues(extractUnLoCodeCoordinatesLatitude(unLoCodeCoordinates))
    checkLongitudeValues(extractUnLoCodeCoordinatesLongitude(unLoCodeCoordinates))
  }

  /**
   * Checks provided latitude part of unLoCodeCoordinates for value validity and consistency.
   * <p/>
   * Method assumes that provided {@code unLoCodeCoordinatesLatitudePart} satisfies the first part of {@link UnLoCodeCoordinates#COORDINATES_PATTERN} regex corresponding to the latitude (before
   * space).
   */
  static void checkLatitudeValues(String unLoCodeCoordinatesLatitudePart) {
    Integer latitudeMinutes = Integer.parseInt(unLoCodeCoordinatesLatitudePart[2..3])
    if (latitudeMinutes >= MINUTE_MAX) {
      throw new AssertionError("Latitude minutes cannot be greater than or equal to $MINUTE_MAX" as Object)
    }

    Integer latitudeDegrees = Integer.parseInt(unLoCodeCoordinatesLatitudePart[0..1])
    if ((latitudeDegrees * MINUTE_MAX + latitudeMinutes) > LATITUDE_DEGREE_MAX * MINUTE_MAX) {
      throw new AssertionError("Latitude degrees cannot be greater than $LATITUDE_DEGREE_MAX" as Object)
    }
  }

  /**
   * Extracts latitude part from provided {@code unLoCodeCoordinates}.
   * <p/>
   * Method assumes that provided {@code unLoCodeCoordinates} satisfies {@link UnLoCodeCoordinates#COORDINATES_PATTERN} regex.
   */
  static String extractUnLoCodeCoordinatesLatitude(String unLoCodeCoordinates) {
    return unLoCodeCoordinates[0..4]
  }

  /**
   * Checks provided longitude part of unLoCodeCoordinates for value validity and consistency.
   * <p/>
   * Method assumes that provided {@code unLoCodeCoordinatesLongitudePart} satisfies the second part of {@link UnLoCodeCoordinates#COORDINATES_PATTERN} regex corresponding to the longitude (after
   * space).
   */
  static void checkLongitudeValues(String unLoCodeCoordinatesLongitudePart) {
    Integer longitudeMinutes = Integer.parseInt(unLoCodeCoordinatesLongitudePart[3..4])
    if (longitudeMinutes >= MINUTE_MAX) {
      throw new AssertionError("Longitude minutes cannot be greater than or equal to $MINUTE_MAX" as Object)
    }

    Integer longitudeDegrees = Integer.parseInt(unLoCodeCoordinatesLongitudePart[0..2])
    if (longitudeDegrees * MINUTE_MAX + longitudeMinutes > LONGITUDE_DEGREE_MAX * MINUTE_MAX) {
      throw new AssertionError("Longitude degrees cannot be greater than $LONGITUDE_DEGREE_MAX" as Object)
    }
  }

  /**
   * Extracts longitude part from provided {@code unLoCodeCoordinates}.
   * <p/>
   * Method assumes that provided {@code unLoCodeCoordinates} satisfies {@link UnLoCodeCoordinates#COORDINATES_PATTERN} regex.
   */
  static String extractUnLoCodeCoordinatesLongitude(String unLoCodeCoordinates) {
    return unLoCodeCoordinates[6..11]
  }

  /**
   * Calculates latitude in degrees from provided {@code unLoCodeCoordinates} string.
   * <p/>
   * Returned {@code BigDecimal} is rounded up at 2 decimals. That gives us a precision around 1 kilometer (see http://wiki.gis.com/wiki/index.php/Decimal_degrees) which is good enough knowing that
   * UnLoCode coordinates do not contain degree seconds.
   * <p/>
   * Method assumes that provided {@code unLoCodeCoordinates} satisfies {@link UnLoCodeCoordinates#COORDINATES_PATTERN} regex.
   */
  @SuppressWarnings("CodeNarc.DuplicateNumberLiteral")
  static BigDecimal calculateLatitudeDegrees(String unLoCodeCoordinates) {
    String latitudeString = extractUnLoCodeCoordinatesLatitude(unLoCodeCoordinates)
    Integer sign = latitudeString[-1] == "N" ? 1 : -1
    Integer degrees = Integer.parseInt(latitudeString[0..1])
    Integer minutes = Integer.parseInt(latitudeString[2..3])

    BigDecimal latitudeDegrees = (sign * (degrees + minutes / MINUTE_MAX)).setScale(2, RoundingMode.HALF_UP)
    return latitudeDegrees
  }

  /**
   * Calculates longitude in degrees from provided {@code unLoCodeCoordinates} string.
   * <p/>
   * Returned {@code BigDecimal} is rounded up at 2 decimals. That gives us a precision around 1 kilometer (see http://wiki.gis.com/wiki/index.php/Decimal_degrees) which is good enough knowing that
   * UnLoCode coordinates do not contain degree seconds.
   * <p/>
   * Method assumes that provided {@code unLoCodeCoordinates} satisfies {@link UnLoCodeCoordinates#COORDINATES_PATTERN} regex.
   */
  @SuppressWarnings("CodeNarc.DuplicateNumberLiteral")
  static BigDecimal calculateLongitudeDegrees(String unLoCodeCoordinates) {
    String longitudeString = extractUnLoCodeCoordinatesLongitude(unLoCodeCoordinates)
    Integer sign = longitudeString[-1] == "E" ? 1 : -1
    Integer degrees = Integer.parseInt(longitudeString[0..2])
    Integer minutes = Integer.parseInt(longitudeString[3..4])

    BigDecimal longitudeDegrees = (sign * (degrees + minutes / MINUTE_MAX)).setScale(2, RoundingMode.HALF_UP)
    return longitudeDegrees
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(coordinatesEncoded, not(blankOrNullString()))
    requireMatch(coordinatesEncoded, hasLength(COORDINATES_ENCODED_STRING_LENGTH))
    requireMatch(coordinatesEncoded, matchesPattern(COORDINATES_PATTERN))

    // Skip check only for UNKNOWN_UN_LO_CODE_COORDINATES instance
    if (UNKNOWN_UN_LO_CODE_COORDINATES != null) {
      checkCoordinatesValues(coordinatesEncoded)
    }
  }

  /**
   * Returns latitude in degrees.
   * <p/>
   * For {@link UnLoCodeCoordinates#UNKNOWN_UN_LO_CODE_COORDINATES} instance, returns {@code null}.
   */
  BigDecimal getLatitudeInDegrees() {
    if (this === UNKNOWN_UN_LO_CODE_COORDINATES) {
      return null
    }

    if (internalCoordinatesCache[0]) {
      return internalCoordinatesCache[0]
    }

    internalCoordinatesCache[0] = calculateLatitudeDegrees(coordinatesEncoded)
    return internalCoordinatesCache[0]
  }

  /**
   * Returns longitude in degrees.
   * <p/>
   * For {@link UnLoCodeCoordinates#UNKNOWN_UN_LO_CODE_COORDINATES} instance, returns {@code null}.
   */
  BigDecimal getLongitudeInDegrees() {
    if (this === UNKNOWN_UN_LO_CODE_COORDINATES) {
      return null
    }

    if (internalCoordinatesCache[1]) {
      return internalCoordinatesCache[1]
    }

    internalCoordinatesCache[1] = calculateLongitudeDegrees(coordinatesEncoded)
    return internalCoordinatesCache[1]
  }
}
