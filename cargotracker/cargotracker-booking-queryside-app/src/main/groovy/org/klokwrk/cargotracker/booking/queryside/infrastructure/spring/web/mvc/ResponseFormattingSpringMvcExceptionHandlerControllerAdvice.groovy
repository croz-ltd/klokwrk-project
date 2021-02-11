package org.klokwrk.cargotracker.booking.queryside.infrastructure.spring.web.mvc

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.web.spring.mvc.ResponseFormattingSpringMvcExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.ControllerAdvice

/**
 * Controller advice component extending from non-component {@link ResponseFormattingSpringMvcExceptionHandler} parent.
 * <p/>
 * Main reason for this class it to be able to create a library-like {@link ResponseFormattingSpringMvcExceptionHandler} class which is not automatically (via component scanning) included in Spring
 * context. Otherwise (since {@link ControllerAdvice} is also annotated with Component), application developer will not have a clear choice to not activate customized and opinionated exception
 * handling from {@link ResponseFormattingSpringMvcExceptionHandler}.
 * <p/>
 * In our case, <code>ResponseFormattingSpringMvcExceptionHandlerControllerAdvice</code> is picked-up by auto-scanning from <code>BookingQuerySideApplication</code>.
 * <p/>
 * Order of this advice is <code>500</code>.
 */
@Order(500)
@ControllerAdvice
@CompileStatic
class ResponseFormattingSpringMvcExceptionHandlerControllerAdvice extends ResponseFormattingSpringMvcExceptionHandler {
}
