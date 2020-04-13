package net.croz.cargotracker.booking.queryside

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Booking query-side application.
 */
@SpringBootApplication
@CompileStatic
class BookingQuerySideApplication {
  static void main(String[] args) {
    SpringApplication.run(BookingQuerySideApplication, args)
  }
}
