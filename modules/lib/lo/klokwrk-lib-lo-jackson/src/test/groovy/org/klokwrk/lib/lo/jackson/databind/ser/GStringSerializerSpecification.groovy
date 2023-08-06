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
package org.klokwrk.lib.lo.jackson.databind.ser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import spock.lang.Specification

class GStringSerializerSpecification extends Specification {
  ObjectMapper objectMapper

  void setup() {
    SimpleModule simpleModule = new SimpleModule()
    simpleModule.addSerializer(GString, new GStringSerializer())

    ObjectMapper objectMapper = new ObjectMapper()
    objectMapper.registerModule(simpleModule)

    this.objectMapper = objectMapper
  }

  void "should serialize GString as a String"() {
    given:
    Closure closure = {
      return "${123} 456"
    }

    Map mapToSerialize = [
        "bla": closure()
    ]

    when:
    String serializedString = objectMapper.writeValueAsString(mapToSerialize)

    then:
    serializedString.contains("123 456")
  }
}
