package org.klokwrk.cargotracker.booking.commandside.infrastructure.spring.web.mvc

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.web.spring.mvc.ResponseFormattingExceptionHandler
import org.springframework.web.bind.annotation.ControllerAdvice

@ControllerAdvice
@CompileStatic
class ResponseFormattingExceptionHandlerControllerAdvice extends ResponseFormattingExceptionHandler {
}
