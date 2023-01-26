package org.klokwrk.cargotracker.lib.test.support.fixture.util

import groovy.transform.CompileStatic

@CompileStatic
class JsonFixtureUtils {
  static String stringToJsonString(String stringToRender) {
    String stringToReturn = stringToRender == null ? "null" : /"$stringToRender"/
    return stringToReturn
  }
}
