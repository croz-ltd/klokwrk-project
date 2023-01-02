/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.view.infrastructure.spring.web.mvc

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
 * In our case, {@code ResponseFormattingConstraintViolationExceptionHandlerControllerAdvice} is picked-up by auto-scanning from {@code BookingQuerySideViewApplication}.
 * <p/>
 * The order of this advice is {@code 100}.
 */
@Order(100)
@ControllerAdvice
@CompileStatic
class ResponseFormattingConstraintViolationExceptionHandlerControllerAdvice extends ResponseFormattingConstraintViolationExceptionHandler {
}
