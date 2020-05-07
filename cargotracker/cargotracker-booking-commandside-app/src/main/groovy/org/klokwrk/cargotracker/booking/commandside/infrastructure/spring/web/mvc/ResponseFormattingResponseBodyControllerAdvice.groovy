package org.klokwrk.cargotracker.booking.commandside.infrastructure.spring.web.mvc

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.project.web.spring.mvc.ResponseFormattingResponseBodyAdvice
import org.springframework.web.bind.annotation.ControllerAdvice

@ControllerAdvice
@CompileStatic
class ResponseFormattingResponseBodyControllerAdvice extends ResponseFormattingResponseBodyAdvice {
}
