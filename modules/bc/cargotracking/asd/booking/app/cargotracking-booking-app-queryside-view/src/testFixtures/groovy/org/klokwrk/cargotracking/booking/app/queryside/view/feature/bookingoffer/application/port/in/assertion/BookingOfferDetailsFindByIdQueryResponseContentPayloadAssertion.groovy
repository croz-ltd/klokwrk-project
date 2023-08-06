/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.assertion

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import javax.measure.Quantity
import java.time.Instant

@CompileStatic
class BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion {
  /**
   * Entry point static assertion method for fluent-style top-level API.
   */
  static BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion assertResponseHasPayloadThat(Map responseMap) {
    responseMap.with {
      assert size() == 2
      assert metaData
      assert metaData instanceof Map
      assert payload != null
      assert payload instanceof Map
    }

    return new BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion(responseMap.payload as Map)
  }

  /**
   * Entry point static assertion method for closure-style top-level API.
   */
  static BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion assertResponseHasPayloadThat(
      Map responseMap,
      @DelegatesTo(value = BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion"
      ) Closure aClosure)
  {
    BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion payloadAssertion = assertResponseHasPayloadThat(responseMap)
    aClosure.resolveStrategy = Closure.DELEGATE_FIRST
    aClosure.delegate = payloadAssertion
    aClosure.call(payloadAssertion)

    return payloadAssertion
  }

  private final Map payloadMap

  BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion(Map payloadMap) {
    this.payloadMap = payloadMap
  }

  BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion isEmpty() {
    assert payloadMap.size() == 0
    return this
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion isSuccessful() {
    payloadMap.with {
      assert size() == 9

      assert bookingOfferId

      assert cargos instanceof List
      (cargos as List<Map>).with {
        assert size() >= 1
        each {
          assert it instanceof Map
          it.with {
            assert commodityType
            boolean isDryCommodity = commodityType == "DRY"

            if (isDryCommodity) {
              assert size() == 7
            }
            else {
              assert size() == 8
            }

            assert containerType
            assert containerCount

            assert commodityWeight instanceof Map
            (commodityWeight as Map).with {
              assert value
              assert unitSymbol
            }

            assert containerTeuCount

            assert maxAllowedWeightPerContainer instanceof Map
            (maxAllowedWeightPerContainer as Map).with {
              assert value
              assert unitSymbol
            }

            assert maxRecommendedWeightPerContainer instanceof Map
            (maxRecommendedWeightPerContainer as Map).with {
              assert value
              assert unitSymbol
            }

            if (!isDryCommodity) {
              assert commodityRequestedStorageTemperature instanceof Map
              (commodityRequestedStorageTemperature as Map).with {
                assert value
                assert unitSymbol
              }
            }

            return
          }
        }
      }

      assert customer instanceof Map
      (customer as Map).with {
        assert customerId
        assert customerType
      }

      assert routeSpecification instanceof Map
      (routeSpecification as Map).with {
        assert creationTime
        assert departureEarliestTime
        assert departureLatestTime
        assert arrivalLatestTime

        assert originLocation instanceof Map
        (originLocation as Map).with {
          assert name
          assert unLoCode
          assert countryName

          assert portCapabilities instanceof List
          (portCapabilities as List).with {
            assert size() >= 1
            assert every { it instanceof String }
          }

          assert unLoCodeFunction
          assert unLoCodeCoordinates
        }

        assert destinationLocation instanceof Map
        (destinationLocation as Map).with {
          assert name
          assert unLoCode
          assert countryName

          assert portCapabilities instanceof List
          (portCapabilities as List).with {
            assert size() >= 1
            assert every { it instanceof String }
          }

          assert unLoCodeFunction
          assert unLoCodeCoordinates
        }
      }

      assert totalCommodityWeight instanceof Map
      (totalCommodityWeight as Map).with {
        assert value
        assert unitSymbol
      }

      assert totalContainerTeuCount

      assert firstEventRecordedAt
      assert lastEventRecordedAt
      assert lastEventSequenceNumber != null
    }

    return this
  }

  BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion hasBookingOfferId(String expectedBookingOfferId) {
    assert payloadMap.bookingOfferId == expectedBookingOfferId
    return this
  }

  BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion hasCustomerTypeOfStandard() {
    (payloadMap.customer as Map).with {
      assert customerType == "STANDARD"
    }
    return this
  }

  BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion hasTotalCommodityWeight(Quantity expectedQuantity) {
    (payloadMap.totalCommodityWeight as Map).with {
      assert value == expectedQuantity.value
      assert unitSymbol == expectedQuantity.unit.toString()
    }

    return this
  }

  BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion hasTotalContainerTeuCount(BigDecimal expectedTotalContainerTeuCount) {
    assert payloadMap.totalContainerTeuCount == expectedTotalContainerTeuCount
    return this
  }

  BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion hasEventMetadataOfTheFirstEventWithCorrectTiming(Instant comparableOperationStartTime) {
    payloadMap.with {
      assert Instant.parse(firstEventRecordedAt as String) > comparableOperationStartTime
      assert firstEventRecordedAt == lastEventRecordedAt
      assert lastEventSequenceNumber == 0
    }

    return this
  }

  BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion hasCargosWithFirstCargoThat(
      @DelegatesTo(value = CargoAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = 'org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion$CargoAssertion'
      ) Closure aClosure)
  {
    hasCargosWithCargoAtIndexThat(0, aClosure)
    return this
  }

  BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion hasCargosWithCargoAtIndexThat(
      Integer anIndex,
      @DelegatesTo(value = CargoAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = 'org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion$CargoAssertion'
      ) Closure aClosure)
  {
    Object cargosAsObject = payloadMap.cargos
    assert cargosAsObject instanceof List

    List<Map> cargos = cargosAsObject as List<Map>
    assert !cargos.isEmpty()
    assert cargos[anIndex] instanceof Map

    CargoAssertion cargoAssertion = new CargoAssertion(cargos[anIndex] as Map)
    aClosure.resolveStrategy = Closure.DELEGATE_FIRST
    aClosure.delegate = cargoAssertion
    aClosure.call(cargoAssertion)

    return this
  }

  BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion hasRouteSpecificationThat(
      @DelegatesTo(value = RouteSpecificationAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = 'org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion$RouteSpecificationAssertion'
      ) Closure aClosure)
  {
    Object routeSpecificationMap = payloadMap.routeSpecification
    assert routeSpecificationMap instanceof Map

    RouteSpecificationAssertion requestedPageRequirementAssertion = new RouteSpecificationAssertion(routeSpecificationMap as Map)
    aClosure.resolveStrategy = Closure.DELEGATE_FIRST
    aClosure.delegate = requestedPageRequirementAssertion
    aClosure.call(requestedPageRequirementAssertion)

    return this
  }

  static class CargoAssertion {
    private final Map cargoMap

    CargoAssertion(Map cargoMap) {
      this.cargoMap = cargoMap
    }

    CargoAssertion isDryDefaultCargo() {
      cargoMap.with {
        assert size() == 7
        assert commodityType == "DRY"
        assert containerType == "TYPE_ISO_22G1"
        assert containerCount == 1
        assert containerTeuCount == 1.0G

        assert commodityWeight instanceof Map
        (commodityWeight as Map).with {
          assert value == 1000
          assert unitSymbol == "kg"
        }

        assert maxAllowedWeightPerContainer instanceof Map

        assert maxRecommendedWeightPerContainer instanceof Map
        (maxRecommendedWeightPerContainer as Map).with {
          assert value == 1000
          assert unitSymbol == "kg"
        }
      }
      return this
    }

    CargoAssertion hasMaxAllowedWeightPerContainer(Quantity expectedQuantity) {
      assert cargoMap.maxAllowedWeightPerContainer instanceof Map
      (cargoMap.maxAllowedWeightPerContainer as Map).with {
        assert value == expectedQuantity.value
        assert unitSymbol == expectedQuantity.unit.toString()
      }

      return this
    }
  }

  static class RouteSpecificationAssertion {
    private final Map routeSpecificationMap

    RouteSpecificationAssertion(Map routeSpecificationMap) {
      this.routeSpecificationMap = routeSpecificationMap
    }

    RouteSpecificationAssertion hasCreationTimeGreaterThan(Instant comparableInstant) {
      assert Instant.parse(routeSpecificationMap.creationTime as String) > comparableInstant
      return this
    }

    RouteSpecificationAssertion hasDepartureEarliestTime(Instant expectedDepartureEarliestTime) {
      assert Instant.parse(routeSpecificationMap.departureEarliestTime as String) == expectedDepartureEarliestTime
      return this
    }

    RouteSpecificationAssertion hasDepartureLatestTime(Instant expectedDepartureLatestTime) {
      assert Instant.parse(routeSpecificationMap.departureLatestTime as String) == expectedDepartureLatestTime
      return this
    }

    RouteSpecificationAssertion hasArrivalLatestTime(Instant expectedArrivalLatestTime) {
      assert Instant.parse(routeSpecificationMap.arrivalLatestTime as String) == expectedArrivalLatestTime
      return this
    }

    RouteSpecificationAssertion hasOriginLocationOfRijeka() {
      assert routeSpecificationMap.originLocation instanceof Map
      (routeSpecificationMap.originLocation as Map).with {
        assert name == "Rijeka"
        assert unLoCode == "HRRJK"
        assert countryName == "Croatia"

        assert portCapabilities instanceof List
        (portCapabilities as List).containsAll(["SEA_PORT", "CONTAINER_PORT"])

        assert unLoCodeFunction == "1234----"
        assert unLoCodeCoordinates == "4520N 01424E"
      }
      return this
    }

    RouteSpecificationAssertion hasDestinationLocationOfRotterdam() {
      assert routeSpecificationMap.destinationLocation instanceof Map
      (routeSpecificationMap.destinationLocation as Map).with {
        assert name == "Rotterdam"
        assert unLoCode == "NLRTM"
        assert countryName == "Netherlands"

        assert portCapabilities instanceof List
        (portCapabilities as List).containsAll(["SEA_PORT", "CONTAINER_PORT"])

        assert unLoCodeFunction == "12345---"
        assert unLoCodeCoordinates == "5155N 00430E"
      }
      return this
    }
  }
}
