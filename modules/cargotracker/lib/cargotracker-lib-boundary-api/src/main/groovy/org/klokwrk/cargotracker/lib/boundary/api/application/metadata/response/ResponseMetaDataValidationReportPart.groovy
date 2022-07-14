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

/**
 * Part of the response metadata containing details about validation failures.
 * <p/>
 * It is rendered only when violation failure occurs. It should not be rendered for any other failure type (domain, other or unknown).
 * <p/>
 * An example of metadata JSON containing violation part rendered for validation failure can be seen in the following example:
 * <pre>
 * {
 *   "metaData": {
 *     "general": {
 *       "severity": "warning",
 *       "locale": "en_GB",
 *       "timestamp": "2020-04-26T09:41:04.917666Z"
 *     },
 *     "http": {
 *       "status": "400",
 *       "message": "Bad Request"
 *     },
 *     "violation": {
 *       "code": "400",
 *       "message": "Request is not valid.",
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
 *
 * @see ValidationReportRoot
 * @see ValidationReportConstraintViolation
 */
@CompileStatic
class ResponseMetaDataValidationReportPart {
  /**
   * In validation report, provides details about the context of failed validation in the form of root object/bean.
   */
  ValidationReportRoot root

  /**
   * In validation report, provides a list of all validation constraint violations caused by the root object or its properties.
   */
  List<ValidationReportConstraintViolation> constraintViolations
}
