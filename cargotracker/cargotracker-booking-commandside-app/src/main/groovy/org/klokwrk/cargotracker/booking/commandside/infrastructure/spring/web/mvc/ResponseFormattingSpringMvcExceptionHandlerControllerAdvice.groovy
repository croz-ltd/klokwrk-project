/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.klokwrk.cargotracker.booking.commandside.infrastructure.spring.web.mvc

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
 * In our case, <code>ResponseFormattingSpringMvcExceptionHandlerControllerAdvice</code> is picked-up by auto-scanning from <code>BookingCommandSideApplication</code>.
 * <p/>
 * Order of this advice is <code>500</code>.
 */
@Order(500)
@ControllerAdvice
@CompileStatic
class ResponseFormattingSpringMvcExceptionHandlerControllerAdvice extends ResponseFormattingSpringMvcExceptionHandler {
}
