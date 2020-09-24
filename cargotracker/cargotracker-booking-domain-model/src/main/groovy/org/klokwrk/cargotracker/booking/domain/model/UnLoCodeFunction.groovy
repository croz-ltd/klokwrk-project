package org.klokwrk.cargotracker.booking.domain.model

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import java.util.regex.Pattern

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.matchesPattern
import static org.hamcrest.Matchers.not
import static org.valid4j.Assertive.require

/**
 * Represents an 8-character function classifier code for the UN/LOCODE location.
 */
@SuppressWarnings(["DuplicateNumberLiteral"])
@KwrkImmutable(post = { postMapConstructorCheckProtocol(args as Map) })
@CompileStatic
class UnLoCodeFunction implements PostMapConstructorCheckable {
  static final Pattern CODE_PATTERN = Pattern.compile(/^(?=.*[0-7B].*)[01-][2-][3-][4-][5-][6-][7-][B-]$/)

  static final UnLoCodeFunction UNKNOWN_UN_LO_CODE_FUNCTION = new UnLoCodeFunction(functionEncoded: "0-------")

  String functionEncoded

  static UnLoCodeFunction createWithPortClassifier() {
    return new UnLoCodeFunction(functionEncoded: "1-------")
  }

  static UnLoCodeFunction copyWithPortClassifier(UnLoCodeFunction unLoCodeFunctionOriginal) {
    if (unLoCodeFunctionOriginal.functionEncoded[0] == "1") {
      return unLoCodeFunctionOriginal
    }

    StringBuilder builder = new StringBuilder(unLoCodeFunctionOriginal.functionEncoded)
    builder.setCharAt(0, '1' as char)

    return new UnLoCodeFunction(functionEncoded: builder.toString())
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    require(functionEncoded, not(blankOrNullString()))
    require(functionEncoded, matchesPattern(CODE_PATTERN))
  }

  Boolean isSpecified() {
    return functionEncoded[0] != "0"
  }

  Boolean isPort() {
    return functionEncoded[0] == "1"
  }

  Boolean isRailTerminal() {
    return functionEncoded[1] == "2"
  }

  Boolean isRoadTerminal() {
    return functionEncoded[2] == "3"
  }

  Boolean isAirport() {
    return functionEncoded[3] == "4"
  }

  Boolean isPostalExchangeOffice() {
    return functionEncoded[4] == "5"
  }

  Boolean isBorderCrossing() {
    return functionEncoded[7] == "B"
  }
}
