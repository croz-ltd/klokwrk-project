package org.klokwrk.cargotracker.booking.domain.model

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

import java.util.regex.Pattern

/**
 * Represents an 8-character function classifier code for the UN/LOCODE location.
 */
@SuppressWarnings(["DuplicateNumberLiteral", "DuplicateStringLiteral"])
@Immutable
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@TupleConstructor(visibilityId = "privateVisibility", pre = { throw new IllegalArgumentException("Calling a private constructor is not allowed") })
@VisibilityOptions(id = "privateVisibility", value = Visibility.PRIVATE)
@MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
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
    assert functionEncoded
    assert functionEncoded.isBlank() == false
    assert CODE_PATTERN.matcher(functionEncoded).matches()
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
