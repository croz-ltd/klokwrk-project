package org.klokwrk.cargotracker.lib.boundary.api.violation

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.not
import static org.valid4j.Assertive.require

/**
 * Immutable data structure describing violation's code and the corresponding non-localized code's message.
 * <p/>
 * There is also <code>codeAsText</code> property that is used for easier resolving of localized messages outside of the domain's boundary.
 * <p/>
 * All three members must be specified at construction time.
 */
@KwrkImmutable(post = { postMapConstructorCheckProtocol(args as Map) })
@CompileStatic
class ViolationCode implements PostMapConstructorCheckable {
  static final ViolationCode UNKNOWN = new ViolationCode(code: "500", codeAsText: "internalServerError", codeMessage: "Internal Server Error")
  static final ViolationCode BAD_REQUEST = new ViolationCode(code: "400", codeAsText: "badRequest", codeMessage: "Bad Request")
  static final ViolationCode NOT_FOUND = new ViolationCode(code: "404", codeAsText: "notFound", codeMessage: "Not Found")

  /**
   * The primary code describing the main category of the violation.
   * <p/>
   * In general, it does not have to be designed to be human-readable, but rather it should be in the form of some primary violation/error identifier. For example, the categorization of HTTP response
   * statuses (200, 400, 404, 500, etc.), or database error code categorizations, are good examples of the kind of information that should go in here.
   */
  String code

  /**
   * More human-readable alias for <code>code</code> property.
   * <p/>
   * In this context, human-readable does not mean full sentences but rather some textual encoded value that is easily recognizable by developers. The intention is that <code>codeAsText</code> is
   * used as an alias of primary code property that is more appealing for writing localized resource bundles at the inbound channel level. For example, when maintaining resource bundles, it should be
   * easier for developers to deduct the meaning of <code>cargoSummaryQueryWebController.fetchCargoSummaryQuery.failure.badRequest.report.titleDetailedText</code> resource bundle key instead the
   * meaning of <code>cargoSummaryQueryWebController.fetchCargoSummaryQuery.failure.400.report.titleDetailedText</code> key. And this is exactly the intention behind this property.
   */
  String codeAsText

  /**
   * A short human-readable message written in English describing the problem identified by primary code.
   * <p/>
   * For example, in HTTP error handling, this message would correspond to the textual descriptions of status codes like "OK", "Internal Server Error", "Not Found", etc.
   */
  String codeMessage

  @SuppressWarnings("GroovyPointlessBoolean")
  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    require(code, not(blankOrNullString()))
    require(codeAsText, not(blankOrNullString()))
    require(codeMessage, not(blankOrNullString()))
  }
}
