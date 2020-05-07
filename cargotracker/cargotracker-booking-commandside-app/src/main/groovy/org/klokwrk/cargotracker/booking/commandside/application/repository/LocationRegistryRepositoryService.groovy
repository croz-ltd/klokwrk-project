package org.klokwrk.cargotracker.booking.commandside.application.repository

import org.klokwrk.cargotracker.booking.domain.model.Location
import org.springframework.stereotype.Service

@Service
interface LocationRegistryRepositoryService {
  Location findByUnLoCode(String unLoCode)
}
