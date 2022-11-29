package org.klokwrk.cargotracker.booking.domain.model.event.support

import spock.lang.Specification
import tech.units.indriya.format.AbstractQuantityFormat
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity

class QuantityFormatterSpecification extends Specification {
  void "should format correctly"() {
    given:
    AbstractQuantityFormat formatter = QuantityFormatter.instance

    when:
    String formattedString = formatter.format(Quantities.getQuantity(valueParam, unitParam))

    then:
    formattedString == formattedStringParam

    where:
    valueParam | unitParam      | formattedStringParam
    1          | Units.KILOGRAM | "1 kg"
    1          | Units.CELSIUS  | "1 °C"
  }

  void "should parse correctly"() {
    given:
    AbstractQuantityFormat parser = QuantityFormatter.instance

    when:
    Quantity quantity = parser.parse(inputStringParam)

    then:
    quantity.value as String == inputStringParam.tokenize().first()
    quantity.unit == unitParam

    where:
    inputStringParam | unitParam
    "1 kg"           | Units.KILOGRAM
    "1 °C"           | Units.CELSIUS
  }
}
