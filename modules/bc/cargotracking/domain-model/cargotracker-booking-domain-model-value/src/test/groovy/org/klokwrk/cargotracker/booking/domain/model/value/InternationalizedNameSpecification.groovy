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
package org.klokwrk.cargotracker.booking.domain.model.value

import spock.lang.Shared
import spock.lang.Specification

class InternationalizedNameSpecification extends Specification {

  @SuppressWarnings("SpellCheckingInspection")
  @Shared
  String funkyString = "Tĥïŝ ĩš â fůňķŷ Šťŕĭńġ"

  @SuppressWarnings("SpellCheckingInspection")
  @Shared
  String funkyStringInternationalized = "This is a funky String"

  void "map constructor should work for correct input params"() {
    when:
    InternationalizedName internationalizedName = new InternationalizedName(name: nameParameter)

    then:
    internationalizedName.name == nameParameter

    where:
    nameParameter | _
    "a"           | _
    "Zagreb"      | _
    "Baška"       | _
    "Baška"       | _
    funkyString   | _
  }

  void "map constructor should fail for invalid input params"() {
    when:
    new InternationalizedName(name: nameParameter)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(errorMessagePartParam)

    where:
    nameParameter | errorMessagePartParam
    null          | "not(blankOrNullString())"
    ""            | "not(blankOrNullString())"
    "   "         | "not(blankOrNullString())"
    "A" * 300     | "allOf(greaterThanOrEqualTo(1), lessThanOrEqualTo(200))"
  }

  void "getNameInternationalized() should return expected value"() {
    when:
    InternationalizedName internationalizedName = new InternationalizedName(name: nameParameter)

    then:
    internationalizedName.nameInternationalized == nameInternationalizedParameter

    where:
    nameParameter | nameInternationalizedParameter
    "a"           | "a"
    "Zagreb"      | "Zagreb"
    "Baška"       | "Baska"
    //noinspection SpellCheckingInspection
    "čćžšđČĆŽŠĐ"  | "cczsdCCZSD"
    funkyString   | funkyStringInternationalized
  }
}
