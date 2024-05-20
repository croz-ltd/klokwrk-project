/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.projection

import groovy.transform.CompileStatic
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.in.AdapterInViolationClass
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.out.AdapterOutViolationClass
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.port.in.ApplicationPortInViolationInterface
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.port.out.ApplicationPortOutViolationInterface
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.service.ApplicationServiceViolationClass
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.aggregate.DomainAggregateViolationClass
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.command.DomainCommandViolationClass
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.service.DomainServiceViolationClass

@SuppressWarnings('unused')
@CompileStatic
class AdapterProjectionViolationClass {
  DomainCommandViolationClass domainCommandViolationClass = new DomainCommandViolationClass()
  DomainServiceViolationClass domainServiceViolationClass = new DomainServiceViolationClass()
  DomainAggregateViolationClass domainAggregateViolationClass = new DomainAggregateViolationClass()

  ApplicationPortInViolationInterface applicationPortInViolationInterface
  ApplicationPortOutViolationInterface applicationPortOutViolationInterface
  ApplicationServiceViolationClass applicationServiceViolationClass = new ApplicationServiceViolationClass()

  AdapterInViolationClass adapterInViolationClass = new AdapterInViolationClass()
  AdapterOutViolationClass adapterOutViolationClass = new AdapterOutViolationClass()

  AdapterProjectionViolationClass() {
  }

  AdapterProjectionViolationClass(ApplicationPortInViolationInterface applicationPortInViolationInterface, ApplicationPortOutViolationInterface applicationPortOutViolationInterface) {
    this.applicationPortInViolationInterface = applicationPortInViolationInterface
    this.applicationPortOutViolationInterface = applicationPortOutViolationInterface
  }
}
