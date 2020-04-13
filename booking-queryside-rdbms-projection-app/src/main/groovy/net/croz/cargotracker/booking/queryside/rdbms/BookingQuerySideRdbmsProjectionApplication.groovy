package net.croz.cargotracker.booking.queryside.rdbms

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@CompileStatic
class BookingQuerySideRdbmsProjectionApplication {
  static void main(String[] args) {
    SpringApplication.run(BookingQuerySideRdbmsProjectionApplication, args)
  }
}
