package org.klokwrk.cargotracker.booking.domain.model

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import net.croz.cargotracker.lang.groovy.constructor.support.PostMapConstructorCheckable
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler

/**
 * Represents a location data as specified by UN/LOCODE standard.
 * <p/>
 * Useful references:
 * <ul>
 *   <li>https://www.unece.org/cefact/codesfortrade/codes_index.html</li>
 *   <li>https://service.unece.org/trade/locode/Service/LocodeColumn.htm</li>
 *   <li>https://www.unece.org/cefact/locode/service/location</li>
 *   <li>https://service.unece.org/trade/locode/hr.htm</li>
 *   <li>http://tfig.unece.org/contents/recommendation-16.htm</li>
 * </ul>
 */
@Immutable
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@TupleConstructor(visibilityId = "privateVisibility", pre = { throw new IllegalArgumentException("Calling a private constructor is not allowed") })
@VisibilityOptions(id = "privateVisibility", value = Visibility.PRIVATE)
@MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
@CompileStatic
class Location implements PostMapConstructorCheckable {

  static final Location UNKNOWN_LOCATION = new Location(
      unLoCode: UnLoCode.UNKNOWN_UN_LO_CODE, name: InternationalizedName.UNKNOWN_INTERNATIONALIZED_NAME, countryName: InternationalizedName.UNKNOWN_INTERNATIONALIZED_NAME,
      unLoCodeFunction: UnLoCodeFunction.UNKNOWN_UN_LO_CODE_FUNCTION
  )

  UnLoCode unLoCode

  /**
   * Location name expressed in their natural language if possible.
   * <p/>
   * Original UnLoCode spec defines character set as: "Roman alphabet using the 26 characters of the character set adopted for international trade data interchange, with diacritic signs, when
   * practicable". However, we are using UTF-8 so all location names can be fully specified in their natural language.
   */
  InternationalizedName name

  /**
   * The name of the country to which this location belongs.
   */
  InternationalizedName countryName

  /**
   * 8-character function classifier code for the UN/LOCODE location.
   */
  UnLoCodeFunction unLoCodeFunction

  // TODO dmurat: expand it into object. Do not use right now
//  String status
  // TODO dmurat: expand it into object. Do not use right now
//  String coordinates

  static Location create(String unLoCode, String name, String countryName, String unLoCodeFunction) {
    Location createdLocation = new Location(
        unLoCode: new UnLoCode(code: unLoCode), name: new InternationalizedName(name: name), countryName: new InternationalizedName(name: countryName),
        unLoCodeFunction: new UnLoCodeFunction(functionEncoded: unLoCodeFunction)
    )

    return createdLocation
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    assert unLoCode
    assert name
    assert countryName
    assert unLoCodeFunction
  }

  Boolean canAcceptCargoFrom(Location originLocation) {
    if (this == originLocation) {
      return false
    }

    if (!originLocation) {
      return false
    }

    if (unLoCodeFunction.isPort() && originLocation.unLoCodeFunction.isPort()) {
      return true
    }

    if (unLoCodeFunction.isRailTerminal() && originLocation.unLoCodeFunction.isRailTerminal()) {
      return true
    }

    return false
  }
}
