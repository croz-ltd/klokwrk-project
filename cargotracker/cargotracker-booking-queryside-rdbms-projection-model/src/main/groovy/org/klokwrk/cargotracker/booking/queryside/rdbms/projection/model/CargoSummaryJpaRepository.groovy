package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model

import org.springframework.data.jpa.repository.JpaRepository

interface CargoSummaryJpaRepository extends JpaRepository<CargoSummaryJpaEntity, Long> {
  CargoSummaryJpaEntity findByAggregateIdentifier(String aggregateIdentifier)
}
