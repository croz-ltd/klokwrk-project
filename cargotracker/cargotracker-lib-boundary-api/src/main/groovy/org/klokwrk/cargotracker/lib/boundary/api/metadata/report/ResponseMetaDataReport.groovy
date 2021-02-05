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
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity

import java.time.Instant

/**
 * Defines the basic structure of response metadata.
 * <p/>
 * It can be extended by input channel/interface with more data specific to the concrete channel. For example, take a look at <code>HttpResponseMetaDataReport</code>.
 * <p/>
 * All of the data presented here will probably be serialized to the end-user in the appropriate form. For example, when rendered as JSON, metadata can look something like the following example:
 * <pre>
 * {
 *     "metaData": {
 *         "severity": "INFO",
 *         "locale": "en_GB",
 *         "timestamp": "2020-04-30T15:37:24.999722Z",
 *     },
 *     "payload": {
 *       ...
 *     }
 * }
 * </pre>
 */
@CompileStatic
class ResponseMetaDataReport {
  /**
   * The UTC timestamp of the response.
   */
  Instant timestamp

  /**
   * Severity of the response.
   */
  Severity severity

  /**
   * Locale used for rendering response message.
   * <p/>
   * Usually, it corresponds to the locale of the current request.
   */
  Locale locale

  /**
   * Sub-structure that describes current violation when the processing of the request ends up in some error.
   * <p/>
   * For successful responses, the violation part should not be rendered.
   *
   * @see ResponseMetaDataReportViolationPart
   */
  ResponseMetaDataReportViolationPart violation

  /**
   * Creates basic ResponseMetaDataReport that contains only a timestamp and <code>INFO</code> severity.
   *
   * @see Severity
   */
  static ResponseMetaDataReport createBasicInfoMetaDataReport() {
    ResponseMetaDataReport responseMetadataReport = new ResponseMetaDataReport(timestamp: Instant.now(), severity: Severity.INFO)
    return responseMetadataReport
  }
}
