package net.croz.cargotracker.booking.queryside.domain.readmodel

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class CargoSummary {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Long id

  String aggregateIdentifier
  Long aggregateSequenceNumber

  String originLocation
  String destinationLocation
}
