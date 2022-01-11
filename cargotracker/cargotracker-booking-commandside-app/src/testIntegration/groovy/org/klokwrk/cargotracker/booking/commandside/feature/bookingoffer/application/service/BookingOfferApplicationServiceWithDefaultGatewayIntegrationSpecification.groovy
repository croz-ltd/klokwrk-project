/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.service

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

// Notes about DirtiesContext:
// To run integration tests consistently, DirtiesContext annotation must be placed on all test classes in this integration suite.
//
// If this becomes an issue for performance reasons, the most straightforward workaround is to remove all DirtiesContext annotations from all test classes and ignore this "WithDefaultGateway"
// specification. Some alternatives are to group the tests into different JVMs via Gradle configuration or to resolve the actual underlying problem.
//
// The original problem stems from the misbehavior of AxonServerCommandBus in the environment with multiple cached Spring application contexts. It looks like the underlying SimpleCommandBus (used as
// a local segment) sometimes does not register handler interceptors correctly for some reason.
//
// Therefore, the current workaround is to create a new command bus for every test class. As test classes can be run in any order, we need DirtiesContext annotation on every test class.
@DirtiesContext
@SpringBootTest(properties = ["axon.extension.tracing.enabled = false"])
@ActiveProfiles("testIntegration")
class BookingOfferApplicationServiceWithDefaultGatewayIntegrationSpecification extends AbstractBookingOfferApplicationServiceIntegrationSpecification {
}
