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
package org.klokwrk.cargotracking.test.support.assertion

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * The class whose instances are responsible for asserting {@code payload.pageInfo} part in the response map.
 * <p/>
 * For more details, take a look at {@link PageablePayloadAssertion}.
 *
 * @see PageablePayloadAssertion
 */
@CompileStatic
class PageInfoAssertion {
  private final Map pageInfoMap

  PageInfoAssertion(Map pageInfoMap) {
    this.pageInfoMap = pageInfoMap
  }

  PageInfoAssertion isSuccessful() {
    assert pageInfoMap instanceof Map
    pageInfoMap.with {
      assert size() == 8

      assert pageOrdinal instanceof Integer
      assert (pageOrdinal as Integer) >= 0

      assert pageElementsCount instanceof Integer
      assert (pageElementsCount as Integer) >= 1

      assert first instanceof Boolean
      assert last instanceof Boolean

      assert totalPagesCount instanceof Integer
      assert (totalPagesCount as Integer) >= 1

      assert totalElementsCount instanceof Integer
      assert (totalElementsCount as Integer) >= 1

      assert (totalElementsCount as Integer) >= (pageElementsCount as Integer)

      assert requestedPageRequirement instanceof Map
      (requestedPageRequirement as Map).with {
        assert size() == 2

        assert ordinal instanceof Integer
        assert (ordinal as Integer) >= 0

        assert size instanceof Integer
        assert (size as Integer) >= 1
      }

      assert requestedSortRequirementList instanceof List
      (requestedSortRequirementList as List<Map>).with {
        assert size() >= 1
        each {
          assert it instanceof Map
          // "each" closure has owner_first as delegate strategy. Therefore, to not repeat "it" for every map property, I'm using "with" here.
          it.with {
            assert size() == 2
            assert propertyName
            assert direction == "DESC" || direction == "ASC"
          }
        }
      }
    }

    return this
  }

  PageInfoAssertion isSuccessfulForEmptyPageContent() {
    assert pageInfoMap instanceof Map
    pageInfoMap.with {
      assert size() == 8
      assert pageOrdinal == 0
      assert pageElementsCount == 0
      assert first == true
      assert last == true
      assert totalPagesCount == 0
      assert totalElementsCount == 0

      assert requestedPageRequirement instanceof Map
      (requestedPageRequirement as Map).with {
        assert size() == 2

        assert ordinal instanceof Integer
        assert (ordinal as Integer) >= 0

        assert size instanceof Integer
        assert (size as Integer) >= 1
      }

      assert requestedSortRequirementList instanceof List
      (requestedSortRequirementList as List<Map>).with {
        assert size() >= 1
        each {
          assert it instanceof Map
          it.with {
            assert size() == 2
            assert propertyName
            assert direction == "DESC" || direction == "ASC"
          }
        }
      }
    }

    return this
  }

  PageInfoAssertion isFirstPageWithDefaults() {
    assert pageInfoMap instanceof Map
    pageInfoMap.with {
      assert size() == 8
      assert pageOrdinal == 0

      assert pageElementsCount instanceof Integer
      assert (pageElementsCount as Integer) >= 1

      assert first == true
      assert last instanceof Boolean

      assert totalPagesCount instanceof Integer
      assert (totalPagesCount as Integer) >= 1

      assert totalElementsCount instanceof Integer
      assert (totalElementsCount as Integer) >= 1

      assert (totalElementsCount as Integer) >= (pageElementsCount as Integer)

      assert requestedPageRequirement instanceof Map
      (requestedPageRequirement as Map).with {
        assert size() == 2
        assert ordinal == 0
        assert size == 25
      }

      assert requestedSortRequirementList instanceof List
      (requestedSortRequirementList as List<Map>).with {
        assert size() == 1
        it[0].with {
          assert it instanceof Map
          assert size() == 2
          assert propertyName == "lastEventRecordedAt"
          assert direction == "DESC"
        }
      }
    }

    return this
  }

  PageInfoAssertion hasPageOrdinal(Long expectedPageOrdinal) {
    assert pageInfoMap.pageOrdinal == expectedPageOrdinal
    return this
  }

  PageInfoAssertion hasPageElementsCount(Long expectedPageElementsCount) {
    assert pageInfoMap.pageElementsCount == expectedPageElementsCount
    return this
  }

