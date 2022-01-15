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
package org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.model.aggregate

import groovy.transform.CompileStatic
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.application.port.out.ApplicationPortOutInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.model.command.DomainCommandClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.model.event.DomainEventClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.model.service.DomainServiceClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.model.value.DomainModelValueClass

@SuppressWarnings('unused')
@CompileStatic
class DomainAggregateClass {
  DomainModelValueClass domainModelValueClass = new DomainModelValueClass()
  DomainEventClass domainEventClass = new DomainEventClass()
  DomainCommandClass domainCommandClass = new DomainCommandClass()
  DomainServiceClass domainServiceClass = new DomainServiceClass()

  ApplicationPortOutInterface applicationPortOutInterface

  DomainAggregateClass() {
  }

  DomainAggregateClass(ApplicationPortOutInterface applicationPortOutInterface) {
    this.applicationPortOutInterface = applicationPortOutInterface
  }
}
