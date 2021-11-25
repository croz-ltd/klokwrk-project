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
package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import static org.hamcrest.Matchers.notNullValue

/**
 * Represents a location data as specified by UN/LOCODE standard.
 * <p/>
 * Useful references:
 * <ul>
 *   <li>https://www.unece.org/cefact/codesfortrade/codes_index.html</li>
 *   <li>https://service.unece.org/trade/locode/Service/LocodeColumn.htm</li>
 *   <li>https://www.unece.org/cefact/locode/service/location</li>
 *   <li>https://service.unece.org/trade/locode/hr.htm</li>
 *   <li>http://tfig.unece.org/contents/recommendation-16.htm</li>
 * </ul>
 */
@KwrkImmutable
@CompileStatic
class Location implements PostMapConstructorCheckable {

  static final Location UNKNOWN_LOCATION = new Location(
      unLoCode: UnLoCode.UNKNOWN_UN_LO_CODE, name: InternationalizedName.UNKNOWN_INTERNATIONALIZED_NAME, countryName: InternationalizedName.UNKNOWN_INTERNATIONALIZED_NAME,
      unLoCodeFunction: UnLoCodeFunction.UNKNOWN_UN_LO_CODE_FUNCTION, unLoCodeCoordinates: UnLoCodeCoordinates.UNKNOWN_UN_LO_CODE_COORDINATES
  )

  UnLoCode unLoCode

  /**
   * Location name expressed in their natural language if possible.
   * <p/>
   * Original UnLoCode spec defines character set as: "Roman alphabet using the 26 characters of the character set adopted for international trade data interchange, with diacritic signs, when
   * practicable". However, we are using UTF-8 so all location names can be fully specified in their natural language.
   */
  InternationalizedName name

  /**
   * The name of the country to which this location belongs.
   */
  InternationalizedName countryName

  /**
   * 8-character function classifier code for the UN/LOCODE location.
   */
  UnLoCodeFunction unLoCodeFunction

  /**
   * 12-character coordinates string for the UN/LOCODE location.
   */
  UnLoCodeCoordinates unLoCodeCoordinates

  static Location create(String unLoCode, String name, String countryName, String unLoCodeFunction, String unLoCodeCoordinates) {
    Location createdLocation = new Location(
        unLoCode: new UnLoCode(code: unLoCode), name: new InternationalizedName(name: name), countryName: new InternationalizedName(name: countryName),
        unLoCodeFunction: new UnLoCodeFunction(functionEncoded: unLoCodeFunction), unLoCodeCoordinates: new UnLoCodeCoordinates(coordinatesEncoded: unLoCodeCoordinates)
    )

    return createdLocation
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    // Here we are comply to the validation ordering as explained in ADR-0013.
    requireMatch(unLoCode, notNullValue())
    requireMatch(name, notNullValue())
    requireMatch(countryName, notNullValue())
    requireMatch(unLoCodeFunction, notNullValue())
    requireMatch(unLoCodeCoordinates, notNullValue())
  }
}
