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
package org.klokwrk.cargotracker.lib.boundary.api.metadata.response

import groovy.transform.CompileStatic

/**
 * Part of the response metadata validation report containing details about validation failure context in the form of root object/bean.
 * <p/>
 * An example of corresponding metadata JSON with only interesting parts included:
 * <pre>
 * {
 *   "metaData": {
 *     "general": { ... },
 *     "http": { ... },
 *     "violation": {
 *       "code": "400",
 *       "codeMessage": "Request is not valid.",
 *       "type": "validation",
 *
 *       "validationReport": {
 *         root: {
 *           type: "myRequest",
 *           message: "Request myRequest is not valid."
 *         }
 *         constraintViolations: [
 *           ...
 *         ]
 *       }
 *     }
 *   },
 *   "payload": {}
 * }
 * </pre>
 */
@CompileStatic
class ValidationReportRoot {
  /**
   * The simple uncapitalized class name of the root object.
   * <p/>
   * Do note that in constraint violations, this name is not included at the beginning of the property path.
   */
  String type

  /**
   * A message describing validation failure context.
   * <p/>
   * In many situations this message will be the same as {@code metaData.violation.codeMessage}. However, occasionally we might want to have a more specific high-level description depending on the
   * concrete context of validation failure.
   */
  String message
}