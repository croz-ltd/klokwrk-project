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
package org.klokwrk.lib.jackson.databind.ser

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import groovy.transform.CompileStatic
import org.klokwrk.lib.xlang.groovy.base.json.RawJsonWrapper

@CompileStatic
class RawJsonWrapperSerializer extends StdSerializer<RawJsonWrapper> {
  RawJsonWrapperSerializer() {
    super(RawJsonWrapper)
  }

  @Override
  void serialize(RawJsonWrapper value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
    jsonGenerator.writeRawValue(value.rawJson)
  }
}
