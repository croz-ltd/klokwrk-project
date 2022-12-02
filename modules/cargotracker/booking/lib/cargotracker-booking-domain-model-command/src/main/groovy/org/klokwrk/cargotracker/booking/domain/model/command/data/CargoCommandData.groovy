package org.klokwrk.cargotracker.booking.domain.model.command.data

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerDimensionType
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import static org.hamcrest.Matchers.notNullValue

/**
 * Encapsulates cargo-related properties in CreteBookingOfferCommand.
 * <p/>
 * It is interesting to note the reasons for using CargoCommandData class instead of just using the Cargo value object.
 * <p/>
 * The Cargo value object contains multiple calculated properties, which are populated in the aggregate with the help of additional services injected into the aggregate. If we use the Cargo value
 * object instead of CargoCommandData, we will end up with a partial Cargo instance that has to be recreated when the command arrives in the aggregate. And this will work without any technical issues.
 * <p/>
 * However, to make an explicit distinction between cargo specification and complete cargo instance, we opted to use CargoCommandData. That way, we don't have to care about how to construct a Cargo
 * instance and whether or not we should use some parameters. This is very useful for potential external users (i.e., projection app) of the command API.
 * <p/>
 * Regarding dependencies, CargoCommandData belongs to the command model (commands and aggregates) and can be referenced and used from wherever the command model is allowed to be used.
 */
@KwrkImmutable
@CompileStatic
class CargoCommandData implements PostMapConstructorCheckable {
  Commodity commodity
  ContainerDimensionType containerDimensionType

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(commodity, notNullValue())
    requireMatch(containerDimensionType, notNullValue())
  }
}
