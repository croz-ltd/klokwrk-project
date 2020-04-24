package net.croz.cargotracker.booking.domain.modelsample

import groovy.transform.CompileStatic
import net.croz.cargotracker.booking.domain.model.Location

@CompileStatic
class LocationSample {
  static final Map<String, Location> locationSampleMap = [
      "HRALJ": Location.create("HRALJ", "Aljmaš", "Hrvatska", "1-3-----"),
      "HRBAK": Location.create("HRBAK", "Bakar", "Hrvatska", "123-----"),
      "HRBAS": Location.create("HRBAS", "Baška", "Hrvatska", "1-3-----"),
      "HRDBV": Location.create("HRDBV", "Dubrovnik", "Hrvatska", "1-34----"),
      "HRKOR": Location.create("HRKOR", "Korčula", "Hrvatska", "1-3-----"),
      "HRKRK": Location.create("HRKRK", "Krk", "Hrvatska", "1-3-----"),
      "HROSI": Location.create("HROSI", "Osijek", "Hrvatska", "1234----"),
      "HRPUY": Location.create("HRPUY", "Pula", "Hrvatska", "1234----"),
      "HRRJK": Location.create("HRRJK", "Rijeka", "Hrvatska", "1234----"),
      "HRSPU": Location.create("HRSPU", "Split", "Hrvatska", "1234----"),
      "HRVUK": Location.create("HRVUK", "Vukovar", "Hrvatska", "123-5--B"),
      "HRZAD": Location.create("HRZAD", "Zadar", "Hrvatska", "1234----"),
      "HRZAG": Location.create("HRZAG", "Zagreb", "Hrvatska", "-2345---")
  ]

  static Location findByUnLoCode(String unLoCode) {
    Location locationFound = locationSampleMap.get(unLoCode, Location.UNKNOWN_LOCATION)
    return locationFound
  }
}
