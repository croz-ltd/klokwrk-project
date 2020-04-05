package net.croz.cargotracker.infrastructure.shared.jackson.databind.deser

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

/**
 * Jackson deserializer which deserializes any empty string as <code>null</code>.
 */
class StringSanitizingDeserializer extends StdDeserializer<String> {
  StringSanitizingDeserializer() {
    super(String)
  }

  @Override
  String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
    JsonNode node = jsonParser.readValueAsTree()
    if (node.asText().trim().isEmpty()) {
      return null
    }

    node.asText()
  }
}
