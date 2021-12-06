/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.feature.cargoinfo.adapter.out.persistence

import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryHandler
import org.klokwrk.cargotracker.booking.queryside.feature.cargoinfo.application.port.in.CargoSummaryQueryRequest
import org.klokwrk.cargotracker.booking.queryside.feature.cargoinfo.application.port.in.CargoSummaryQueryResponse
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model.CargoSummaryJpaEntity
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model.CargoSummaryJpaRepository
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo
import org.springframework.stereotype.Service

/**
 * Implements cargo summary query handling.
 * <p/>
 * Do note that there is no point for marking this class/method with <code>Transactional</code> annotations. Transaction is under control of Axon's <code>SpringTransactionManager</code> which does
 * not supports <code>Transactional</code> annotation. Rather <code>SpringTransactionManager</code> uses a single <code>TransactionDefinition</code> for all its message handlers.
 * <p/>
 * This is unfortunate since we are loosing capability to specify transaction attributes per class or method, and readOnly attribute will be quite nice to have. Fortunately, since this (and others)
 * query handler runs in standalone application, we can resolve this by configuring "global" Axon's transaction definition to be read-only.
 * <p/>
 * For more information and some resources, take a look at <code>axonTransactionManager</code> bean in <code>SpringBootConfig</code> class.
 */
@Service
@CompileStatic
class CargoSummaryQueryHandlerService {
  private final CargoSummaryJpaRepository cargoSummaryJpaRepository

  CargoSummaryQueryHandlerService(CargoSummaryJpaRepository cargoSummaryJpaRepository) {
    this.cargoSummaryJpaRepository = cargoSummaryJpaRepository
  }

  @QueryHandler
  CargoSummaryQueryResponse handleCargoSummaryQueryRequest(CargoSummaryQueryRequest cargoSummaryQueryRequest) {
    CargoSummaryJpaEntity cargoSummaryJpaEntity = cargoSummaryJpaRepository.findByCargoIdentifier(cargoSummaryQueryRequest.cargoIdentifier)

    if (!cargoSummaryJpaEntity) {
      throw new QueryException(ViolationInfo.NOT_FOUND)
    }

    return new CargoSummaryQueryResponse(cargoSummaryJpaEntity.properties)
  }
}
