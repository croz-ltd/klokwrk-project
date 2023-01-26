package org.klokwrk.cargotracker.lib.test.support.fixture.util

import groovy.transform.CompileStatic

import javax.measure.Quantity

@CompileStatic
class JsonFixtureUtils {
  static String stringToJsonString(String stringToRender) {
    String stringToReturn = stringToRender == null ? "null" : /"$stringToRender"/
    return stringToReturn
  }

  @SuppressWarnings("CodeNarc.ReturnsNullInsteadOfEmptyCollection")
  static Map<String, ?> quantityToJsonMap(Quantity quantity) {
    if (quantity == null) {
      return null
    }

    return [
        value: quantity.value,
        unitSymbol: quantity.unit.toString()
    ]
  }

  static String quantityToJsonString(Quantity quantity) {
    if (quantity == null) {
      return "null"
    }

    return """
        {
            "value": $quantity.value,
            "unitSymbol": "$quantity.unit"
        }
    """
  }
}
