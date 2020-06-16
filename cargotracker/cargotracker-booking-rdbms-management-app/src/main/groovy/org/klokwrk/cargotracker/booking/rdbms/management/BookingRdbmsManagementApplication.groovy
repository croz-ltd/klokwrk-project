package org.klokwrk.cargotracker.booking.rdbms.management

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@CompileStatic
class BookingRdbmsManagementApplication {
  static void main(String[] args) {
    SpringApplication.run(BookingRdbmsManagementApplication, args)
  }
}
