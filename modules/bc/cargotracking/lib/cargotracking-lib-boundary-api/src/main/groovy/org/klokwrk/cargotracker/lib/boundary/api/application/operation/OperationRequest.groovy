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
package org.klokwrk.cargotracker.lib.boundary.api.application.operation

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.lib.xlang.groovy.base.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkMapConstructorDefaultPostCheck
import org.klokwrk.lib.xlang.groovy.base.transform.options.RelaxedPropertyHandler

import static org.hamcrest.Matchers.notNullValue

/**
 * Defines the basic format of request messages exchanged over domain facade boundary.
 *
 * @see OperationMessage
 */
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor
@KwrkMapConstructorDefaultPostCheck
@CompileStatic
class OperationRequest<P> implements OperationMessage<P, Map<String, ?>>, PostMapConstructorCheckable {
  Map<String, ?> metaData = Collections.emptyMap()
  P payload

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(metaData, notNullValue())
    requireMatch(payload, notNullValue())
  }
}
