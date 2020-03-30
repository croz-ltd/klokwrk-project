package net.croz.cargotracker.booking.queryside.domain.readmodel

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator

@Entity
class CargoSummary {
  @Id
  @GeneratedValue(generator = "cargoSummarySequenceGenerator", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "cargoSummarySequenceGenerator", sequenceName = "cargo_summary_sequence", initialValue = 1, allocationSize = 50)
  Long id

  String aggregateIdentifier
  Long aggregateSequenceNumber

  String originLocation
  String destinationLocation
}
