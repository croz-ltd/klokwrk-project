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
