/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.adapter.out.remoting

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.out.LocationByUnLoCodeQueryPortOut
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.booking.domain.model.value.PortCapabilities
import org.springframework.stereotype.Service

// TODO dmurat: implement real registry
@Service
@CompileStatic
class InMemoryLocationRegistryService implements LocationByUnLoCodeQueryPortOut {
  Location locationByUnLoCodeQuery(String unLoCode) {
    Location locationFound = LocationSample.locationByUnLoCodeQuery(unLoCode)
    return locationFound
  }

  static class LocationSample {
    @SuppressWarnings("CodeNarc.DuplicateStringLiteral")
    static final Map<String, Location> LOCATION_SAMPLE_MAP = [
        // Locations in Croatia
        "HRKRK": Location.make("HRKRK", "Krk", "Croatia", "1-3-----", "4502N 01435E", PortCapabilities.SEA_PORT_CAPABILITIES),
        "HRRJK": Location.make("HRRJK", "Rijeka", "Croatia", "1234----", "4520N 01424E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "HRZAG": Location.make("HRZAG", "Zagreb", "Croatia", "-2345---", "4548N 01600E"),

        // Top 10 container ports in Europe, together with their corresponding state capital cities
        // ==========
        // 1. Port of Rotterdam (Netherlands), Amsterdam capital
        "NLRTM": Location.make("NLRTM", "Rotterdam", "Netherlands", "12345---", "5155N 00430E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "NLAMS": Location.make("NLAMS", "Amsterdam", "Netherlands", "12345---", "5224N 00449E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),

        // 2. Port of Antwerp (Belgium), Brussel capital
        "BEANR": Location.make("BEANR", "Antwerpen", "Belgium", "12345---", "5113N 00425E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "BEBRU": Location.make("BEBRU", "Brussel", "Belgium", "1234----", "5050N 00420E", PortCapabilities.RIVER_PORT_CAPABILITIES),

        // 3. Port of Hamburg (Germany), Berlin
        "DEHAM": Location.make("DEHAM", "Hamburg", "Germany", "12345---", "5331N 00956E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "DEBER": Location.make("DEBER", "Berlin", "Germany", "12345---", "5231N 01323E", PortCapabilities.RIVER_PORT_CAPABILITIES),

        // 4. Port of Bremerhaven (Germany), Berlin
        "DEBRV": Location.make("DEBRV", "Bremerhaven", "Germany", "1234----", "5333N 00835E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),

        // 5. Port of Algeciras (Spain), Madrid
        "ESALG": Location.make("ESALG", "Algeciras", "Spain", "1-------", "3607N 00526W", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "ESMAD": Location.make("ESMAD", "Madrid", "Spain", "-2345---", "4025N 00343W"),

        // 6. Port of Piraeus (Greece), Athens
        "GRPIR": Location.make("GRPIR", "Piraeus", "Greece", "1-------", "3756N 02337E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "GRATH": Location.make("GRATH", "Athens", "Greece", "---45---", "3759N 02344E"),

        // 7. Port of Valencia (Spain), Madrid
        "ESVLC": Location.make("ESVLC", "Valencia", "Spain", "12345---", "3928N 00023W", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),

        // 8. Port of Felixstowe (United Kingdom), London
        "GBFXT": Location.make("GBFXT", "Felixstowe", "United Kingdom", "1-------", "5158N 00118E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "GBLON": Location.make("GBLON", "London", "United Kingdom", "1---5---", "5130N 00008W", PortCapabilities.RIVER_PORT_CAPABILITIES),

        // 9. Port of Barcelona (Spain), Madrid
        "ESBCN": Location.make("ESBCN", "Barcelona", "Spain", "12345---", "4123N 00211E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),

        //10. Port of Le Havre (France), Paris
        "FRLEH": Location.make("FRLEH", "Le Havre", "France", "12345---", "4930N 00006E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "FRPAR": Location.make("FRPAR", "Paris", "France", "123-5---", "4851N 00221E", PortCapabilities.RIVER_PORT_CAPABILITIES),
        // ==========

        // Selection of other important container ports in the world, together with their corresponding state capital cities
        // ==========
        // 1. Shanghai (China), Beijing
        "CNSGH": Location.make("CNSGH", "Shanghai", "China", "12345---", "3114N 12129E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "CNBJS": Location.make("CNBJS", "Beijing", "China", "-23456--", "3955N 11624E"),

        // 2. Hong Kong (Hong Kong), Victoria
        "HKHKG": Location.make("HKHKG", "Hong Kong", "Hong Kong", "1-3-----", "2222N 11408E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "HKVIC": Location.make("HKVIC", "Victoria", "Hong Kong", "1-------", "2217N 11409E", PortCapabilities.SEA_PORT_CAPABILITIES),

        // 3. Singapore (Singapore), Singapore
        "SGSIN": Location.make("SGSIN", "Singapore", "Singapore", "1--45---", "0116N 10345E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),

        // 4. Busan Port (Republic of Korea), Seoul
        "KRPUS": Location.make("KRPUS", "Busan", "Republic of Korea", "1234567-", "3508N 12903E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "KRSEL": Location.make("KRSEL", "Seoul", "Republic of Korea", "12345---", "3731N 12656E", PortCapabilities.RIVER_PORT_CAPABILITIES),

        // 5. Jebel Ali (The United Arab Emirates), Abu Dhabi
        "AEJEA": Location.make("AEJEA", "Jebel Ali", "The United Arab Emirates", "1-------", "2500N 05503E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "AEAUH ": Location.make("AEAUH", "Abu Dhabi", "The United Arab Emirates", "1-345---", "2428N 05422E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),

        // 6. Los Angeles (The United States of America), Washington
        "USLAX": Location.make("USLAX", "Los Angeles", "The United States of America", "1--45---", "3344N 11816W", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "USWAS": Location.make("USWAS", "Washington", "The United States of America", "-234----", "3855N 07701W"),

        // 7. Colombo (Sri Lanka), Colombo
        "LKCMB": Location.make("LKCMB", "Colombo", "Sri Lanka", "12345---", "0655N 07951E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),

        // 8. Jawaharlal Nehru (India), New Delhi
        "INNSA": Location.make("INNSA", "Jawaharlal Nehru", "India", "1-------", "1857N 07257E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "INICD": Location.make("INICD", "New Delhi", "India", "123--6--", "2836N 07712E", PortCapabilities.RIVER_PORT_CAPABILITIES),

        // 9. Jeddah (Saudi Arabia), Riyadh
        "SAJED": Location.make("SAJED", "Jeddah", "Saudi Arabia", "1--45---", "2132N 03910E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "SARUH": Location.make("SARUH", "Riyadh", "Saudi Arabia", "---45---", "2438N 04646E"),

        //10. Salalah (Oman), Muscat
        "OMSLL": Location.make("OMSLL", "Salalah", "Oman", "1--4----", "2438N 04646E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "OMMCT": Location.make("OMMCT", "Muscat", "Oman", "1--45---", "2336N 05835E", PortCapabilities.SEA_PORT_CAPABILITIES),

        //11. New York (The United States of America), Washington
        "USNYC": Location.make("USNYC", "New York", "The United States of America", "12345---", "4042N 07400W", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),

        //12. Santos (Brazil), Brasilia
        "BRSSZ": Location.make("BRSSZ", "Santos", "Brazil", "1234----", "2357S 04620W", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "BRBSB": Location.make("BRBSB", "Brasilia", "Brazil", "---4----", "1547S 04755W"),

        //13. Tanger Med (Morocco), Rabat
        "MAPTM": Location.make("MAPTM", "Tanger Med", "Morocco", "123-----", "3554N 00530W", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "MARBA": Location.make("MARBA", "Rabat", "Morocco", "1--4----", "3357N 00654W", PortCapabilities.SEA_PORT_CAPABILITIES),

        //14. Brisbane (Australia), Canberra
        "AUBNE": Location.make("AUBNE", "Brisbane", "Australia", "12345---", "2728S 15301E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
        "AUCBR": Location.make("AUCBR", "Canberra", "Australia", "12345---", "3517S 14908E", PortCapabilities.RIVER_PORT_CAPABILITIES),
        // ==========
    ]

    static Location locationByUnLoCodeQuery(String unLoCode) {
      Location locationFound = LOCATION_SAMPLE_MAP.get(unLoCode, Location.UNKNOWN_LOCATION)
      return locationFound
    }
  }
}
