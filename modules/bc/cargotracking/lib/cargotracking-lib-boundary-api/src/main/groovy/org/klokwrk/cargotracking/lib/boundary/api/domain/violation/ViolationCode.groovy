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
package org.klokwrk.cargotracking.lib.boundary.api.domain.violation

import groovy.transform.CompileStatic
import org.klokwrk.lib.xlang.groovy.base.constant.CommonConstants
import org.klokwrk.lib.xlang.groovy.base.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkImmutable

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue

/**
 * Immutable data structure describing violation's code and the corresponding non-localized code's message.
 * <p/>
 * There are also optional {@code resolvableMessageKey} and {@code resolvableMessageParameters} properties used for easier resolving of localized messages outside the domain's boundary.
 * <p/>
 * When resolving {@code DomainException} for display rendering, the corresponding rendered should honor the intended overriding order between {@code ViolationCode.codeMessage}, domain exception
 * message, and {@code ViolationCode.resolvableMessageKey}. For example:
 * <ul>
 * <li>If {@code ViolationCode.resolvableMessageKey} is available, the exception renderer should use is for resolving a message through resource bundle.</li>
 * <li>Otherwise, if {@code DomainException} message exists and is not blank string, the exception renderer should use it directly.</li>
 * <li>Otherwise, the exception renderer should use {@code ViolationCode.codeMessage} directly.</li>
 * </ul>
 */
@KwrkImmutable
@CompileStatic
class ViolationCode implements PostMapConstructorCheckable {
  static final String RESOLVABLE_MESSAGE_KEY_UNAVAILABLE = CommonConstants.NOT_AVAILABLE

  static final String UNKNOWN_CODE = "500"
  static final String UNKNOWN_CODE_MESSAGE = "Internal Server Error"
  static final String UNKNOWN_RESOLVABLE_MESSAGE_KEY = "internalServerError"
  static final ViolationCode UNKNOWN = new ViolationCode(code: UNKNOWN_CODE, codeMessage: UNKNOWN_CODE_MESSAGE, resolvableMessageKey: UNKNOWN_RESOLVABLE_MESSAGE_KEY, resolvableMessageParameters: [])

  static final String BAD_REQUEST_CODE = "400"
  static final String BAD_REQUEST_CODE_MESSAGE = "Bad Request"
  static final String BAD_REQUEST_RESOLVABLE_MESSAGE_KEY = "badRequest"
  static final ViolationCode BAD_REQUEST = new ViolationCode(
      code: BAD_REQUEST_CODE, codeMessage: BAD_REQUEST_CODE_MESSAGE, resolvableMessageKey: BAD_REQUEST_RESOLVABLE_MESSAGE_KEY, resolvableMessageParameters: []
  )

  static final String NOT_FOUND_CODE = "404"
  static final String NOT_FOUND_CODE_MESSAGE = "Not Found"
  static final String NOT_FOUND_RESOLVABLE_MESSAGE_KEY = "notFound"
  static final ViolationCode NOT_FOUND = new ViolationCode(
      code: NOT_FOUND_CODE, codeMessage: NOT_FOUND_CODE_MESSAGE, resolvableMessageKey: NOT_FOUND_RESOLVABLE_MESSAGE_KEY, resolvableMessageParameters: []
  )

  /**
   * The primary code describing the main category of the violation.
   * <p/>
   * In general, it does not have to be designed to be human-readable, but rather it should be in the form of some primary violation/error identifier. For example, the categorization of HTTP response
   * statuses (200, 400, 404, 500, etc.), or database error code categorizations, are good examples of the kind of information that should go in here.
   * <p/>
   * Since the authors find HTTP error codes quite a good high-level categorization of errors, we are using them in klokwrk. However, any other custom categorization can be employed instead.
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
   * {@code bookingOfferQueryWebController.bookingOfferSummaryQuery.failure.other.badRequest} resource bundle key instead the meaning of
   * {@code bookingOfferQueryWebController.bookingOfferSummaryQuery.failure.other.400} key. And this is exactly the intention behind this property.
   * <p/>
   * If we need some further categorization inside {@code resolvableMessageKey}, it is recommended to use dot character for that purpose. For example, like in {@code notFound.personSummary}.
   */
  String resolvableMessageKey = RESOLVABLE_MESSAGE_KEY_UNAVAILABLE

  /**
   * A list of strings containing parameters for resolving internationalized message corresponding to this {@code ViolationCode}.
   */
  List<String> resolvableMessageParameters = []

  /**
   * The factory method for creating {@code ViolationCode} instances.
   * <p/>
   * Parameters {@code resolvableMessageKey} and {@code resolvableMessageParameters} are optional. If not specified, they are resolved to {@code RESOLVABLE_MESSAGE_KEY_UNAVAILABLE} constant and empty
   * list, respectively.
   */
  static ViolationCode make(String code, String codeMessage, String resolvableMessageKey = RESOLVABLE_MESSAGE_KEY_UNAVAILABLE, List<String> resolvableMessageParameters = []) {
    ViolationCode violationCode = new ViolationCode(code: code, codeMessage: codeMessage, resolvableMessageKey: resolvableMessageKey, resolvableMessageParameters: resolvableMessageParameters)
    return violationCode
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(code, not(blankOrNullString()))
    requireMatch(codeMessage, not(blankOrNullString()))
    requireMatch(resolvableMessageKey, not(blankOrNullString()))
    requireMatch(resolvableMessageParameters, notNullValue())
  }

  /**
   * Whether or not this violation code contains a resolvable message key.
   */
  boolean isResolvable() {
    return RESOLVABLE_MESSAGE_KEY_UNAVAILABLE != resolvableMessageKey
  }
}
