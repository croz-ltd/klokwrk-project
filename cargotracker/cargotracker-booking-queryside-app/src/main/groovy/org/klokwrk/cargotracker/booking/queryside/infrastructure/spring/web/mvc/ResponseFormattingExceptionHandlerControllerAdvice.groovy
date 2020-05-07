package org.klokwrk.cargotracker.booking.queryside.infrastructure.spring.web.mvc

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.web.spring.mvc.ResponseFormattingExceptionHandler
import org.springframework.web.bind.annotation.ControllerAdvice

/**
 * Controller advice component extending non-component {@link ResponseFormattingExceptionHandler} parent.
 * <p/>
 * Main reason for this class it to be able to create a library-like {@link ResponseFormattingExceptionHandler} class which is not
 * automatically (via component scanning) included in Spring context. Otherwise (since {@link ControllerAdvice} is also annotated with Component), application developer will not have a clear choice
 * to not activate customized and opinionated exception handling from {@link ResponseFormattingExceptionHandler}.
 * <p/>
 * In our case, <code>ResponseFormattingExceptionHandlerControllerAdvice</code> is picked-up by auto-scanning from <code>BookingQuerySideApplication</code>.
 */
@ControllerAdvice
@CompileStatic
class ResponseFormattingExceptionHandlerControllerAdvice extends ResponseFormattingExceptionHandler {
}
