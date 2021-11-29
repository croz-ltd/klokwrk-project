package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.misc.UUIDUtils
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not

/**
 * Represents an identifier of {@code CargoAggregate} without any business meaning.
 * <p/>
 * This identifier is used for technical purposes of identifying an aggregate in CQRS/ES system. With current implementation, an encapsulated string identifier must be parsable by
 * {@link UUID#fromString(java.lang.String)} method.
 */
@KwrkImmutable
@CompileStatic
class CargoId implements PostMapConstructorCheckable {
  String identifier

  /**
   * Factory method for creating {@code CargoId} instance.
   *
   * @param uuidString String in UUID format. Must be parsable by {@link UUID#fromString(java.lang.String)} method.
   */
  static CargoId create(String uuidString) {
    return new CargoId(identifier: uuidString)
  }

  /**
   * Factory method for creating {@code CargoId} instance with generated internal identifier value.
   * <p/>
   * Method will generate internal identifier with {@link UUID#randomUUID()}.
   *
   * @param uuidString String in UUID format. If not {@code null}, must be parsable by {@link UUID#fromString(java.lang.String)} method.
   */
  static CargoId createWithGeneratedIdentifier() {
    String uuidStringToUse = UUID.randomUUID()
    return create(uuidStringToUse)
  }

  /**
   * Factory method for creating {@code CargoId} instance with generated internal identifier value if needed.
   * <p/>
   * When supplied {@code uuidString} is {@code null} or empty string, method will generate internal identifier with {@link UUID#randomUUID()}.
   *
   * @param uuidString String in UUID format. If not {@code null} or empty string, must be parsable by {@link UUID#fromString(java.lang.String)} method.
   */
  static CargoId createWithGeneratedIdentifierIfNeeded(String uuidString) {
    String uuidStringToUse = uuidString ?: UUID.randomUUID().toString()
    return create(uuidStringToUse)
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(identifier, not(blankOrNullString()))
    requireMatch(UUIDUtils.checkIfRandomUuid(identifier), is(true))
  }
}
