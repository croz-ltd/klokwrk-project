package net.croz.cargotracker.booking.queryside.rdbms.domain.querymodel

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler

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
class CargoSummaryQueryEntity {
  @Id
  @GeneratedValue(generator = "cargoSummarySequenceGenerator", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "cargoSummarySequenceGenerator", sequenceName = "cargo_summary_sequence", initialValue = 1, allocationSize = 50)
  Long id

  String aggregateIdentifier
  Long aggregateSequenceNumber

  String originLocation
  String destinationLocation
}
