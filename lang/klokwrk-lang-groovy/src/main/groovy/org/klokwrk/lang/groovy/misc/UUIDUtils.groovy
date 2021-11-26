package org.klokwrk.lang.groovy.misc

import groovy.transform.CompileStatic

@CompileStatic
class UUIDUtils {

  /**
   * Checks if provided string represents random UUID (uuid version 4 and uuid variant 2, i.e. {@code 00000000-0000-4000-8000-000000000000}).
   */
  @SuppressWarnings("CodeNarc.CatchException")
  static Boolean checkIfRandomUuid(String uuidStringToCheck) {
    if (!uuidStringToCheck) {
      return false
    }

    UUID uuid

    try {
      uuid = UUID.fromString(uuidStringToCheck)
    }
    catch (Exception ignore) {
      return false
    }

    Boolean isVersionValid = uuid.version() == 4
    Boolean isVariantValid = uuid.variant() == 2
    return isVersionValid && isVariantValid
  }
}
