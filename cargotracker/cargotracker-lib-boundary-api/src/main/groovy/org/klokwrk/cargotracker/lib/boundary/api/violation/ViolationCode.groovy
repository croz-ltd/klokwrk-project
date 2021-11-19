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
package org.klokwrk.cargotracker.lib.boundary.api.violation

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
  static final ViolationCode UNKNOWN = new ViolationCode(code: "500", codeKey: "internalServerError", codeMessage: "Internal Server Error")
  static final ViolationCode BAD_REQUEST = new ViolationCode(code: "400", codeKey: "badRequest", codeMessage: "Bad Request")
  static final ViolationCode NOT_FOUND = new ViolationCode(code: "404", codeKey: "notFound", codeMessage: "Not Found")

  /**
   * The primary code describing the main category of the violation.
   * <p/>
   * In general, it does not have to be designed to be human-readable, but rather it should be in the form of some primary violation/error identifier. For example, the categorization of HTTP response
   * statuses (200, 400, 404, 500, etc.), or database error code categorizations, are good examples of the kind of information that should go in here.
   */
  String code

  /**
   * More human-readable alias for <code>code</code> property.
   * <p/>
   * In this context, human-readable does not mean full sentences but rather some textual encoded value that is easily recognizable by developers. The intention is that <code>codeKey</code> is
   * used as an alias of primary code property that is more appealing for writing localized resource bundles at the inbound channel level. For example, when maintaining resource bundles, it should be
   * easier for developers to deduct the meaning of <code>cargoInfoWebController.cargoSummaryQuery.failure.other.badRequest</code> resource bundle key instead the meaning of
   * <code>cargoInfoWebController.cargoSummaryQuery.failure.other.400</code> key. And this is exactly the intention behind this property.
   * <p/>
   * If we need some kind of categorization inside <code>codeKey</code>, it is recommended to use dot character. For example, <code>notFound.personSummary</code>.
   */
  String codeKey

  /**
   * A short human-readable message written in English describing the problem identified by primary code.
   * <p/>
   * For example, in HTTP error handling, this message would correspond to the textual descriptions of status codes like "OK", "Internal Server Error", "Not Found", etc.
   */
  String codeMessage

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(code, not(blankOrNullString()))
    requireMatch(codeKey, not(blankOrNullString()))
    requireMatch(codeMessage, not(blankOrNullString()))
  }
}
