package net.croz.cargotracker.booking.queryside.rdbms.domain.readmodel

import org.springframework.data.jpa.repository.JpaRepository

interface CargoSummaryQueryEntityRepository extends JpaRepository<CargoSummaryQueryEntity, Long> {
  CargoSummaryQueryEntity findByAggregateIdentifier(String aggregateIdentifier)
}