  PageInfoAssertion hasPageElementsCountGreaterThenOrEqual(Long comparablePageElementsCount) {
    assert (pageInfoMap.pageElementsCount as Long) >= comparablePageElementsCount
    return this
  }

  PageInfoAssertion hasTotalElementsCount(Long expectedTotalElementsCount) {
    assert pageInfoMap.totalElementsCount == expectedTotalElementsCount
    return this
  }

  PageInfoAssertion hasTotalElementsCountGreaterThanOrEqual(Long comparableTotalElementsCount) {
    assert (pageInfoMap.totalElementsCount as Long) >= comparableTotalElementsCount
    return this
  }

  PageInfoAssertion hasFirstFlagOf(Boolean expectedFirstFlag) {
    assert pageInfoMap.first == expectedFirstFlag
    return this
  }

  PageInfoAssertion hasRequestedPageRequirementThat(
      @DelegatesTo(value = RequestedPageRequirementAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = 'org.klokwrk.cargotracking.test.support.assertion.PageInfoAssertion$RequestedPageRequirementAssertion'
      ) Closure aClosure)
  {
    Object requestedPageRequirementMap = pageInfoMap?.requestedPageRequirement
    assert requestedPageRequirementMap instanceof Map

    RequestedPageRequirementAssertion requestedPageRequirementAssertion = new RequestedPageRequirementAssertion(requestedPageRequirementMap as Map)
    aClosure.resolveStrategy = Closure.DELEGATE_FIRST
    aClosure.delegate = requestedPageRequirementAssertion
    aClosure.call(requestedPageRequirementAssertion)

    return this
  }

  PageInfoAssertion hasRequestedSortRequirementListWithFirstElementThat(
      @DelegatesTo(value = RequestedSortRequirementAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = 'org.klokwrk.cargotracking.test.support.assertion.PageInfoAssertion$RequestedSortRequirementAssertion'
      ) Closure aClosure)
  {
    hasRequestedSortRequirementListWithElementAtIndexThat(0, aClosure)
    return this
  }

  PageInfoAssertion hasRequestedSortRequirementListWithElementAtIndexThat(
      Integer anIndex,
      @DelegatesTo(value = RequestedSortRequirementAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = 'org.klokwrk.cargotracking.test.support.assertion.PageInfoAssertion$RequestedSortRequirementAssertion'
      ) Closure aClosure)
  {
    Object requestedSortRequirementMapAsObject = pageInfoMap?.requestedSortRequirementList
    assert requestedSortRequirementMapAsObject instanceof List

    Map requestedSortRequirementMap = (pageInfoMap.requestedSortRequirementList as List<Map>)[anIndex]
    assert requestedSortRequirementMap instanceof Map

    RequestedSortRequirementAssertion requestedSortRequirementAssertion = new RequestedSortRequirementAssertion(requestedSortRequirementMap)
    aClosure.resolveStrategy = Closure.DELEGATE_FIRST
    aClosure.delegate = requestedSortRequirementAssertion
    aClosure.call(requestedSortRequirementAssertion)

    return this
  }

  static class RequestedPageRequirementAssertion {
    private final Map requestedPageRequirementMap

    RequestedPageRequirementAssertion(Map requestedPageRequirementMap) {
      this.requestedPageRequirementMap = requestedPageRequirementMap
    }

    RequestedPageRequirementAssertion hasOrdinal(Long expectedOrdinal) {
      assert requestedPageRequirementMap.ordinal == expectedOrdinal
      return this
    }

    RequestedPageRequirementAssertion hasSize(Long expectedSize) {
      assert requestedPageRequirementMap.size == expectedSize
      return this
    }
  }

  static class RequestedSortRequirementAssertion {
    private final Map requestedSortRequirementMap

    RequestedSortRequirementAssertion(Map requestedSortRequirementMap) {
      this.requestedSortRequirementMap = requestedSortRequirementMap
    }

    RequestedSortRequirementAssertion hasPropertyName(String expectedPropertyName) {
      assert requestedSortRequirementMap.propertyName == expectedPropertyName
      return this
    }

    RequestedSortRequirementAssertion hasDirection(String expectedDirection) {
      assert requestedSortRequirementMap.direction == expectedDirection
      return this
    }
  }
}
