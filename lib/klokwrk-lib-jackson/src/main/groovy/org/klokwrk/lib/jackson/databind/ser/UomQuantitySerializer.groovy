package org.klokwrk.lib.jackson.databind.ser

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import groovy.transform.CompileStatic
import tech.units.indriya.format.LocalUnitFormat
import tech.units.indriya.unit.UnitDimension

import javax.measure.Dimension
import javax.measure.Quantity
import javax.measure.Unit
import javax.measure.UnitConverter

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
 *     "unit": {
 *       "name":"Kilogram",
 *       "symbol":"kg"
 *     }
 *   }
 * }
 * </pre>
 * Before serialization, and derived UOM units are normalized to the base unit values. The exception are the units of temperature, which are serialized as given, without normalization.
 * For example, a bean instance
 * <pre>
 * MyBean myBean = new MyBean(name: "someName", weight: Quantities.getQuantity(1_234_000, Units.GRAM))
 * </pre>
 * will be serialized as
 * <pre>
 * {
 *   "name":"someName",
 *   "weight":{
 *     "value":1234,
 *     "unit":{
 *       "name":"Kilogram",
 *       "symbol":"kg"
 *     }
 *   }
 * }
 * </pre>
 * On the other hand, a bean instance with temperature quantity
 * <pre>
 * class MyBeanWithTemperatureQuantity {
 *   String name
 *   Quantity<Temperature> temperature
 * }
 *
 * MyBeanWithTemperatureQuantity myBeanWithTemperatureQuantity =
 *     new MyBeanWithTemperatureQuantity(name: "someName", temperature: Quantities.getQuantity(10, Units.CELSIUS))
 * </pre>
 * will be serialized as
 * <pre>
 * {
 *   "name":"someName",
 *   "temperature":{
 *     "value":10,
 *     "unit":{
 *       "name":"Celsius",
 *       "symbol":"&deg;C"
 *     }
 *   }
 * }
 * </pre>
 * A list od derived and base units is provided by the reference implementation of UOM JSR385 API (https://github.com/unitsofmeasurement/indriya).
 */
@CompileStatic
class UomQuantitySerializer extends StdSerializer<Quantity> {
  static final List<Dimension> NOT_NORMALIZED_DIMENSIONS = [UnitDimension.TEMPERATURE]

  UomQuantitySerializer() {
    super(Quantity)
  }

  @Override
  void serialize(Quantity quantity, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    LocalUnitFormat localUnitFormat = LocalUnitFormat.instance

    Unit unitToSerialize = quantity.unit
    Number valueToSerialize = quantity.value

    if (!NOT_NORMALIZED_DIMENSIONS.contains(quantity.unit.dimension)) {
      unitToSerialize = quantity.unit.systemUnit
      UnitConverter unitConverter = quantity.unit.getConverterTo(unitToSerialize)
      valueToSerialize = unitConverter.convert(quantity.value)
    }

    jsonGenerator.with {
      writeStartObject()

      writeFieldName("value")
      writeNumber(valueToSerialize.toString())

      writeFieldName("unit")
      writeStartObject()
      writeStringField("name", unitToSerialize.name)
      writeStringField("symbol", localUnitFormat.format(unitToSerialize))
      writeEndObject()
    }

    if (quantity.scale != Quantity.Scale.ABSOLUTE) {
      jsonGenerator.writeStringField("scale", quantity.scale.name())
    }

    jsonGenerator.writeEndObject()
  }
}
