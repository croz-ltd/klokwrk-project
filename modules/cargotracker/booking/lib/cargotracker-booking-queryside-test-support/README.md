# cargotracker-booking-queryside-test-support

Module `cargotracker-booking-queryside-test-support` defines helper classes for easier writing of unit, integration, and component tests related to the `queryside` of the `cargotracker-booking`
subdomain.

Those helper classes include:
- factories for creating Docker containers used by tests that leverage the Testcontainers library
- helpers for asserting database state
- helpers for creating Axon domain event messages
