package org.klokwrk.cargotracker.booking.domain.model.service

import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerDimensionType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

class DefaultCommodityCreatorServiceSpecification extends Specification {
  void "constructor should fail for null parameter"() {
    when:
    new DefaultCommodityCreatorService(null)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("[item: maxAllowedWeightPerContainerPolicy, expected: notNullValue(), actual: null]")
  }

  void "from() method should work as expected"() {
    given:
    MaxAllowedWeightPerContainerPolicy maxAllowedWeightPerContainerPolicy = new PercentBasedMaxAllowedWeightPerContainerPolicy(90)
    DefaultCommodityCreatorService defaultCommodityCreatorService = new DefaultCommodityCreatorService(maxAllowedWeightPerContainerPolicy)

    CommodityInfo commodityInfo = CommodityInfo.make(CommodityType.DRY, 50_000)

    when:
    Commodity commodity = defaultCommodityCreatorService.from(ContainerDimensionType.DIMENSION_ISO_22, commodityInfo)

    then:
    verifyAll(commodity, {
      containerType == ContainerType.TYPE_ISO_22G1
      it.commodityInfo == commodityInfo
      maxAllowedWeightPerContainer == Quantities.getQuantity(22_500, Units.KILOGRAM)
      maxRecommendedWeightPerContainer == Quantities.getQuantity(16_667, Units.KILOGRAM)
      containerCount == 3
      containerTeuCount == 3
    })
  }
}
