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
package org.klokwrk.lang.groovy.json

import groovy.transform.CompileStatic

/**
 * A wrapper for raw JSON.
 * <p/>
 * Intended to be used with Jackson library, when we need an unchanged JSON string serialization and deserialization.
 * <p/>
 * There are corresponding Jackson serializer and deserializer implementations in klokwrk-lib-jackson module. To understand how this works and if it fits your needs, take a look at the
 * {@code RawJsonWrapperSerializerSpecification} and {@code RawJsonWrapperDeserializerSpecification} tests.
 */
@CompileStatic
class RawJsonWrapper {
  String rawJson
}
