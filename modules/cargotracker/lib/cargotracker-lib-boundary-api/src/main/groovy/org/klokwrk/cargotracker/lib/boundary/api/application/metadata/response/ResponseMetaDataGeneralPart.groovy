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
package org.klokwrk.cargotracker.lib.boundary.api.application.metadata.response

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.domain.severity.Severity

import java.time.Instant

/**
 * Part of the response metadata that contains general information.
 * <p/>
 * It should be rendered for all responses.
 * <p/>
 * An example of metadata containing general part can be seen in the following example:
 * <pre>
 * {
 *   "metaData": {
 *     "general": {
 *       "severity": "warning",
 *       "locale": "en_GB",
 *       "timestamp": "2020-04-30T16:03:48.795816Z"
 *     }
 *     ...
 *   },
 *   "payload": {
 *     ...
 *   }
 * }
 * </pre>
 */
@CompileStatic
class ResponseMetaDataGeneralPart {
  /**
   * The UTC timestamp of the response.
   */
  Instant timestamp

  /**
   * Severity of the response as a lowercase string corresponding to the values of {@link Severity} enum.
   */
  String severity

  /**
   * Locale used for rendering response message.
   * <p/>
   * Usually, it corresponds to the locale of the current request.
   */
  Locale locale
}
