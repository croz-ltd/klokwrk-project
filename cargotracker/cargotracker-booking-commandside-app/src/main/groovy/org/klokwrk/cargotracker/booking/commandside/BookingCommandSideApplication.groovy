package org.klokwrk.cargotracker.booking.commandside

import groovy.transform.CompileStatic
import groovy.transform.Generated
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Booking command-side application.
 */
@SpringBootApplication
@CompileStatic
class BookingCommandSideApplication {
  @Generated // Ignore in JaCoCo report as main method is not covered by JaCoCo (probably it is too early for JaCoCo to chip in)
  static void main(String[] args) {
    SpringApplication.run(BookingCommandSideApplication, args)
  }
}
