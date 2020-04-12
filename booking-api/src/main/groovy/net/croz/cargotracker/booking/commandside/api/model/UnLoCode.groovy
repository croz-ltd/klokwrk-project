package net.croz.cargotracker.booking.commandside.api.model

import groovy.transform.Immutable
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import net.croz.cargotracker.lang.groovy.constructor.support.PostMapConstructorCheckable
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler

import java.util.regex.Pattern

/**
 * Code conforming to the UN/LOCODE standard.
 * <p/>
 * Useful reference: https://service.unece.org/trade/locode/Service/LocodeColumn.htm
 */
@Immutable
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@TupleConstructor(visibilityId = "privateVisibility", pre = { throw new IllegalArgumentException("Calling a private constructor is not allowed") })
@VisibilityOptions(id = "privateVisibility", value = Visibility.PRIVATE)
@MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
class UnLoCode implements PostMapConstructorCheckable {

  // Note: be careful to put this static field before UNKNOWN_UN_LO_CODE. Otherwise there will be NPE while constructing UNKNOWN_UN_LO_CODE.
  static final Pattern CODE_PATTERN = Pattern.compile(/^[A-Z]{4}[A-Z2-9]$/)

  static final UnLoCode UNKNOWN_UN_LO_CODE = new UnLoCode(code: "NAUNK")

  /**
   * Code for the UnLoCode location.
   * <p/>
   * The two first digits in indicates the country in which the place is located. The values used concur with the  ISO 3166 alpha-2 Country Code.
   * <p/>
   * In the next part of the code you can find a 3-character code for the location. The 3-character code element for the location will normally comprise three letters. However, where all permutations
   * available for a country have been exhausted, the numerals 2-9 may also be used.
   */
  String code

  @SuppressWarnings("GroovyPointlessBoolean")
  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    assert code
    assert code.isBlank() == false
    assert CODE_PATTERN.matcher(code).matches()
  }

  /**
   * Fetches ISO 3166 alpha-2 Country Code.
   */
  String getCountryCode() {
    return code[0..1]
  }

  /**
   * The part of full UN/LOCODE which refers to the location itself.
   */
  String getLocationCode() {
    return code[2..4]
  }
}
