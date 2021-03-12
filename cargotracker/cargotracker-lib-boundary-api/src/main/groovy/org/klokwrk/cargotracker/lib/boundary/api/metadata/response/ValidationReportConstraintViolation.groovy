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
 * Part of the response metadata validation report containing details about individual violation of some validation constraint.
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
 *         root: { type: "myRequest", message: "Request myRequest is not valid." }
 *         constraintViolations: [
 *           { type: "customObjectLevelValidationType", scope: "object", path: "", message: "The combination of properties is not quite right." },
 *           { type: "notNull", scope: "property", path: "myProperty", message: "must not be null", invalidPropertyValue: "null" },
 *           { type: "notNull", scope: "property", path: "myOtherProperty", message: "must not be null", invalidPropertyValue: "null" },
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
class ValidationReportConstraintViolation {
  /**
   * The type of violated validation constraint.
   * <p/>
   * Corresponds to the uncapitalized simple class name of the constraint annotation, for example, {@code notNull}, {@code notBlank}, etc.
   */
  String type

  /**
   * The scope of violated validation constraint describing weather constraint is related to the property or object.
   * <p/>
   * Should contain only {@code property} or {@code object} values.
   * <p/>
   * Corresponds to the location where related constraint annotation is applied, for example, {@code FIELD} or {@code GETTER} for {@code property} scope or {@code TYPE} (class) for {@code object}
   * scope.
   */
  String scope

  /**
   * The path of property or object that caused concrete validation constraint violation.
   * <p/>
   * It does not include root object type.
   */
  String path

  /**
   * Localized message for concrete validation constraint violation.
   */
  String message

  /**
   * For validation constraint violations with the {@code property} scope, contains the value of that property.
   * <p/>
   * It should not be rendered for {@code object} scope.
   */
  String invalidPropertyValue
}
