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

  @Column(nullable = false, columnDefinition="char(36)") String aggregateIdentifier
  @Column(nullable = false) Long aggregateSequenceNumber

  @Column(nullable = false) String originLocation
  @Column(nullable = false) String destinationLocation

  @Column(nullable = false) String inboundChannelName
  @Column(nullable = false) String inboundChannelType
}
