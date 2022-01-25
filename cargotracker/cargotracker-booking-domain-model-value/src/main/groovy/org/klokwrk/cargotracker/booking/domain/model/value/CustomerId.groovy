package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.misc.UUIDUtils
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not

/**
 * Encapsulates an identifier of a Customer aggregate from external bounded context.
 * <p/>
 * Identifier does not have any business meaning. It is used just for referencing.
 * <p/>
 * Encapsulated identifier must exactly match whatever is used in external bounded context for Customer identifying.
 */
@KwrkImmutable
@CompileStatic
class CustomerId implements PostMapConstructorCheckable {
  String identifier

  /**
   * Factory method for creating {@code CustomerId} instance.
   *
   * @param uuidString String in random UUID format (UUID version 4, variant 2).
   */
  static CustomerId make(String uuidString) {
    return new CustomerId(identifier: uuidString)
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(identifier, not(blankOrNullString()))
    requireMatch(UUIDUtils.checkIfRandomUuid(identifier), is(true))
  }
}
