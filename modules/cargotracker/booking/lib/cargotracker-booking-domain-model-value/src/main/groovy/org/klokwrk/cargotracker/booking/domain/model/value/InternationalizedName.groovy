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

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import java.text.Normalizer
import java.util.regex.Pattern

import static org.hamcrest.Matchers.allOf
import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.greaterThanOrEqualTo
import static org.hamcrest.Matchers.lessThanOrEqualTo
import static org.hamcrest.Matchers.not

/**
 * Represents an Unicode name capable to produce internationalized name.
 * <p/>
 * Internationalized name is produced simply by replacing diacritic characters with their non-diacritic Unicode counterparts. For the majority of diacritic characters, their non-diacritic counterpart
 * is encoded in Unicode itself. Therefore, such Unicode diacritic characters can be replaced by simple regex matching after decomposing them into characters canonical form containing separate codes
 * for base character and diacritic. In general this can be accomplished with following code fragment:
 * <pre>
 *   String nonDiacriticName = Normalizer.normalize(originalName, Normalizer.Form.NFD).replaceAll(DIACRITIC_MATCHING_PATTERN, "")
 * </pre>
 * Unfortunatelly, there are some diacritic characters that do not have separate code for diacritic. One example is "LATIN SMALL/CAPITAL LETTER D WITH STROKE" (<code>đ/Đ</code>). For these diacritic
 * characters additional custom replacement is needed as is implemented in {@link InternationalizedName#getNameInternationalized()} method.
 * <p/>
 * Some useful references:
 * <ul>
 *   <li>https://web.archive.org/web/20070917051642/http://java.sun.com/mailers/techtips/corejava/2007/tt0207.html#1</li>
 *   <li>https://docs.oracle.com/javase/8/docs/api/java/text/Normalizer.html</li>
 *   <li>https://web.archive.org/web/20200329072305/https://www.unicode.org/reports/tr44/#Properties</li>
 *   <li>https://memorynotfound.com/remove-accents-diacritics-from-string</li>
 * </ul>
 */
@KwrkImmutable
@CompileStatic
class InternationalizedName implements PostMapConstructorCheckable {
  static final InternationalizedName UNKNOWN_INTERNATIONALIZED_NAME = new InternationalizedName(name: "UNKNOWN")

  private static final Pattern DIACRITIC_MATCHING_PATTERN = Pattern.compile(/[\p{InCombiningDiacriticalMarks}]+/)

  @SuppressWarnings("CodeNarc.UnnecessaryCast")
  private static final Map<CharSequence, CharSequence> ADDITIONAL_REPLACEMENTS_MAP = [
      đ: "d",
      Đ: "D"
  ] as Map<CharSequence, CharSequence>

  String name

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    // Here we are comply to the validation ordering as explained in ADR-0013.
    requireMatch(name, not(blankOrNullString()))
    requireMatch(name.size(), allOf(greaterThanOrEqualTo(1), lessThanOrEqualTo(200)))
  }

  String getNameInternationalized() {
    return Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll(DIACRITIC_MATCHING_PATTERN, "").replace(ADDITIONAL_REPLACEMENTS_MAP)
  }
}
