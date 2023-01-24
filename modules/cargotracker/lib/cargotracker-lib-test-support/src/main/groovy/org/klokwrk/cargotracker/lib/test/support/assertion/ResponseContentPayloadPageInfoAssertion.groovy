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
package org.klokwrk.cargotracker.lib.test.support.assertion

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * The class whose instances are responsible for asserting {@code payload.pageInfo} part in the response map.
 * <p/>
 * For more details, take a look at {@link ResponseContentPageablePayloadAssertion}.
 *
 * @see ResponseContentPageablePayloadAssertion
 */
@CompileStatic
class ResponseContentPayloadPageInfoAssertion {
  private final Map pageInfoMap

  ResponseContentPayloadPageInfoAssertion(Map pageInfoMap) {
    this.pageInfoMap = pageInfoMap
  }

  ResponseContentPayloadPageInfoAssertion isSuccessful() {
    pageInfoMap.with {
      assert size() == 8
      assert (pageOrdinal as Integer) >= 0
      assert (pageElementsCount as Integer) >= 1
      assert first != null
      assert last != null
      assert (totalPagesCount as Integer) >= 1
      assert (totalElementsCount as Integer) >= 1
      assert (totalElementsCount as Integer) >= (pageElementsCount as Integer)

      (requestedPageRequirement as Map).with {
        assert size() == 2
        assert (ordinal as Integer) >= 0
        assert (size as Integer) >= 1
      }

      (requestedSortRequirementList as List<Map>).with {
        assert size() >= 1
        each {
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

  ResponseContentPayloadPageInfoAssertion isSuccessfulForEmptyPageContent() {
    pageInfoMap.with {
      assert size() == 8
      assert pageOrdinal == 0
      assert pageElementsCount == 0
      assert first == true
      assert last == true
      assert totalPagesCount == 0
      assert totalElementsCount == 0

      (requestedPageRequirement as Map).with {
        assert size() == 2
        assert (ordinal as Integer) >= 0
        assert (size as Integer) >= 1
      }

      (requestedSortRequirementList as List<Map>).with {
        assert size() >= 1
        each {
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

  ResponseContentPayloadPageInfoAssertion isFirstPageWithDefaults() {
    pageInfoMap.with {
      assert size() == 8
      assert pageOrdinal == 0
      assert (pageElementsCount as Integer) >= 1
      assert first == true
      assert last != null
      assert (totalPagesCount as Integer) >= 1
      assert (totalElementsCount as Integer) >= 1
      assert (totalElementsCount as Integer) >= (pageElementsCount as Integer)

      (requestedPageRequirement as Map).with {
        assert size() == 2
        assert ordinal == 0
        assert size == 25
      }

      (requestedSortRequirementList as List<Map>).with {
        assert size() == 1
        it[0].with {
          assert size() == 2
          assert propertyName == "lastEventRecordedAt"
          assert direction == "DESC"
        }
      }
    }

    return this
  }

  @SuppressWarnings("unused")
  ResponseContentPayloadPageInfoAssertion hasPageOrdinal(Long expectedPageOrdinal) {
    assert pageInfoMap.pageOrdinal == expectedPageOrdinal
    return this
  }

  ResponseContentPayloadPageInfoAssertion hasPageElementsCount(Long expectedPageElementsCount) {
    assert pageInfoMap.pageElementsCount == expectedPageElementsCount
    return this
  }

  ResponseContentPayloadPageInfoAssertion hasPageElementsCountGreaterThenOrEqual(Long comparablePageElementsCount) {
    assert (pageInfoMap.pageElementsCount as Long) >= comparablePageElementsCount
    return this
  }

  ResponseContentPayloadPageInfoAssertion hasTotalElementsCount(Long expectedTotalElementsCount) {
    assert pageInfoMap.totalElementsCount == expectedTotalElementsCount
    return this
  }

  @SuppressWarnings("unused")
  ResponseContentPayloadPageInfoAssertion hasTotalElementsCountGreaterThanOrEqual(Long comparableTotalElementsCount) {
    assert (pageInfoMap.totalElementsCount as Long) >= comparableTotalElementsCount
    return this
  }

  @SuppressWarnings("unused")
  ResponseContentPayloadPageInfoAssertion hasFirstFlagOf(Boolean expectedFirstFlag) {
    assert pageInfoMap.first == expectedFirstFlag
    return this
  }

  @SuppressWarnings("unused")
  ResponseContentPayloadPageInfoAssertion hasRequestedPageRequirementThat(
      @DelegatesTo(value = RequestedPageRequirementAssertion, strategy = Closure.DELEGATE_ONLY)
      @ClosureParams(
          value = SimpleType,
          options = 'org.klokwrk.cargotracker.lib.test.support.assertion.ResponseContentPayloadPageInfoAssertion$RequestedPageRequirementAssertion'
      ) Closure aClosure)
  {
    RequestedPageRequirementAssertion requestedPageRequirementAssertion = new RequestedPageRequirementAssertion(pageInfoMap.requestedPageRequirement as Map)
    aClosure.resolveStrategy = Closure.DELEGATE_ONLY
    aClosure.delegate = requestedPageRequirementAssertion
    aClosure.call(requestedPageRequirementAssertion)

    return this
  }

  @SuppressWarnings("unused")
  ResponseContentPayloadPageInfoAssertion hasRequestedSortRequirementListWithFirstElementThat(
      @DelegatesTo(value = RequestedSortRequirementAssertion, strategy = Closure.DELEGATE_ONLY)
      @ClosureParams(
          value = SimpleType,
          options = 'org.klokwrk.cargotracker.lib.test.support.assertion.ResponseContentPayloadPageInfoAssertion$RequestedSortRequirementAssertion'
      ) Closure aClosure)
  {
    hasRequestedSortRequirementListWithElementAtIndexThat(0, aClosure)
    return this
  }

  ResponseContentPayloadPageInfoAssertion hasRequestedSortRequirementListWithElementAtIndexThat(
      Integer anIndex,
      @DelegatesTo(value = RequestedSortRequirementAssertion, strategy = Closure.DELEGATE_ONLY)
      @ClosureParams(
          value = SimpleType,
          options = 'org.klokwrk.cargotracker.lib.test.support.assertion.ResponseContentPayloadPageInfoAssertion$RequestedSortRequirementAssertion'
      ) Closure aClosure)
  {
    Map requestedSortRequirementMap = (pageInfoMap.requestedSortRequirementList as List<Map>)[anIndex]
    assert requestedSortRequirementMap

    RequestedSortRequirementAssertion requestedSortRequirementAssertion = new RequestedSortRequirementAssertion(requestedSortRequirementMap)
    aClosure.resolveStrategy = Closure.DELEGATE_ONLY
    aClosure.delegate = requestedSortRequirementAssertion
    aClosure.call(requestedSortRequirementAssertion)

    return this
  }

  static class RequestedPageRequirementAssertion {
    private final Map requestedPageRequirementMap

    RequestedPageRequirementAssertion(Map requestedPageRequirementMap) {
      this.requestedPageRequirementMap = requestedPageRequirementMap
    }

    @SuppressWarnings("unused")
    RequestedPageRequirementAssertion hasOrdinal(Long expectedOrdinal) {
      assert requestedPageRequirementMap.ordinal == expectedOrdinal
      return this
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    RequestedSortRequirementAssertion hasPropertyName(String expectedPropertyName) {
      assert requestedSortRequirementMap.propertyName == expectedPropertyName
      return this
    }

    @SuppressWarnings("unused")
    RequestedSortRequirementAssertion hasDirection(String expectedDirection) {
      assert requestedSortRequirementMap.direction == expectedDirection
      return this
    }
  }
}
