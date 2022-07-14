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
package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic

/**
 * Enumeration representing a capability (or a feature) of a port.
 */
@CompileStatic
enum PortCapabilityType {
  /**
   * Not a port.
   */
  NO_PORT,

  /**
   * Port at a sea, or closely connected to the sea.
   */
  SEA_PORT,

  /**
   * Port at the river (implies not a sea port).
   */
  RIVER_PORT,

  /**
   * Port with a container terminal.
   */
  CONTAINER_PORT,

  /**
   * Port with a bulk cargo terminal.
   */
  BULK_CARGO_PORT,

  /**
   * Port with an oil terminal.
   */
  OIL_PORT,

  /**
   * Port dealing with human passengers.
   */
  PASSENGER_PORT
}
