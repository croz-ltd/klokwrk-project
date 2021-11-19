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
package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.adapter.in.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoCommandRequest

/**
 * Represents a web request for book cargo operation.
 * <p/>
 * Very often, web interface will use <code>BookCargoCommandRequest</code> directly. However, <code>BookCargoCommandWebRequest</code> can be used for handling additional properties that are only web
 * specific and should be handled in controller before sending the <code>BookCargoCommandRequest</code> into domain application layer.
 */
@CompileStatic
class BookCargoCommandWebRequest extends BookCargoCommandRequest {
}
