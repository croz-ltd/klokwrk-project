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
package org.klokwrk.cargotracker.booking.queryside

import groovy.transform.CompileStatic
import groovy.transform.Generated
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Booking query-side application.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = ["org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model", "org.klokwrk.lib.springframework.data.jpa.repository.hibernate"])
@EntityScan(basePackages = ["org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model"])
@CompileStatic
class BookingQuerySideApplication {
  // Generated annotation ignores main method in JaCoCo report as main method is not covered by JaCoCo (probably it is too early for JaCoCo to chip in)
  @Generated
  static void main(String[] args) {
    SpringApplication.run(BookingQuerySideApplication, args)
  }
}
