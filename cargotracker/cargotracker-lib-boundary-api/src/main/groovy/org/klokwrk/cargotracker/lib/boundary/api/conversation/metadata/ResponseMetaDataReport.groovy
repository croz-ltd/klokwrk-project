package org.klokwrk.cargotracker.lib.boundary.api.conversation.metadata

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
 *         "titleText": "Info",
 *         "timestamp": "2020-04-30T15:37:24.999722Z",
 *         "titleDetailedText": "Your request is successfully executed."
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
   * Localized and short description of the response.
   * <p/>
   * Can contain very brief, ideally single word, description of the response. Some examples might be "<code>Info</code>", "<code>Warning</code>" or "<code>Error</code>". It should be suitable for
   * presenting as a title of a notification displayed to the user.
   */
  String titleText

  /**
   * Localized and more detailed, but still short, description of the response.
   * <p/>
   * Ideally, it should be in the form of a single complete sentence. For example, something like "<code>Your request is successfully executed.</code>" or "<code>Your request is not accepted since
   * provided data are not valid.</code>". It should be suitable for presenting as a leading, more detailed description of a notification displayed to the user.
   * <p/>
   * Do note that in the case of validation errors, a full list of violated data rules is not part of this message. These should be specified as part of <code>violation</code>.
   */
  String titleDetailedText

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
