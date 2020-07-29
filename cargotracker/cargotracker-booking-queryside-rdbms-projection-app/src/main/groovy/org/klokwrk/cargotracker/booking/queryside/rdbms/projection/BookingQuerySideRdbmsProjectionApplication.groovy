package org.klokwrk.cargotracker.booking.queryside.rdbms.projection

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["org.klokwrk.cargotracker.booking.queryside.rdbms.projection.domain.querymodel"])
@EntityScan(basePackages = ["org.axonframework.eventhandling.tokenstore.jpa", "org.klokwrk.cargotracker.booking.queryside.rdbms.projection.domain.querymodel"])
@CompileStatic
class BookingQuerySideRdbmsProjectionApplication {
  static void main(String[] args) {
    SpringApplication.run(BookingQuerySideRdbmsProjectionApplication, args)
  }
}
