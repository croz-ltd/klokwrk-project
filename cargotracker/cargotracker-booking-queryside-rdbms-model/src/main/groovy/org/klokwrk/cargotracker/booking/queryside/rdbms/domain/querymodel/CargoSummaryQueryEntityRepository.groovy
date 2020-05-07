package org.klokwrk.cargotracker.booking.queryside.rdbms.domain.querymodel

import org.springframework.data.jpa.repository.JpaRepository

interface CargoSummaryQueryEntityRepository extends JpaRepository<CargoSummaryQueryEntity, Long> {
  CargoSummaryQueryEntity findByAggregateIdentifier(String aggregateIdentifier)
}
