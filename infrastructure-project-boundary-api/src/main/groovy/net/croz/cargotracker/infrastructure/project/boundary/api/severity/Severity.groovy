package net.croz.cargotracker.infrastructure.project.boundary.api.severity

import groovy.transform.CompileStatic

/**
 * Enumerates severity levels for response messages from the domain.
 */
@CompileStatic
enum Severity {
  INFO, WARNING, ERROR
}
