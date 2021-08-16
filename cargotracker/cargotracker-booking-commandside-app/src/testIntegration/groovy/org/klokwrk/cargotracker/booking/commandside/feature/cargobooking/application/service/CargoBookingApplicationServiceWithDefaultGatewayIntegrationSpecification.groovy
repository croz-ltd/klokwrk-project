package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.service

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = ["axon.extension.tracing.enabled = false"])
@ActiveProfiles("testIntegration")
class CargoBookingApplicationServiceWithDefaultGatewayIntegrationSpecification extends AbstractCargoBookingApplicationServiceIntegrationSpecification {
}
