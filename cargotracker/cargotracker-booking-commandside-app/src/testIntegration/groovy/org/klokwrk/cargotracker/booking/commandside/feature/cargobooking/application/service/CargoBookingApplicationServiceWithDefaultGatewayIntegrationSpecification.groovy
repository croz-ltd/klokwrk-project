package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.service

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = ["axon.extension.tracing.enabled = false"])
@ActiveProfiles("testIntegration")
@DirtiesContext // Need this to recreate commandBus in parent test and clear it from added handler interceptors. Didn't succeed with interceptor unregistering.
class CargoBookingApplicationServiceWithDefaultGatewayIntegrationSpecification extends AbstractCargoBookingApplicationServiceIntegrationSpecification {
}
