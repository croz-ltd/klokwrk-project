package org.klokwrk.cargotracker.lib.boundary.api.metadata.response

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity

import java.time.Instant

/**
 * Part of the response metadata that contains general information.
 * <p/>
 * It should be rendered for all responses.
 * <p/>
 * An example of metadata containing general part can be seen in the following example:
 * <pre>
 * {
 *     "metaData": {
 *         "general": {
 *             "severity": "WARNING",
 *             "locale": "en_GB",
 *             "timestamp": "2020-04-30T16:03:48.795816Z"
 *         }
 *         ...
 *     },
 *     "payload": {
 *         ...
 *     }
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
   * Severity of the response.
   */
  Severity severity

  /**
   * Locale used for rendering response message.
   * <p/>
   * Usually, it corresponds to the locale of the current request.
   */
  Locale locale
}
