/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.lib.boundary.api.domain.violation

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.not

/**
 * Immutable data structure describing violation's code and the corresponding non-localized code's message.
 * <p/>
 * There is also <code>codeKey</code> property that is used for easier resolving of localized messages outside of the domain's boundary.
 * <p/>
 * All three members must be specified at construction time.
 */
@KwrkImmutable
@CompileStatic
class ViolationCode implements PostMapConstructorCheckable {
  static final ViolationCode UNKNOWN = new ViolationCode(code: "500", codeMessage: "Internal Server Error", resolvableMessageKey: "internalServerError")
  static final ViolationCode BAD_REQUEST = new ViolationCode(code: "400", codeMessage: "Bad Request", resolvableMessageKey: "badRequest")
  static final ViolationCode NOT_FOUND = new ViolationCode(code: "404", codeMessage: "Not Found", resolvableMessageKey: "notFound")

  /**
   * The primary code describing the main category of the violation.
   * <p/>
   * In general, it does not have to be designed to be human-readable, but rather it should be in the form of some primary violation/error identifier. For example, the categorization of HTTP response
   * statuses (200, 400, 404, 500, etc.), or database error code categorizations, are good examples of the kind of information that should go in here.
   */
  String code

  /**
   * A short hardcoded human-readable message written in English describing the problem identified by this {@code ViolationCode}.
   * <p/>
   * For example, in HTTP error handling, this message would correspond to the textual descriptions of status codes like "OK", "Internal Server Error", "Not Found", etc.
   */
  String codeMessage

  /**
   * A key for resolving an internationalized message corresponding to this {@code ViolationCode}, i.e., through the resource bundle.
   * <p/>
   * In theory, we could use {@code code} as a key for resolving internationalized messages. However, {@code code} can be very technical and often expressed as a stringified number, which makes it
   * hard to recognize for developers or other human translators.
   * <p/>
   * Therefore, {@code resolvableMessageKey} is a resolvable alias for {@code code} that is much more appealing to use for writing localized resource bundles at the inbound/outbound channel level.
   * <p/>
   * For example, when maintaining resource bundles, it should be much easier for a developer to deduct the meaning of
   * {@code bookingOfferWebController.bookingOfferSummaryQuery.failure.other.badRequest} resource bundle key instead the meaning of
   * {@code bookingOfferWebController.bookingOfferSummaryQuery.failure.other.400} key. And this is exactly the intention behind this property.
   * <p/>
   * If we need some further categorization inside {@code resolvableMessageKey}, it is recommended to use dot character for that purpose. For example, like in {@code notFound.personSummary}.
   */
  String resolvableMessageKey

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(code, not(blankOrNullString()))
    requireMatch(codeMessage, not(blankOrNullString()))
    requireMatch(resolvableMessageKey, not(blankOrNullString()))
  }
}
