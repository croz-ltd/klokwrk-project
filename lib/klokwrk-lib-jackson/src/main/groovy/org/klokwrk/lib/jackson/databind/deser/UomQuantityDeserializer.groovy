package org.klokwrk.lib.jackson.databind.deser

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import groovy.transform.CompileStatic
import tech.units.indriya.format.FormatBehavior
import tech.units.indriya.format.LocalUnitFormat
import tech.units.indriya.format.NumberDelimiterQuantityFormat
import tech.units.indriya.format.UnitStyle
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.UnitDimension
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.Unit
import javax.measure.format.MeasurementParseException

/**
 * Jackson deserializer for Quantity from Units of Measurements (UOM) JSR385 API (https://github.com/unitsofmeasurement/unit-api).
 * <p/>
 * Three different deserialization string formats are supported:
 * <ul>
 *   <li>Quantity as a string</li>
 *   <li>Quantity as an object with unit as a string</li>
 *   <li>Quantity as an object with unit as an object</li>
 * </ul>
 * Suppose we have the following bean as a target object:
 * <pre>
 * class MyBean {
 *   String name
 *   Quantity&lt;Mass&gt; weight
 * }
 * </pre>
 * It has a {@code weight} property with typed {@code Quantity} of {@code Mass} (untyped raw quantity is also supported). For this bean, we can provide deserialization string in three different
 * formats:
 * <p/>
 * <b>Quantity as a string</b>
 * <pre>
 * {
 *   "name": "someName",
 *   "weight": "5 kg"
 * }
 * </pre>
 * <b>Quantity as an object with unit as a string</b>
 * <pre>
 * {
 *   "name": "someName",
 *   "weight": {
 *     "value": 5,
 *     "unit": "kg"
 *   }
 * }
 * </pre>
 * <b>Quantity as an object with unit as an object</b>
 * <pre>
 * {
 *   "name": "someName",
 *   "weight": {
 *     "value": 5,
 *     "unit": {
 *       "name":"Kilogram",
 *       "symbol":"kg"
 *     }
 *   }
 * }
 * </pre>
 *
 * The last format with the unit as an object is a format used by the corresponding Quantity serializer - {@code UomQuantitySerializer}.
 * <p/>
 * When providing unit as an object, only one of {@code "name"} or {@code "symbol"} have to be specified. If both are present, {@code "symbol"} is considered first and {@code "name"} is used only
 * if symbol's value is empty string or {@code null}.
 * <p/>
 * Any unsupported unit symbol or unit name will result in {@code MeasurementParseException} exception for all formats. Supported symbols and names are provided by the reference implementation of
 * UOM JSR385 API (https://github.com/unitsofmeasurement/indriya).
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

    if (quantityJsonNode.isTextual() && (quantityJsonNode.textValue().trim() != "")) {
      Quantity quantity = parseTextualJsonNodeOfQuantity(quantityJsonNode, unitType)
      return quantity
    }

    if (quantityJsonNode.isObject()) {
      Quantity quantity = parseObjectJsonNodeOfQuantity(quantityJsonNode, unitType)
      return quantity
    }

    throw new MeasurementParseException("Failed to parse Quantity from '${ quantityJsonNode }'.")
  }

  protected Quantity parseTextualJsonNodeOfQuantity(JsonNode quantityJsonNode, JavaType quantityUnitJavaType) {
    NumberDelimiterQuantityFormat numberDelimiterQuantityFormat = NumberDelimiterQuantityFormat.getInstance(FormatBehavior.LOCALE_SENSITIVE)
    Quantity quantityParsed
    try {
      quantityParsed = numberDelimiterQuantityFormat.parse(quantityJsonNode.textValue().trim())
    }
    catch (IllegalArgumentException | MeasurementParseException e) {
      throw new MeasurementParseException("Failed to parse Quantity from '${ quantityJsonNode }'. Cause message: ${ e.message }")
    }

    return checkQuantityUnitJavaTypeMatching(quantityUnitJavaType, quantityParsed)
  }

  protected Quantity parseObjectJsonNodeOfQuantity(JsonNode quantityJsonNode, JavaType quantityUnitJavaType) {
    JsonNode valueJsonNode = quantityJsonNode.get("value")
    JsonNode unitJsonNode = quantityJsonNode.get("unit")

    Boolean isValueJsonNodeValid = valueJsonNode.isNumber()
    Boolean isUnitJsonNodeValid = (unitJsonNode.isTextual() && (unitJsonNode.textValue().trim() != "")) || (unitJsonNode.isObject())

    if (isValueJsonNodeValid && isUnitJsonNodeValid) {
      Quantity<? extends Quantity> quantityParsed

      if (unitJsonNode.isTextual()) {
        quantityParsed = parseQuantityWhenUnitJsonNodeIsTextual(valueJsonNode, unitJsonNode, quantityJsonNode)
      }
      else {
        quantityParsed = parseQuantityWhenUnitJsonNodeIsObject(valueJsonNode, unitJsonNode, quantityJsonNode)
      }

      JsonNode scaleJsonNode = quantityJsonNode.get("scale")
      Boolean isScaleJsonNodeValid = (scaleJsonNode != null) && scaleJsonNode.isTextual() && (scaleJsonNode.textValue().trim() != "")
      if (isScaleJsonNodeValid == Boolean.TRUE) {
        String inputScaleName = scaleJsonNode.textValue().trim()
        String correspondingRealScaleName = Quantity
            .Scale
            .values()
            .collect({ Quantity.Scale scale -> scale.name().toUpperCase() })
            .find { String realScaleName -> realScaleName.equalsIgnoreCase(inputScaleName) }

        if (correspondingRealScaleName) {
          quantityParsed = Quantities.getQuantity(quantityParsed.value, quantityParsed.unit, Quantity.Scale.valueOf(correspondingRealScaleName))
        }
      }

      return checkQuantityUnitJavaTypeMatching(quantityUnitJavaType, quantityParsed)
    }

    throw new MeasurementParseException("Failed to parse Quantity from '${ quantityJsonNode }'.")
  }

  protected Quantity parseQuantityWhenUnitJsonNodeIsTextual(JsonNode valueJsonNode, JsonNode unitJsonNode, JsonNode quantityJsonNode) {
    Quantity quantityParsed
    NumberDelimiterQuantityFormat numberDelimiterQuantityFormat = NumberDelimiterQuantityFormat.getInstance(FormatBehavior.LOCALE_SENSITIVE)
    try {
      quantityParsed = numberDelimiterQuantityFormat.parse("${ valueJsonNode.asText().trim() } ${ unitJsonNode.textValue().trim() }")
    }
    catch (IllegalArgumentException | MeasurementParseException e) {
      throw new MeasurementParseException("Failed to parse Quantity from '${ quantityJsonNode }'. Cause message: ${ e.message }")
    }

    return quantityParsed
  }

  protected Quantity parseQuantityWhenUnitJsonNodeIsObject(JsonNode valueJsonNode, JsonNode unitJsonNode, JsonNode quantityJsonNode) {
    JsonNode unitSymbolJsonNode = unitJsonNode.get("symbol")
    JsonNode unitNameJsonNode = unitJsonNode.get("name")

    Unit parsedUnit = null
    if (unitSymbolJsonNode != null && unitSymbolJsonNode.isTextual() && unitSymbolJsonNode.textValue().trim() != "") {
      String unitSymbol = unitSymbolJsonNode.textValue().trim()
      try {
        parsedUnit = LocalUnitFormat.instance.parse(unitSymbol)
      }
      catch (IllegalArgumentException | MeasurementParseException e) {
        throw new MeasurementParseException("Failed to parse Quantity from '${ quantityJsonNode }' because unit symbol '$unitSymbol' is not supported. Cause message: ${ e.message }")
      }
    }

    if (parsedUnit == null && (unitNameJsonNode != null && unitNameJsonNode.isTextual() && unitNameJsonNode.textValue().trim() != "")) {
      String unitName = unitNameJsonNode.textValue().trim()
      parsedUnit = Units.instance.getUnit(unitName, UnitStyle.NAME, true)
      if (parsedUnit == null) {
        throw new MeasurementParseException("Failed to parse Quantity from '${ quantityJsonNode }' because unit name '$unitName' is not supported.")
      }
    }

    if (parsedUnit == null) {
      throw new MeasurementParseException("Failed to parse Quantity from '${ quantityJsonNode }'.")
    }

    Quantity quantityParsed = Quantities.getQuantity(valueJsonNode.asText().trim().toBigDecimal(), parsedUnit)
    return quantityParsed
  }

  protected Quantity checkQuantityUnitJavaTypeMatching(JavaType quantityUnitJavaType, Quantity quantityParsed) {
    if (quantityUnitJavaType == null) {
      return quantityParsed
    }

    if (quantityParsed.unit.dimension == UnitDimension.of(quantityUnitJavaType.rawClass as Class<Quantity>)) {
      return quantityParsed
    }

    throw new MeasurementParseException("Failed to parse unexpected dimension of a quantity [quantity: $quantityParsed, expected dimension: ${ quantityUnitJavaType.rawClass.name }].")
  }
}
