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
package org.klokwrk.cargotracker.lib.boundary.api.application.metadata.response

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.domain.severity.Severity

import java.time.Instant

/**
 * Defines the basic structure of response metadata.
 * <p/>
 * It can be extended by input channel/interface with more data specific to the concrete channel. For example, take a look at <code>HttpResponseMetaData</code>.
 * <p/>
 * All of the data presented here will probably be serialized to the end-user in the appropriate form. For example, when rendered as JSON, metadata can look something like the following example:
 * <pre>
 * {
 *     "metaData": {
 *         "general": {
 *             "severity": "info",
 *             "locale": "en_GB",
 *             "timestamp": "2020-04-30T15:37:24.999722Z"
 *         }
 *     },
 *     "payload": {
 *       ...
 *     }
 * }
 * </pre>
 */
@CompileStatic
class ResponseMetaData {
  /**
   * Sub-structure containing general parts of the response.
   * <p/>
   * Present and rendered for all kind of responses, successful and erroneous.
   *
   * @see ResponseMetaDataGeneralPart
   */
  ResponseMetaDataGeneralPart general

  /**
   * Sub-structure that describes current violation when the processing of the request ends up in some error.
   * <p/>
   * For successful responses, the violation part should not be rendered.
   *
   * @see ResponseMetaDataViolationPart
   */
  ResponseMetaDataViolationPart violation

  /**
   * Creates basic ResponseMetaData that contains only a general part with the timestamp and <code>INFO</code> severity.
   *
   * @see Severity
   */
  static ResponseMetaData createBasicInfoResponseMetaData() {
    ResponseMetaData responseMetadata = new ResponseMetaData(general: new ResponseMetaDataGeneralPart(timestamp: Instant.now(), severity: Severity.INFO.name().toLowerCase()))
    return responseMetadata
  }
}
