/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.port.out

import groovy.transform.CompileStatic
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.in.AdapterInViolationClass
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.out.AdapterOutViolationClass
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.projection.AdapterProjectionViolationClass
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.port.in.ApplicationPortInViolationInterface
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.service.ApplicationServiceViolationClass
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.aggregate.DomainAggregateViolationClass
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.command.DomainCommandViolationClass
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.event.DomainEventViolationClass
import org.klokwrk.lib.lo.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.service.DomainServiceViolationClass

@SuppressWarnings('unused')
@CompileStatic
interface ApplicationPortOutViolationInterface {
  void accessDomainEventClass(DomainEventViolationClass domainEventViolationClass)
  void accessDomainCommandClass(DomainCommandViolationClass domainCommandViolationClass)
  void accessDomainServiceClass(DomainServiceViolationClass domainServiceViolationClass)
  void accessDomainAggregateClass(DomainAggregateViolationClass domainAggregateViolationClass)

  void accessApplicationPortIn(ApplicationPortInViolationInterface applicationPortInViolationInterface)
  void accessApplicationService(ApplicationServiceViolationClass applicationServiceViolationClass)

  void accessAdapterIn(AdapterInViolationClass adapterInViolationClass)
  void accessAdapterOut(AdapterOutViolationClass adapterOutViolationClass)
  void accessAdapterProjection(AdapterProjectionViolationClass adapterProjectionViolationClass)
}
