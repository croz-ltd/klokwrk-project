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
import org.klokwrk.lib.uom.format.KwrkSimpleUnitFormat

import javax.measure.Quantity
import javax.measure.Unit
import javax.measure.format.UnitFormat

/**
 * Jackson serializer for Quantity from Units of Measurements (UOM) JSR385 API (https://github.com/unitsofmeasurement/unit-api).
 * <p/>
 * Suppose we have the following bean as a target object (untyped raw quantities are also supported):
 * <pre>
 * class MyBean {
 *   String name
 *   Quantity&lt;Mass&gt; weight
 * }
 * </pre>
 * For the {@code weight} quantity of 5 kilograms, the bean instance will be serialized as
 * <pre>
 * {
 *   "name": "someName",
 *   "weight": {
 *     "value": 5,
 *     "unitSymbol": "kg"
 *   }
 * }
 * </pre>
 * Here is another example with temperature quantity:
 * <pre>
 * class MyBeanWithTemperatureQuantity {
 *   String name
 *   Quantity&lt;Temperature&gt; temperature
 * }
 *
 * MyBeanWithTemperatureQuantity myBeanWithTemperatureQuantity =
 *     new MyBeanWithTemperatureQuantity(name: "someName", temperature: 10.degC)
 * </pre>
 * will be serialized as
 * <pre>
 * {
 *   "name":"someName",
 *   "temperature":{
 *     "value":10,
 *     "unitSymbol": "&deg;C"
 *   }
 * }
 * </pre>
 */
@CompileStatic
class UomQuantitySerializer extends StdSerializer<Quantity> {
  // Some implementation notes:
  // The initial idea was to have additional elements further describing a unit, like unitName (i.e., Kilogram, Gram, Celsius, etc.) and unitDimension (i.e., Mass, Temperature, Speed, etc.). However,
  // it turned out that not every unit has a name, nor is it possible to map a unit to its dimension with current version of indriya API. However, we can implement all these things by maintaining
  // those mappings in our KwrkSimpleUnitFormat. Currently, there is no real need, so we left tose features out.

  UomQuantitySerializer() {
    super(Quantity)
  }

  @Override
  void serialize(Quantity quantity, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    UnitFormat unitFormatter = KwrkSimpleUnitFormat.instance

    Unit unitToSerialize = quantity.unit
    Number valueToSerialize = quantity.value

    jsonGenerator.with {
      writeStartObject()

      writeFieldName("value")
      writeNumber(valueToSerialize.toString())

      writeStringField("unitSymbol", unitFormatter.format(unitToSerialize))
    }

    jsonGenerator.writeEndObject()
  }
}
