/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.adapter.out.remoting

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.out.FindLocationPortOut
import org.klokwrk.cargotracker.booking.domain.model.Location
import org.springframework.stereotype.Service

// TODO dmurat: implement real registry
@Service
@CompileStatic
class InMemoryLocationRegistryService implements FindLocationPortOut {
  Location findByUnLoCode(String unLoCode) {
    Location locationFound = LocationSample.findByUnLoCode(unLoCode)
    return locationFound
  }

  static class LocationSample {
    @SuppressWarnings("CodeNarc.DuplicateStringLiteral")
    static final Map<String, Location> LOCATION_SAMPLE_MAP = [
        "HRALJ": Location.create("HRALJ", "Aljmaš", "Hrvatska", "1-3-----"),
        "HRBAK": Location.create("HRBAK", "Bakar", "Hrvatska", "123-----"),
        "HRBAS": Location.create("HRBAS", "Baška", "Hrvatska", "1-3-----"),
        "HRDBV": Location.create("HRDBV", "Dubrovnik", "Hrvatska", "1-34----"),
        "HRDKO": Location.create("HRDKO", "Đakovo", "Hrvatska", "--3-----"),
        "HRKOR": Location.create("HRKOR", "Korčula", "Hrvatska", "1-3-----"),
        "HRKRK": Location.create("HRKRK", "Krk", "Hrvatska", "1-3-----"),
        "HRMVN": Location.create("HRMVN", "Motovun", "Hrvatska", "--3-----"),
        "HROSI": Location.create("HROSI", "Osijek", "Hrvatska", "1234----"),
        "HRPUY": Location.create("HRPUY", "Pula", "Hrvatska", "1234----"),
        "HRRJK": Location.create("HRRJK", "Rijeka", "Hrvatska", "1234----"),
        "HRSPU": Location.create("HRSPU", "Split", "Hrvatska", "1234----"),
        "HRVUK": Location.create("HRVUK", "Vukovar", "Hrvatska", "123-5--B"),
        "HRVZN": Location.create("HRVZN", "Varaždin", "Hrvatska", "-23-----"),
        "HRZAD": Location.create("HRZAD", "Zadar", "Hrvatska", "1234----"),
        "HRZAG": Location.create("HRZAG", "Zagreb", "Hrvatska", "-2345---")
    ]

    static Location findByUnLoCode(String unLoCode) {
      Location locationFound = LOCATION_SAMPLE_MAP.get(unLoCode, Location.UNKNOWN_LOCATION)
      return locationFound
    }
  }
}
