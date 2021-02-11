package org.klokwrk.cargotracker.booking.commandside.infrastructure.spring.web.mvc

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.web.spring.mvc.ResponseFormattingUnknownExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.ControllerAdvice

/**
 * Controller advice component extending from non-component {@link ResponseFormattingUnknownExceptionHandler} parent.
 * <p/>
 * Main reason for this class it to be able to create a library-like {@link ResponseFormattingUnknownExceptionHandler} class which is not automatically (via component scanning) included in Spring
 * context. Otherwise (since {@link ControllerAdvice} is also annotated with Component), application developer will not have a clear choice to not activate customized and opinionated exception
 * handling from {@link ResponseFormattingUnknownExceptionHandler}.
 * <p/>
 * In our case, <code>ResponseFormattingUnknownExceptionHandler</code> is picked-up by auto-scanning from <code>BookingCommandSideApplication</code>.
 * <p/>
 * Order of this advice is <code>1000</code>.
 */
@Order(1000)
@ControllerAdvice
@CompileStatic
class ResponseFormattingUnknownExceptionHandlerControllerAdvice extends ResponseFormattingUnknownExceptionHandler {
}
