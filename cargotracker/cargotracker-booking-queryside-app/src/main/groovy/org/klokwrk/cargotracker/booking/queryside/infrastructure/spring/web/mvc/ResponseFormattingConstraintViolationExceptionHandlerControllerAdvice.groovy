package org.klokwrk.cargotracker.booking.queryside.infrastructure.spring.web.mvc

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.web.spring.mvc.ResponseFormattingConstraintViolationExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.ControllerAdvice

/**
 * Controller advice component extending from non-component {@link ResponseFormattingConstraintViolationExceptionHandler} parent.
 * <p/>
 * The main reason for this class is to create a library-like {@link ResponseFormattingConstraintViolationExceptionHandler} class that is not automatically (via component scanning) included in the
 * Spring context. Otherwise (since {@link ControllerAdvice} is also annotated with Component), the application developer will not have a clear choice not to activate customized and opinionated
 * exception handling from {@link ResponseFormattingConstraintViolationExceptionHandler}.
 * <p/>
 * In our case, {@code ResponseFormattingConstraintViolationExceptionHandlerControllerAdvice} is picked-up by auto-scanning from {@code BookingQuerySideApplication}.
 * <p/>
 * The order of this advice is {@code 100}.
 */
@Order(100)
@ControllerAdvice
@CompileStatic
class ResponseFormattingConstraintViolationExceptionHandlerControllerAdvice extends ResponseFormattingConstraintViolationExceptionHandler {
}
