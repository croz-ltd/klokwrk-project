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
package org.klokwrk.cargotracking.domain.model.value

import spock.lang.Specification

import static ContainerDimensionType.DIMENSION_ISO_12
import static ContainerDimensionType.DIMENSION_ISO_22
import static ContainerDimensionType.DIMENSION_ISO_42
import static ContainerFeaturesType.FEATURES_ISO_G1
import static ContainerFeaturesType.FEATURES_ISO_R1_STANDARD_REEFER
import static ContainerType.TYPE_ISO_12G1
import static ContainerType.TYPE_ISO_12R1_STANDARD_REEFER
import static ContainerType.TYPE_ISO_22G1
import static ContainerType.TYPE_ISO_22R1_STANDARD_REEFER
import static ContainerType.TYPE_ISO_42G1
import static ContainerType.TYPE_ISO_42R1_STANDARD_REEFER

class ContainerTypeSpecification extends Specification {
  void "should have expected enum size"() {
    // Failure of this test is a signal that we should check places where enumeration is used and update tests and switch/if/else statements
    expect:
    ContainerType.values().size() == 6
  }

  void "should have unique combination of dimensions and features across all enum values"() {
    given:
    ContainerType examinedContainerType = containerTypeParam as ContainerType

    when:
    Boolean isSameCombinationFound = ContainerType
        .values()
        .findAll({ ContainerType containerType -> containerType != examinedContainerType })
        .find({ ContainerType containerType -> containerType.dimensionType == examinedContainerType.dimensionType && containerType.featuresType == examinedContainerType.featuresType })

    then:
    !isSameCombinationFound

    where:
    containerTypeParam << ContainerType.values()
  }

  void "find method should work as expected"() {
    when:
    ContainerType containerTypeResult = ContainerType.find(containerDimensionTypeParam, containerFeaturesTypeParam)

    then:
    containerTypeResult == containerTypeResultParam

    where:
    containerDimensionTypeParam | containerFeaturesTypeParam      | containerTypeResultParam
    null                        | null                            | null
    DIMENSION_ISO_22            | null                            | null
    null                        | FEATURES_ISO_G1                 | null
    DIMENSION_ISO_12            | FEATURES_ISO_G1                 | TYPE_ISO_12G1
    DIMENSION_ISO_12            | FEATURES_ISO_R1_STANDARD_REEFER | TYPE_ISO_12R1_STANDARD_REEFER
    DIMENSION_ISO_22            | FEATURES_ISO_G1                 | TYPE_ISO_22G1
    DIMENSION_ISO_22            | FEATURES_ISO_R1_STANDARD_REEFER | TYPE_ISO_22R1_STANDARD_REEFER
    DIMENSION_ISO_42            | FEATURES_ISO_G1                 | TYPE_ISO_42G1
    DIMENSION_ISO_42            | FEATURES_ISO_R1_STANDARD_REEFER | TYPE_ISO_42R1_STANDARD_REEFER
  }
}
