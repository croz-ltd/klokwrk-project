package org.klokwrk.cargotracker.booking.commandside.infrastructure.repository

import groovy.transform.CompileStatic
import net.croz.cargotracker.booking.domain.model.Location
import net.croz.cargotracker.booking.domain.modelsample.LocationSample
import org.klokwrk.cargotracker.booking.commandside.application.repository.LocationRegistryRepositoryService
import org.springframework.stereotype.Service

// TODO dmurat: implement real registry
@Service
@CompileStatic
class InMemoryLocationRegistryRepositoryService implements LocationRegistryRepositoryService {
  Location findByUnLoCode(String unLoCode) {
    Location locationFound = LocationSample.findByUnLoCode(unLoCode)
    return locationFound
  }
}
