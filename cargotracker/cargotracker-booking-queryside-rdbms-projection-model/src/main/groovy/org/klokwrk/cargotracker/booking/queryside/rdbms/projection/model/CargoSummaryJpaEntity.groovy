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
package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@Entity
@CompileStatic
@Table(name = "cargo_summary")
class CargoSummaryJpaEntity {
  @Id
  @GeneratedValue(generator = "cargoSummarySequenceGenerator", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "cargoSummarySequenceGenerator", sequenceName = "cargo_summary_sequence", initialValue = 1, allocationSize = 50)
  Long id

  @Column(nullable = false, columnDefinition="char(36)") String cargoIdentifier
  @Column(nullable = false) String originLocation
  @Column(nullable = false) String destinationLocation

  @Column(nullable = false) Long aggregateVersion
  @Column(nullable = false) String inboundChannelName
  @Column(nullable = false) String inboundChannelType
}
