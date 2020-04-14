package net.croz.cargotracker.booking.queryside.rdbms.projection

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = ["net.croz.cargotracker.booking.queryside.rdbms.projection", "net.croz.cargotracker.booking.queryside.rdbms.domain.readmodel"])
@CompileStatic
class BookingQuerySideRdbmsProjectionApplication {
  static void main(String[] args) {
    SpringApplication.run(BookingQuerySideRdbmsProjectionApplication, args)
  }
}
