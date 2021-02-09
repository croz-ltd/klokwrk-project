/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.lib.boundary.api.metadata.response

import groovy.transform.CompileStatic

/**
 * Part of the response metadata that describes violation when request processing ends up in some error.
 * <p/>
 * For successful responses, it should not be rendered.
 * <p/>
 * An example of metadata containing violation part can be seen in the following example:
 * <pre>
 * {
 *     "metaData": {
 *         "general": {
 *             "severity": "WARNING",
 *             "locale": "en_GB",
 *             "timestamp": "2020-04-30T16:03:48.795816Z"
 *         },
 *         "violation": {
 *             "code": "400",
 *             "codeMessage": "Destination location cannot accept cargo from specified origin location.",
 *             "type": "DOMAIN"
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
   * </p>
   * In general, it does not have to be designed to be human-readable, but rather it should be in the form of some violation/error identifier. For example, the categorization of HTTP response
   * statuses (200, 400, 404, 500, etc.), or database error code categorizations, are good examples of the kind of information that should go in here.
   * <p/>
   * Usually, this code is originating from domain exceptions and only copied in this metadata response.
   */
  String code

  /**
   * A localized human-readable message describing the main category of the violation or error.
   */
  String codeMessage

  /**
   * Type of violation.
   * <p/>
   * This information may be valuable for the client when deciding how to handle violation. Violations of different types may add additional violation data.
   */
  ViolationType type

  /**
   * UUID contained in the log message of logged exception.
   * <p/>
   * We cannot recover from some violations. A typical example is a <code>NullPointerException</code> raised during the execution of server side code. The usual method for handling such exceptions is
   * to log them on the server and report them as generic exceptions to the client (without passing the stack trace). However, to enable the client to report the issue, there is a need to reference
   * the concrete exception. This logUuid is that kind of reference. The client can use it while reporting the problem, and maintainers can then correlate it with the stack trace in the log.
   * <p/>
   * Property <code>logUuid</code> should occur in the response only if the exception stack trace is written in the log on the server-side. This should always happen for violations with ERROR
   * severity. For other severities, it may or may not occur, and usually will not.
   */
  String logUuid
}
