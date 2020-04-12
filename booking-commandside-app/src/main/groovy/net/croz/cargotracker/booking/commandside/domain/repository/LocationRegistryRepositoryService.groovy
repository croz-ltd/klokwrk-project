package net.croz.cargotracker.booking.commandside.domain.repository

import net.croz.cargotracker.booking.commandside.api.model.Location
import org.springframework.stereotype.Service

@Service
interface LocationRegistryRepositoryService {
  Location findByUnLoCode(String unLoCode)
}
