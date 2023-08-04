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
package org.klokwrk.cargotracking.lib.boundary.api.application.metadata.response

import groovy.transform.CompileStatic

/**
 * Part of the response metadata that describes violation when request processing ends up in some error.
 * <p/>
 * For successful responses, it should not be rendered.
 * <p/>
 * An example of metadata containing violation part rendered for domain failure can be seen in the following example:
 * <pre>
 * {
 *     "metaData": {
 *         "general": {
 *             "severity": "warning",
 *             "locale": "en_GB",
 *             "timestamp": "2020-04-30T16:03:48.795816Z"
 *         },
 *         "violation": {
 *             "code": "400",
 *             "message": "Destination location cannot accept cargo from specified origin location.",
 *             "type": "domain"
 *         }
 *     },
 *     "payload": {}
 * }
 * </pre>
 */
@CompileStatic
class ResponseMetaDataViolationPart {
  /**
   * The primary code describing the main category of the violation or error.
   * <p/>
   * In general, it does not have to be designed to be human-readable, but rather it should be in the form of some violation/error identifier. For example, the categorization of HTTP response
   * statuses (200, 400, 404, 500, etc.), or database error code categorizations, are good examples of the kind of information that should go in here.
   */
  String code

  /**
   * A localized human-readable message describing the code property.
   */
  String message

  /**
   * Type of violation as a lowercase string corresponding to the values of {@link ViolationType} enum.
   * <p/>
   * This information may be valuable for the client when deciding how to handle the violation. Violations of different types may add additional data in the violation part.
   */
  String type

  /**
   * Violation identifying UUID contained in the message of logged exception.
   * <p/>
   * We cannot recover from some violations. A typical example is a {@code NullPointerException} raised during the execution of server side code. The usual method for handling such exceptions is
   * to log them on the server and report them as generic exceptions to the client (without passing the stack trace). However, to enable the client to report the issue, there is a need to reference
   * the concrete exception somehow. This {@code logUuid} is that kind of reference. The client can use it while reporting the problem, and maintainers can then correlate it with the stack trace in
   * the log.
   * <p/>
   * Property {@code logUuid} should occur in the response only if the exception stack trace is written in the log on the server-side. This should always happen for violations with {@code error}
   * severity. For other severities, it may or may not occur, and usually will not.
   */
  String logUuid

  /**
   * Violation part containing detailed information about validation failures.
   * <p/>
   * This part is rendered only when violation failure occurs. It is not rendered for any other failure type (domain, other or unknown).
   *
   * @see ResponseMetaDataValidationReportPart
   */
  ResponseMetaDataValidationReportPart validationReport
}
