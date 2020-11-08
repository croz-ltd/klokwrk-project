/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.in

import groovy.transform.CompileStatic
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.out.AdapterOutViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.projection.AdapterProjectionViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.port.out.ApplicationPortOutViolationInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.service.ApplicationServiceViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.aggregate.DomainAggregateViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.command.DomainCommandViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.event.DomainEventViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.DomainModelViolationClass

@SuppressWarnings('unused')
@CompileStatic
class AdapterInViolationClass {
  DomainModelViolationClass domainModelViolationClass = new DomainModelViolationClass()
  DomainEventViolationClass domainEventViolationClass = new DomainEventViolationClass()
  DomainCommandViolationClass domainCommandViolationClass = new DomainCommandViolationClass()
  DomainAggregateViolationClass domainAggregateViolationClass = new DomainAggregateViolationClass()

  ApplicationPortOutViolationInterface applicationPortOutViolationInterface
  ApplicationServiceViolationClass applicationServiceViolationClass = new ApplicationServiceViolationClass()

  AdapterOutViolationClass adapterOutViolationClass = new AdapterOutViolationClass()
  AdapterProjectionViolationClass adapterProjectionViolationClass = new AdapterProjectionViolationClass()

  AdapterInViolationClass() {
  }

  AdapterInViolationClass(ApplicationPortOutViolationInterface applicationPortOutViolationInterface) {
    this.applicationPortOutViolationInterface = applicationPortOutViolationInterface
  }
}
