package net.croz.cargotracker.booking.commandside.infrastructure.repository

import groovy.transform.CompileStatic
import net.croz.cargotracker.booking.commandside.application.repository.LocationRegistryRepositoryService
import net.croz.cargotracker.booking.domain.model.InternationalizedName
import net.croz.cargotracker.booking.domain.model.Location
import net.croz.cargotracker.booking.domain.model.UnLoCode
import net.croz.cargotracker.booking.domain.model.UnLoCodeFunction
import org.springframework.stereotype.Service

// TODO dmurat: implement real registry
@Service
@CompileStatic
class InMemoryLocationRegistryRepositoryService implements LocationRegistryRepositoryService {
  Map<String, Location> locationMockRegistry = [
      "HRALJ": new Location(
          unLoCode: new UnLoCode(code: "HRALJ"), name: new InternationalizedName(name: "Aljmaš"), countryName: new InternationalizedName(name: "Hrvatska"),
          unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "1-3-----")
      ),
      "HRBAK": new Location(
          unLoCode: new UnLoCode(code: "HRBAK"), name: new InternationalizedName(name: "Bakar"), countryName: new InternationalizedName(name: "Hrvatska"),
          unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "123-----")
      ),
      "HRBAS": new Location(
          unLoCode: new UnLoCode(code: "HRBAS"), name: new InternationalizedName(name: "Baška"), countryName: new InternationalizedName(name: "Hrvatska"),
          unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "1-3-----")
      ),
      "HRDBV": new Location(
          unLoCode: new UnLoCode(code: "HRDBV"), name: new InternationalizedName(name: "Dubrovnik"), countryName: new InternationalizedName(name: "Hrvatska"),
          unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "1-34----")
      ),
      "HRKOR": new Location(
          unLoCode: new UnLoCode(code: "HRKOR"), name: new InternationalizedName(name: "Korčula"), countryName: new InternationalizedName(name: "Hrvatska"),
          unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "1-3-----")
      ),
      "HRKRK": new Location(
          unLoCode: new UnLoCode(code: "HRKRK"), name: new InternationalizedName(name: "Krk"), countryName: new InternationalizedName(name: "Hrvatska"),
          unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "1-3-----")
      ),
      "HROSI": new Location(
          unLoCode: new UnLoCode(code: "HROSI"), name: new InternationalizedName(name: "Osijek"), countryName: new InternationalizedName(name: "Hrvatska"),
          unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "1234----")
      ),
      "HRPUY": new Location(
          unLoCode: new UnLoCode(code: "HRPUY"), name: new InternationalizedName(name: "Pula"), countryName: new InternationalizedName(name: "Hrvatska"),
          unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "1234----")
      ),
      "HRRJK": new Location(
          unLoCode: new UnLoCode(code: "HRRJK"), name: new InternationalizedName(name: "Rijeka"), countryName: new InternationalizedName(name: "Hrvatska"),
          unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "1234----")
      ),
      "HRSPU": new Location(
          unLoCode: new UnLoCode(code: "HRSPU"), name: new InternationalizedName(name: "Split"), countryName: new InternationalizedName(name: "Hrvatska"),
          unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "1234----")
      ),
      "HRVUK": new Location(
          unLoCode: new UnLoCode(code: "HRVUK"), name: new InternationalizedName(name: "Vukovar"), countryName: new InternationalizedName(name: "Hrvatska"),
          unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "123-5--B")
      ),
      "HRZAD": new Location(
          unLoCode: new UnLoCode(code: "HRZAD"), name: new InternationalizedName(name: "Zadar"), countryName: new InternationalizedName(name: "Hrvatska"),
          unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "1234----")
      ),
      "HRZAG": new Location(
          unLoCode: new UnLoCode(code: "HRZAG"), name: new InternationalizedName(name: "Zagreb"), countryName: new InternationalizedName(name: "Hrvatska"),
          unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "-2345---")
      )
  ]

  Location findByUnLoCode(String unLoCode) {
    Location locationFound = locationMockRegistry.get(
        unLoCode,
        new Location(
            unLoCode: UnLoCode.UNKNOWN_UN_LO_CODE, name: InternationalizedName.UNKNOWN_INTERNATIONALIZED_NAME, countryName: InternationalizedName.UNKNOWN_INTERNATIONALIZED_NAME,
            unLoCodeFunction: UnLoCodeFunction.UNKNOWN_UN_LO_CODE_FUNCTION
        )
    )

    return locationFound
  }
}
