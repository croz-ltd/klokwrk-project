package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.out

import org.klokwrk.cargotracker.booking.domain.model.Location
import org.springframework.stereotype.Service

@Service
interface FindLocationPortOut {
  Location findByUnLoCode(String unLoCode)
}
