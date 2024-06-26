/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.xlang.groovy.base.constructor.support.stub

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import org.klokwrk.lib.xlang.groovy.base.constructor.support.PostMapConstructorCheckable

@MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
@CompileStatic
class TestPerson implements PostMapConstructorCheckable {
  String firstName
  String lastName

  private String fullName
  String getFullName() {
    return fullName
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    assert firstName
    assert firstName.isBlank() == false

    assert lastName
    assert lastName.isBlank() == false
  }

  @Override
  void postMapConstructorPostCheckProcess(Map<String, ?> constructorArguments) {
    fullName = "$firstName $lastName"
  }
}
