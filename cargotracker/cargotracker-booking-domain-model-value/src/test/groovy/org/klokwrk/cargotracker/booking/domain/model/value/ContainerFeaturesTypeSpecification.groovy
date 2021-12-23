package org.klokwrk.cargotracker.booking.domain.model.value

import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerFeaturesType.FEATURES_ISO_G1
import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerFeaturesType.FEATURES_ISO_R1_STANDARD_REEFER

class ContainerFeaturesTypeSpecification extends Specification {
  void "should have expected enum size"() {
    expect:
    ContainerFeaturesType.values().size() == 2
  }

  void "should have expected container temperature ranges"() {
    expect:
    FEATURES_ISO_G1.containerTemperatureRange.minimum == null
    FEATURES_ISO_G1.containerTemperatureRange.maximum == null

    FEATURES_ISO_R1_STANDARD_REEFER.containerTemperatureRange.minimum == Quantities.getQuantity(-30, Units.CELSIUS)
    FEATURES_ISO_R1_STANDARD_REEFER.containerTemperatureRange.maximum == Quantities.getQuantity(30, Units.CELSIUS)
  }

  void "isContainerTemperatureControlled() method should work as expected"() {
    given:
    ContainerFeaturesType containerFeaturesType = containerFeturesTypeParam

    when:
    Boolean result = containerFeaturesType.isContainerTemperatureControlled()

    then:
    result == resultParam

    where:
    containerFeturesTypeParam       | resultParam
    FEATURES_ISO_G1                 | false
    FEATURES_ISO_R1_STANDARD_REEFER | true
  }
}
