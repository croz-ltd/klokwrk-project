package org.klokwrk.lib.jackson.databind.ser

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import groovy.transform.CompileStatic

import javax.measure.Quantity

@CompileStatic
class UomQuantitySerializer extends StdSerializer<Quantity> {

  UomQuantitySerializer() {
    super(Quantity)
  }

  @Override
  void serialize(Quantity quantity, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeStartObject()
    jsonGenerator.writeFieldName("value")
    jsonGenerator.writeNumber(quantity.value.toString())
    jsonGenerator.writeStringField("unit", quantity.unit.toString())

    if (quantity.scale != Quantity.Scale.ABSOLUTE) {
      jsonGenerator.writeStringField("scale", quantity.scale.name())
    }

    jsonGenerator.writeEndObject()
  }
}
