package org.klokwrk.cargotracker.booking.domain.model.service

import spock.lang.Specification
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass
import java.math.RoundingMode

import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerType.TYPE_ISO_12G1
import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerType.TYPE_ISO_12R1_STANDARD_REEFER
import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerType.TYPE_ISO_22G1
import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerType.TYPE_ISO_22R1_STANDARD_REEFER
import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerType.TYPE_ISO_42G1
import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerType.TYPE_ISO_42R1_STANDARD_REEFER

class PercentBasedMaxAllowedWeightPerContainerPolicySpecification extends Specification {
  void "constructor default params should work as expected"() {
    when:
    PercentBasedMaxAllowedWeightPerContainerPolicy policy = new PercentBasedMaxAllowedWeightPerContainerPolicy(90)

    then:
    policy.percentOfContainerTypeMaxCommodityWeight == 90
    policy.targetUnitOfMass == Units.KILOGRAM
    policy.roundingMode == RoundingMode.DOWN
  }

  void "constructor should fail for null parameters"() {
    when:
    new PercentBasedMaxAllowedWeightPerContainerPolicy(percentOfContainerTypeMaxCommodityWeightParam, targetUnitOfMassParam, roundingModeParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messageParam)

    where:
    percentOfContainerTypeMaxCommodityWeightParam | targetUnitOfMassParam | roundingModeParam | messageParam
    null                                          | Units.KILOGRAM        | RoundingMode.DOWN | "[item: percentOfContainerTypeMaxCommodityWeight, expected: notNullValue(), actual: null]"
    90                                            | null                  | RoundingMode.DOWN | "[item: targetUnitOfMass, expected: notNullValue(), actual: null]"
    90                                            | Units.KILOGRAM        | null              | "[item: roundingMode, expected: notNullValue(), actual: null]"
  }

  void "constructor should fail for invalid percentage parameter"() {
    when:
    new PercentBasedMaxAllowedWeightPerContainerPolicy(percentOfContainerTypeMaxCommodityWeightParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messageParam)

    where:
    percentOfContainerTypeMaxCommodityWeightParam | messageParam
    0                                             | "[item: percentOfContainerTypeMaxCommodityWeight, expected: greaterThanOrEqualTo(1), actual: 0]"
    101                                           | "[item: percentOfContainerTypeMaxCommodityWeight, expected: lessThanOrEqualTo(100), actual: 101]"
  }

  void "maxAllowedWightPerContainer() method should work as expected for all container types"() {
    given:
    PercentBasedMaxAllowedWeightPerContainerPolicy policy = new PercentBasedMaxAllowedWeightPerContainerPolicy(90)

    when:
    Quantity<Mass> maxAllowedWightPerContainer = policy.maxAllowedWeightPerContainer(containerTypeParam)

    then:
    maxAllowedWightPerContainer.unit == Units.KILOGRAM
    maxAllowedWightPerContainer.value instanceof Integer
    maxAllowedWightPerContainer.value == maxAllowedWightPerContainerValueParam

    where:
    containerTypeParam            | maxAllowedWightPerContainerValueParam
    TYPE_ISO_12G1                 | 8_910
    TYPE_ISO_12R1_STANDARD_REEFER | 8_820
    TYPE_ISO_22G1                 | 19_530
    TYPE_ISO_22R1_STANDARD_REEFER | 19_440
    TYPE_ISO_42G1                 | 23_850
    TYPE_ISO_42R1_STANDARD_REEFER | 22_500
  }
}
