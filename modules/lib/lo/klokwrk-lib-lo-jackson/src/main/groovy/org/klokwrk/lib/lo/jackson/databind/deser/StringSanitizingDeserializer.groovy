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
package org.klokwrk.lib.lo.jackson.databind.deser

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import groovy.transform.CompileStatic

/**
 * Jackson deserializer which deserializes any empty string as <code>null</code>.
 */
@CompileStatic
class StringSanitizingDeserializer extends StdDeserializer<String> {
  StringSanitizingDeserializer() {
    super(String)
  }

  @Override
  String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
    JsonNode node = jsonParser.readValueAsTree() as JsonNode
    if (node.asText().trim().isEmpty()) {
      return null
    }

    return node.asText()
  }
}
