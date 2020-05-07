package org.klokwrk.cargotracker.lib.boundary.api.severity

import groovy.transform.CompileStatic

/**
 * Enumerates severity levels for response messages from the domain.
 */
@CompileStatic
enum Severity {
  INFO, WARNING, ERROR
}
