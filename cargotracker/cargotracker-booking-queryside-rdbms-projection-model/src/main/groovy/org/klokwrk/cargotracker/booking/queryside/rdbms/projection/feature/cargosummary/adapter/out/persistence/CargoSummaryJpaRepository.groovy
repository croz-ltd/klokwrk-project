package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.feature.cargosummary.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface CargoSummaryJpaRepository extends JpaRepository<CargoSummaryJpaEntity, Long> {
  CargoSummaryJpaEntity findByAggregateIdentifier(String aggregateIdentifier)
}
