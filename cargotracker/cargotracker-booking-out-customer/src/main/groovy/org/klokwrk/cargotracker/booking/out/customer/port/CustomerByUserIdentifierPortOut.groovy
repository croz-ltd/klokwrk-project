package org.klokwrk.cargotracker.booking.out.customer.port

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.value.Customer

/**
 * Outbound port for fetching {@link Customer} from external bounded context.
 */
@CompileStatic
interface CustomerByUserIdentifierPortOut {
  /**
   * Finds {@link Customer} based on its user identifier.
   * <p/>
   * The user identifier can be many things. It can be something like a username or an email for registered users. For anonymous users, it can be an artificial identifier extracted from a cookie or
   * session id.
   * <p/>
   * It is vital to distinguish user identifiers from corresponding CustomerId. CustomerId is stored and remembered for registered users, and it is mapped to the current user identifier. While we can
   * change the user identifier and even delete it, we can not change the CustomerId. In customer management bounded context, CustomerId can be deleted, though. But events containing it cannot be
   * removed, of course.
   * <p/>
   * For anonymous users, CustomerId will probably be constructed on the fly and probably not stored in the customer management bounded context. The corresponding mapping to the user identifier will
   * be short-lived in that case. When an anonymous user deletes its temporary identifier (session closed or cookie deleted), there will be no way to access data related to the corresponding
   * CustomerId.
   * <p/>
   * The implementation of this operation may throw {@link org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException} if customer can not be found. Message key for such exception
   * can be something like '{@code customerByUserIdentifierPortOut.findCustomerByUserIdentifier.notFound}'.
   */
  Customer findCustomerByUserIdentifier(String userIdentifier)
}
