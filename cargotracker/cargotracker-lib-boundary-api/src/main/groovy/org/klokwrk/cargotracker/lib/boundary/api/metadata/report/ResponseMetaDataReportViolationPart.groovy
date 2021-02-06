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
package org.klokwrk.cargotracker.lib.boundary.api.metadata.report

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
 *             "codeMessage": "Destination location cannot accept cargo from specified origin location."
 *         }
 *     },
 *     "payload": {}
 * }
 * </pre>
 */
@CompileStatic
class ResponseMetaDataReportViolationPart {
  /**
   * The primary code describing the main category of the violation or error.
   * </p>
   * In general, it does not have to be designed to be human-readable, but rather it should be in the form of some violation/error identifier. For example, the categorization of HTTP response
   * statuses (200, 400, 404, 500, etc.), or database error code categorizations, are good examples of the kind of information that should go in here.
   * <p/>
   * Usually, this code is originating from domain exceptions and only copied in this metadata report.
   */
  String code

  /**
   * A localized human-readable message describing the main category of the violation or error.
   */
  String codeMessage
}
