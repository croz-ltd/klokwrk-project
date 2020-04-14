package net.croz.cargotracker.booking.commandside.infrastructure.repository

import groovy.transform.CompileStatic
import net.croz.cargotracker.booking.commandside.application.repository.LocationRegistryRepositoryService
import net.croz.cargotracker.booking.domain.model.InternationalizedName
import net.croz.cargotracker.booking.domain.model.Location
import net.croz.cargotracker.booking.domain.model.UnLoCode
import org.springframework.stereotype.Service

// TODO dmurat: implement real registry
@Service
@CompileStatic
class InMemoryLocationRegistryRepositoryService implements LocationRegistryRepositoryService {
  Map<String, Location> locationMockRegistry = [
      "HRALJ": new Location(unLoCode: new UnLoCode(code: "HRALJ"), name: new InternationalizedName(name: "Aljmaš"), countryName: new InternationalizedName(name: "Hrvatska")),
      "HRBAK": new Location(unLoCode: new UnLoCode(code: "HRBAK"), name: new InternationalizedName(name: "Bakar"), countryName: new InternationalizedName(name: "Hrvatska")),
      "HRBAS": new Location(unLoCode: new UnLoCode(code: "HRBAS"), name: new InternationalizedName(name: "Baška"), countryName: new InternationalizedName(name: "Hrvatska")),
      "HRKOR": new Location(unLoCode: new UnLoCode(code: "HRKOR"), name: new InternationalizedName(name: "Korčula"), countryName: new InternationalizedName(name: "Hrvatska")),
      "HRRJK": new Location(unLoCode: new UnLoCode(code: "HRRJK"), name: new InternationalizedName(name: "Rijeka"), countryName: new InternationalizedName(name: "Hrvatska"))
  ]

  Location findByUnLoCode(String unLoCode) {
    Location locationFound = locationMockRegistry.get(
        unLoCode,
        new Location(unLoCode: UnLoCode.UNKNOWN_UN_LO_CODE, name: InternationalizedName.UNKNOWN_INTERNATIONALIZED_NAME, countryName: InternationalizedName.UNKNOWN_INTERNATIONALIZED_NAME)
    )

    return locationFound
  }
}
