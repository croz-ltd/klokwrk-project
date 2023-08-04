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
package org.klokwrk.lib.jackson.databind.deser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.klokwrk.lib.xlang.groovy.base.json.RawJsonWrapper
import spock.lang.Specification

class RawJsonWrapperDeserializerSpecification extends Specification {
  static class MyBeanWithRawJson {
    String name
    Integer age
    RawJsonWrapper details
  }

  ObjectMapper objectMapper

  void setup() {
    SimpleModule simpleModule = new SimpleModule()
    simpleModule.addDeserializer(RawJsonWrapper, new RawJsonWrapperDeserializer())

    ObjectMapper objectMapper = new ObjectMapper()
    objectMapper.registerModule(simpleModule)

    this.objectMapper = objectMapper
  }

  void "should work as expected for json string"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "age": 25,
        "details": {"a": "aValue"}
      }
      """

    when:
    MyBeanWithRawJson deserializedMyBean = objectMapper.readValue(stringToDeserialize, MyBeanWithRawJson)

    then:
    deserializedMyBean.name == "someName"
    deserializedMyBean.age == 25
    deserializedMyBean.details.rawJson == /{"a": "aValue"}/
  }

  void "should work as expected for json bytes"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "age": 25,
        "details": {"a": "aValue"}
      }
      """

    when:
    MyBeanWithRawJson deserializedMyBean = objectMapper.readValue(stringToDeserialize.bytes, MyBeanWithRawJson)

    then:
    deserializedMyBean.name == "someName"
    deserializedMyBean.age == 25
    deserializedMyBean.details.rawJson == /{"a": "aValue"}/
  }
}
