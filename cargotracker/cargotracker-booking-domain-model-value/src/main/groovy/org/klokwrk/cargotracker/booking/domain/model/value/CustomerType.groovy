package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic

/**
 * Enumerates Customer types.
 * <p/>
 * Available Customer types are:
 * <ul>
 *   <li><b>ANONYMOUS</b>: not logged in customer. Does not have any benefits, i.e., like special booking discounts.</li>
 *   <li><b>STANDARD</b>: logged in customer. Usually does not have any benefits, i.e., like special booking discounts. It is equivalent to the anonymous, but logged in the system.</li>
 *   <li><b>GOLD</b>: logged in customer. Usually has some benefits like smaller discounts.</li>
 *   <li><b>PLATINUM</b>: logged in customer. It has benefits like larger discounts.</li>
 * </ul>
 */
@CompileStatic
enum CustomerType {
  ANONYMOUS,
  STANDARD,
  GOLD,
  PLATINUM
}
