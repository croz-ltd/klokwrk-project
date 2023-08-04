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
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.TextNode
import groovy.transform.CompileStatic
import org.klokwrk.lib.uom.format.KwrkSimpleUnitFormat
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.UnitDimension

import javax.measure.Quantity
import javax.measure.Unit
import javax.measure.format.MeasurementParseException

/**
 * Jackson deserializer for Quantity from Units of Measurements (UOM) JSR385 API (https://github.com/unitsofmeasurement/unit-api).
 * <p/>
 * Suppose we have the following bean as a target object:
 * <pre>
 * class MyBean {
 *   String name
 *   Quantity&lt;Mass&gt; weight
 * }
 * </pre>
 * It has a {@code weight} property with typed {@code Quantity} of {@code Mass} (untyped raw quantity is also supported). For this bean, we can provide deserialization string in the following format:
 * <pre>
 * {
 *   "name": "someName",
 *   "weight": {
 *     "value": 5.5
 *     "unitSymbol": "kg"
 *   }
 * }
 * </pre>
 * The same format is used by the corresponding Quantity serializer - {@code UomQuantitySerializer}.
 * <p/>
 * Any unsupported unit symbol will result in {@code MeasurementParseException} exception. Supported symbols and names are provided by the reference implementation of UOM JSR385
 * API (https://github.com/unitsofmeasurement/indriya).
 */
@CompileStatic
class UomQuantityDeserializer extends StdDeserializer<Quantity> implements ContextualDeserializer {
  private JavaType unitType

  UomQuantityDeserializer() {
    super(Quantity)
  }

  @Override
  JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty property) throws JsonMappingException {
    JavaType wrapperType = property.type
    JavaType unitType = wrapperType.containedType(0)
    UomQuantityDeserializer deserializer = new UomQuantityDeserializer(unitType: unitType)

    return deserializer
  }

  @Override
  Quantity deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
    JsonNode quantityJsonNode = jsonParser.readValueAsTree() as JsonNode

    if (quantityJsonNode.isObject()) {
      Quantity quantity = parseJsonNodeOfQuantity(quantityJsonNode)
      return quantity
    }

    throw new MeasurementParseException("Failed to parse Quantity from '${ quantityJsonNode }'.")
  }

  protected Quantity parseJsonNodeOfQuantity(JsonNode quantityJsonNode) {
    JsonNode valueJsonNode = quantityJsonNode.get("value")
    JsonNode unitSymbolJsonNode = quantityJsonNode.get("unitSymbol")

    Boolean isTheTypeOfValueJsonNodeValid = valueJsonNode.isNumber()
    Boolean isTheTypeOfUnitSymbolJsonNodeValid = unitSymbolJsonNode.isTextual()

    if (isTheTypeOfValueJsonNodeValid && isTheTypeOfUnitSymbolJsonNodeValid) {
      Quantity<? extends Quantity> quantityParsed
      quantityParsed = parseQuantity(valueJsonNode as NumericNode, unitSymbolJsonNode as TextNode, quantityJsonNode)

      return checkQuantityUnitJavaTypeMatching(quantityParsed)
    }

    throw new MeasurementParseException("Failed to parse Quantity from '${ quantityJsonNode }'.")
  }

  protected Quantity parseQuantity(NumericNode valueNumericJsonNode, TextNode unitSymbolTextJsonNode, JsonNode quantityJsonNode) {
    Unit unit
    try {
      unit = KwrkSimpleUnitFormat.instance.parse(unitSymbolTextJsonNode.textValue().trim())
    }
    catch (MeasurementParseException e) {
      throw new MeasurementParseException("Failed to parse Quantity from '${ quantityJsonNode }'. Cause message: ${ e.message }")
    }

    Quantity quantity = Quantities.getQuantity(valueNumericJsonNode.numberValue(), unit)
    return quantity
  }

  protected Quantity checkQuantityUnitJavaTypeMatching(Quantity quantityParsed) {
    if (unitType == null) {
      return quantityParsed
    }

    if (quantityParsed.unit.dimension == UnitDimension.of(unitType.rawClass as Class<Quantity>)) {
      return quantityParsed
    }

    throw new MeasurementParseException("Failed to parse unexpected dimension of a quantity [quantity: $quantityParsed, expected dimension: ${ unitType.rawClass.simpleName }].")
  }
}
