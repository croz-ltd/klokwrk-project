package org.klokwrk.cargotracker.booking.queryside

import groovy.transform.CompileStatic
import groovy.transform.Generated
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Booking query-side application.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = ["org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model"])
@EntityScan(basePackages = ["org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model"])
@CompileStatic
class BookingQuerySideApplication {
  @Generated // Ignore in JaCoCo report as main method is not covered by JaCoCo (probably it is too early for JaCoCo to chip in)
  static void main(String[] args) {
    SpringApplication.run(BookingQuerySideApplication, args)
  }
}
