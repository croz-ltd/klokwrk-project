package org.klokwrk.cargotracker.lib.boundary.api.conversation.metadata

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
 *         "severity": "WARNING",
 *         "violation": {
 *             "code": "400",
 *             "codeMessage": "Destination location cannot accept cargo from specified origin location."
 *         },
 *         "locale": "en_GB",
 *         "titleText": "Warning",
 *         "timestamp": "2020-04-30T16:03:48.795816Z",
 *         "titleDetailedText": "Cargo is not booked since destination location cannot accept cargo from specified origin location."
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
