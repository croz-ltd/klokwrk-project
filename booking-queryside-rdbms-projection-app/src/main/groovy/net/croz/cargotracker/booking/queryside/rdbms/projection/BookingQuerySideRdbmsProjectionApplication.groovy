package net.croz.cargotracker.booking.queryside.rdbms.projection

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["net.croz.cargotracker.booking.queryside.rdbms.projection", "net.croz.cargotracker.booking.queryside.rdbms.domain.querymodel"])
@EnableJpaRepositories(basePackages = ["net.croz.cargotracker.booking.queryside.rdbms.domain.querymodel"])
@EntityScan(basePackages = ["org.axonframework.eventhandling.tokenstore.jpa", "net.croz.cargotracker.booking.queryside.rdbms.domain.querymodel"])
@CompileStatic
class BookingQuerySideRdbmsProjectionApplication {
  static void main(String[] args) {
    SpringApplication.run(BookingQuerySideRdbmsProjectionApplication, args)
  }
}
