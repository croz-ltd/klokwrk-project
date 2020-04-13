package net.croz.cargotracker.booking.domain.model

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
 * </ul>
 */
@Immutable
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@TupleConstructor(visibilityId = "privateVisibility", pre = { throw new IllegalArgumentException("Calling a private constructor is not allowed") })
@VisibilityOptions(id = "privateVisibility", value = Visibility.PRIVATE)
@MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
@CompileStatic
class Location implements PostMapConstructorCheckable {
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

  // TODO dmurat: expand it into object. Do not use right now
//  String function
  // TODO dmurat: expand it into object. Do not use right now
//  String status
  // TODO dmurat: expand it into object. Do not use right now
//  String coordinates

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    assert unLoCode
    assert name
    assert countryName
  }
}
