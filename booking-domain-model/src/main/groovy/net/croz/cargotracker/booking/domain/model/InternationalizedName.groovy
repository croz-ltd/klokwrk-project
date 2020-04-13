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

import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Represents an Unicode name capable to produce internationalized name.
 * </p>
 * Internationalized name is produced simply by replacing diacritic characters with their non-diacritic Unicode counterparts. For the majority of diacritic characters, their non-diacritic counterpart
 * is encoded in Unicode itself. Therefore, such Unicode diacritic characters can be replaced by simple regex matching after decomposing them into characters canonical form containing separate codes
 * for base character and diacritic. In general this can be accomplished with following code fragment:
 * <pre>
 *   String nonDiacriticName = Normalizer.normalize(originalName, Normalizer.Form.NFD).replaceAll(DIACRITIC_MATCHING_PATTERN, "")
 * </pre>
 * Unfortunatelly, there are some diacritic characters that do not have separate code for diacritic. One example is "LATIN SMALL/CAPITAL LETTER D WITH STROKE" (<code>đ/Đ</code>). For these diacritic
 * characters additional custom replacement is needed as is implemented in {@link InternationalizedName#getNameInternationalized()} method.
 * <p/>
 * Some useful references:
 * <ul>
 *   <li>https://web.archive.org/web/20070917051642/http://java.sun.com/mailers/techtips/corejava/2007/tt0207.html#1</li>
 *   <li>https://docs.oracle.com/javase/8/docs/api/java/text/Normalizer.html</li>
 *   <li>https://web.archive.org/web/20200329072305/https://www.unicode.org/reports/tr44/#Properties</li>
 *   <li>https://memorynotfound.com/remove-accents-diacritics-from-string</li>
 * </ul>
 */
@Immutable
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@TupleConstructor(visibilityId = "privateVisibility", pre = { throw new IllegalArgumentException("Calling a private constructor is not allowed") })
@VisibilityOptions(id = "privateVisibility", value = Visibility.PRIVATE)
@MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
@CompileStatic
class InternationalizedName implements PostMapConstructorCheckable {
  static final InternationalizedName UNKNOWN_INTERNATIONALIZED_NAME = new InternationalizedName(name: "UNKNOWN")

  private static final Pattern DIACRITIC_MATCHING_PATTERN = Pattern.compile(/[\p{InCombiningDiacriticalMarks}]+/)
  private static final Map<CharSequence, CharSequence> ADDITIONAL_REPLACEMENTS_MAP = [
      đ: "d",
      Đ: "D"
  ] as Map<CharSequence, CharSequence>

  String name

  @SuppressWarnings("GroovyPointlessBoolean")
  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    assert name
    assert name.isBlank() == false
  }

  String getNameInternationalized() {
    return Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll(DIACRITIC_MATCHING_PATTERN, "").replace(ADDITIONAL_REPLACEMENTS_MAP)
  }
}
