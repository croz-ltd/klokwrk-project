package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler
import org.klokwrk.lib.validation.constraint.UnLoCodeFormatConstraint
import org.klokwrk.lib.validation.group.Level1
import org.klokwrk.lib.validation.group.Level2
import org.klokwrk.lib.validation.group.Level3

import javax.validation.GroupSequence
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

/**
 * DTO encapsulating route specification data pieces gathered from external ports/adapters.
 */
@GroupSequence([RouteSpecificationData, Level1, Level2, Level3])
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class RouteSpecificationData {
  /**
   * Origin (start) location of a cargo.
   * <p/>
   * Not null and not blank when not null, and must be in unLoCode format. After formal validation passes (1st level validation), it also must exist in the location registry (2nd level validation).
   */
  @UnLoCodeFormatConstraint(groups = [Level3])
  @Size(min = 5, max = 5, groups = [Level2])
  @NotBlank(groups = [Level1])
  String originLocation

  /**
   * Destination (end) location of a cargo.
   * <p/>
   * Not null and not blank when not null, and must be in unLoCode format. After formal validation passes (1st level validation) , it also must exist in the location registry (2nd level validation).
   */
  @UnLoCodeFormatConstraint(groups = [Level3])
  @Size(min = 5, max = 5, groups = [Level2])
  @NotBlank(groups = [Level1])
  String destinationLocation
}
