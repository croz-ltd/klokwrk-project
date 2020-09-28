package org.klokwrk.cargotracker.lib.boundary.api.violation

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import static org.hamcrest.Matchers.notNullValue

/**
 * Defines an immutable data structure that describes the reason for the <code>DomainException</code> exception.
 * <p/>
 * It contains the violation's severity and data structure describing the code of the violation. Both members need to be specified at construction time.
 *
 * @see org.klokwrk.cargotracker.lib.boundary.api.exception.DomainException
 * @see ViolationCode
 * @see Severity
 */
@KwrkImmutable(post = { postMapConstructorCheckProtocol(args as Map) })
@CompileStatic
class ViolationInfo implements PostMapConstructorCheckable {
  static final ViolationInfo UNKNOWN = new ViolationInfo(severity: Severity.ERROR, violationCode: ViolationCode.UNKNOWN)
  static final ViolationInfo BAD_REQUEST = new ViolationInfo(severity: Severity.WARNING, violationCode: ViolationCode.BAD_REQUEST)
  static final ViolationInfo NOT_FOUND = new ViolationInfo(severity: Severity.WARNING, violationCode: ViolationCode.NOT_FOUND)

  static ViolationInfo createForBadRequestWithCustomCodeAsText(String customCodeAsText) {
    ViolationCode violationCode = new ViolationCode(code: ViolationCode.BAD_REQUEST.code, codeAsText: customCodeAsText, codeMessage: ViolationCode.BAD_REQUEST.codeMessage)
    return new ViolationInfo(severity: ViolationInfo.BAD_REQUEST.severity, violationCode: violationCode)
  }

  Severity severity
  ViolationCode violationCode

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(severity, notNullValue())
    requireMatch(violationCode, notNullValue())
  }
}
