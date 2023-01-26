package org.klokwrk.cargotracker.lib.test.support.fixture.util

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.test.support.fixture.base.JsonFixtureBuilder

import javax.measure.Quantity

@CompileStatic
class JsonFixtureUtils {
  static String stringToJsonString(String stringToRender) {
    String stringToReturn = stringToRender == null ? "null" : /"$stringToRender"/
    return stringToReturn
  }

  static List<Map<String, ?>> jsonFixtureBuilderListToJsonList(List<? extends JsonFixtureBuilder> jsonFixtureBuilderList) {
    List<Map<String, ?>> listToUse = []
    jsonFixtureBuilderList.each {
      listToUse.add(it.buildAsMap())
    }

    return listToUse
  }

  static String jsonFixtureBuilderListToJsonListString(List<? extends JsonFixtureBuilder> jsonFixtureBuilderList) {
    String listStringContent = jsonFixtureBuilderList.collect({ it.buildAsJsonString() }).join(",")
    return "[$listStringContent]"
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
