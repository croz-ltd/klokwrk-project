package org.klokwrk.cargotracker.booking.commandside.infrastructure.spring.web.mvc

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.web.spring.mvc.ResponseFormattingUnknownExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.ControllerAdvice

@Order(1000)
@ControllerAdvice
@CompileStatic
class ResponseFormattingUnknownExceptionHandlerControllerAdvice extends ResponseFormattingUnknownExceptionHandler {
}
