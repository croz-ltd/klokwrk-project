package org.klokwrk.cargotracker.booking.commandside

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Booking command-side application.
 */
@SpringBootApplication
@CompileStatic
class BookingCommandSideApplication {
  static void main(String[] args) {
    SpringApplication.run(BookingCommandSideApplication, args)
  }
}
