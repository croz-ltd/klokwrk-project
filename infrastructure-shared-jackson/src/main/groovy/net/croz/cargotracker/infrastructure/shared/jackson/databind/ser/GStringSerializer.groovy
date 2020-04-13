package net.croz.cargotracker.infrastructure.shared.jackson.databind.ser

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import groovy.transform.CompileStatic

/**
 * Jackson serializer which serializes Groovy's <code>GString</code> type by converting it into a <code>String</code>.
 */
@CompileStatic
class GStringSerializer extends StdSerializer<GString> {
  GStringSerializer() {
    super(GString)
  }

  @Override
  void serialize(GString value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
    jsonGenerator.writeString(value.toString())
  }
}
