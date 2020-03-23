package net.croz.cargotracker.booking.queryside.domain.readmodel

import org.springframework.data.jpa.repository.JpaRepository

interface CargoSummaryRepository extends JpaRepository<CargoSummary, Long> {
  CargoSummary findByAggregateIdentifier(String aggregateIdentifier)
}
