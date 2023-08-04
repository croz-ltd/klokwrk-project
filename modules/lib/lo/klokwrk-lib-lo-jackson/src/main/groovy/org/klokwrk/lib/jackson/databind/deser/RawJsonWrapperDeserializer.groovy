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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import groovy.transform.CompileStatic
import org.klokwrk.lib.xlang.groovy.base.json.RawJsonWrapper

@CompileStatic
class RawJsonWrapperDeserializer extends StdDeserializer<RawJsonWrapper> {
  RawJsonWrapperDeserializer() {
    super(RawJsonWrapper)
  }

  @SuppressWarnings("CodeNarc.Instanceof")
  @Override
  RawJsonWrapper deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
    int rawJsonWrapperValueStart
    int rawJsonWrapperValueEnd
    String rawJsonValue

    if (jsonParser.currentLocation().contentReference().rawContent instanceof byte[]) {
      rawJsonWrapperValueStart = jsonParser.currentLocation().byteOffset as int
      jsonParser.skipChildren()
      rawJsonWrapperValueEnd = jsonParser.currentLocation.byteOffset as int
      rawJsonValue = new String(jsonParser.currentLocation.contentReference().rawContent as byte[], rawJsonWrapperValueStart - 1, rawJsonWrapperValueEnd - rawJsonWrapperValueStart + 1)
    }
    else { // instance of char[]
      rawJsonWrapperValueStart = jsonParser.currentLocation().charOffset as int
      jsonParser.skipChildren()
      rawJsonWrapperValueEnd = jsonParser.currentLocation.charOffset as int

      String parentJson = jsonParser.currentLocation.contentReference().rawContent
      rawJsonValue = parentJson.substring(rawJsonWrapperValueStart - 1, rawJsonWrapperValueEnd)
    }

    RawJsonWrapper rawJsonWrapper = new RawJsonWrapper(rawJson: rawJsonValue)

    return rawJsonWrapper
  }
}
