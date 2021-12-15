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
import tech.units.indriya.format.SimpleQuantityFormat
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.UnitDimension

import javax.measure.Quantity
import javax.measure.format.MeasurementParseException

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

  @SuppressWarnings("DuplicatedCode")
  @Override
  Quantity deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
    JsonNode jsonNode = jsonParser.readValueAsTree() as JsonNode

    if (jsonNode.isTextual() && (jsonNode.textValue().trim() != "")) {
      Quantity quantityParsed = SimpleQuantityFormat.instance.parse(jsonNode.textValue().trim())

      if (unitType == null) {
        return quantityParsed
      }

      if (quantityParsed.unit.dimension == UnitDimension.of(unitType.rawClass as Class<Quantity>)) {
        return quantityParsed
      }

      throw new MeasurementParseException("Failed to parse unexpected dimension of a quantity [quantity: $quantityParsed, expected dimension: ${ unitType.rawClass.name }].")
    }

    if (jsonNode.isObject()) {
      JsonNode valueJsonNode = jsonNode.get("value")
      JsonNode unitJsonNode = jsonNode.get("unit")
      JsonNode scaleJsonNode = jsonNode.get("scale")

      Boolean isValueJsonNodeValid = valueJsonNode.isNumber()
      Boolean isUnitJsonNodeValid = unitJsonNode.isTextual() && (unitJsonNode.textValue().trim() != "")

      if (isValueJsonNodeValid && isUnitJsonNodeValid) {
        Quantity<? extends Quantity> quantityParsed = SimpleQuantityFormat.instance.parse("${valueJsonNode.asText().trim()} ${unitJsonNode.textValue().trim()}")

        Boolean isScaleJsonNodeValid = (scaleJsonNode != null) && scaleJsonNode.isTextual() && (scaleJsonNode.textValue().trim() != "")
        if (isScaleJsonNodeValid == Boolean.TRUE) {
          String inputScaleName = scaleJsonNode.textValue().trim()
          String correspondingRealScaleName = Quantity.Scale
              .values()
              .collect({ Quantity.Scale scale -> scale.name().toUpperCase() })
              .find { String realScaleName -> realScaleName.equalsIgnoreCase(inputScaleName) }

          if (correspondingRealScaleName) {
            quantityParsed = Quantities.getQuantity(quantityParsed.value, quantityParsed.unit, Quantity.Scale.valueOf(correspondingRealScaleName))
          }
        }

        if (unitType == null) {
          return quantityParsed
        }

        if (quantityParsed.unit.dimension == UnitDimension.of(unitType.rawClass as Class<Quantity>)) {
          return quantityParsed
        }

        throw new MeasurementParseException("Failed to parse unexpected dimension of a quantity [quantity: $quantityParsed, expected dimension: ${ unitType.rawClass.name }].")
      }
    }

    throw new MeasurementParseException("Failed to parse Quantity from '${ jsonNode }'.")
  }
}
