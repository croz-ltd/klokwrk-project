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

import org.klokwrk.cargotracker.lib.test.support.assertion.testobject.TestPageablePersonAssertion
import spock.lang.Shared
import spock.lang.Specification

class ResponseContentPageablePayloadAssertionSpecification extends Specification {
  @SuppressWarnings("CodeNarc.PropertyName")
  @Shared
  final Map responseContentMap_populated_singlePageElement = [
      payload: [
          pageInfo: [
              pageOrdinal: 0,
              pageElementsCount: 1,
              first: true,
              last: true,
              totalPagesCount: 1,
              totalElementsCount: 1,
              requestedPageRequirement: [ordinal: 0, size: 25],
              requestedSortRequirementList: [
                  [propertyName: "lastEventRecordedAt", direction: "DESC"]
              ]
          ],
          pageContent: [
              [firstName: "a first name", lastName: "a last name", age: 0]
          ]
      ]
  ]

  @SuppressWarnings("CodeNarc.PropertyName")
  @Shared
  final Map responseContentMap_populated_threePageElements = [
      payload: [
          pageInfo: [
              pageOrdinal: 0,
              pageElementsCount: 3,
              first: true,
              last: true,
              totalPagesCount: 1,
              totalElementsCount: 3,
              requestedPageRequirement: [ordinal: 0, size: 25],
              requestedSortRequirementList: [
                  [propertyName: "lastEventRecordedAt", direction: "DESC"]
              ]
          ],
          pageContent: [
              [firstName: "a first name 1", lastName: "a last name 1", age: 1],
              [firstName: "a first name 2", lastName: "a last name 2", age: 2],
              [firstName: "a first name 3", lastName: "a last name 3", age: 3]
          ]
      ]
  ]

  @SuppressWarnings("CodeNarc.PropertyName")
  @Shared
  final Map responseContentMap_empty = [
      payload: [
          pageInfo: [
              pageOrdinal: 0,
              pageElementsCount: 0,
              first: true,
              last: true,
              totalPagesCount: 0,
              totalElementsCount: 0,
              requestedPageRequirement: [ordinal: 0, size: 25],
              requestedSortRequirementList: [
                  [propertyName: "lastEventRecordedAt", direction: "DESC"]
              ]
          ],
          pageContent: [
          ]
      ]
  ]

  void "assertResponseContent - should work as expected"() {
    given:
    Map responseContentMap = [
        metaData: [:],
        payload: [:]
    ]

    when:
    TestPageablePersonAssertion.assertResponseContent(responseContentMap)

    then:
    true
  }

  void "assertResponseContent - should fail as expected"() {
    when:
    TestPageablePersonAssertion.assertResponseContent(responseContentMapParam)

    then:
    thrown(AssertionError)

    where:
    responseContentMapParam        | _
    null                           | _
    [:]                            | _
    [a: 1, b: 1]                   | _
    [metaData: null, b: 1]         | _
    [metaData: [:], payload: null] | _
  }

  void "constructor - should work as expected"() {
    given:
    Map responseContentMap = [payload: [:]]

    when:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap)

    then:
    assertion
  }

  void "constructor - should fail as expected"() {
    when:
    new TestPageablePersonAssertion(responseContentMapParam)

    then:
    thrown(AssertionError)

    where:
    responseContentMapParam | _
    null                    | _
    [:]                     | _
    [a: 1]                  | _
    [payload: null]         | _
  }

  void "isSuccessful - should work as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion = assertion.isSuccessful()

    then:
    assertion
  }

  void "isSuccessful - should fail as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMapParam)

    when:
    assertion.isSuccessful()

    then:
    thrown(AssertionError)

    where:
    responseContentMapParam                       | _
    [payload: [:]]                                | _
    [payload: [a: 1, b: 1]]                       | _
    [payload: [pageInfo: null, b: 1]]             | _
    [payload: [pageInfo: [:], pageContent: null]] | _
    [payload: [pageInfo: [:], pageContent: []]]   | _
  }

  void "isSuccessfulAndEmpty - should work as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_empty)

    when:
    assertion = assertion.isSuccessfulAndEmpty()

    then:
    assertion
  }

  void "isSuccessfulAndEmpty - should fail as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMapParam)

    when:
    assertion.isSuccessfulAndEmpty()

    then:
    thrown(AssertionError)

    where:
    responseContentMapParam                            | _
    [payload: [:]]                                     | _
    [payload: [a: 1, b: 1]]                            | _
    [payload: [pageInfo: null, b: 1]]                  | _
    [payload: [pageInfo: [:], pageContent: null]]      | _
    [payload: [pageInfo: [:], pageContent: [1, 2, 3]]] | _
  }

  void "isEmpty - should work as expected"() {
    given:
    Map responseContentMap = [payload: [:]]
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap)

    when:
    assertion = assertion.isEmpty()

    then:
    assertion
  }

  void "isEmpty - should fail as expected"() {
    given:
    Map responseContentMap = [payload: [a: 1]]
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap)

    when:
    assertion.isEmpty()

    then:
    thrown(AssertionError)
  }

  void "hasPageInfoThat - should work as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion = assertion.hasPageInfoThat {
      hasPageElementsCount(1)
    }

    then:
    assertion
  }

  void "hasPageInfoThat - should fail as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion.hasPageInfoThat {
      hasPageElementsCount(10)
    }

    then:
    thrown(AssertionError)
  }

  void "hasPageInfoThat - should fail as expected at the time of closure dispatch"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMapParam)

    when:
    assertion.hasPageInfoThat {
    }

    then:
    thrown(AssertionError)

    where:
    responseContentMapParam     | _
    [payload: [:]]              | _
    [payload: [pageInfo: null]] | _
    [payload: [pageInfo: 1]]    | _
  }

  void "hasPageInfoOfFirstPageWithDefaults - should work as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion = assertion.hasPageInfoOfFirstPageWithDefaults()

    then:
    assertion
  }

  void "hasPageInfoOfFirstPageWithDefaults - should fail as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_empty)

    when:
    assertion.hasPageInfoOfFirstPageWithDefaults()

    then:
    thrown(AssertionError)
  }

  void "hasPageContentSizeGreaterThanOrEqual - should work as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_populated_threePageElements)

    when:
    assertion = assertion.hasPageContentSizeGreaterThanOrEqual(pageContentSizeParam)

    then:
    assertion

    where:
    pageContentSizeParam | _
    1                    | _
    2                    | _
    3                    | _
  }

  void "hasPageContentSizeGreaterThanOrEqual - should fail as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_populated_threePageElements)

    when:
    assertion.hasPageContentSizeGreaterThanOrEqual(4)

    then:
    thrown(AssertionError)
  }

  void "hasPageContentWithAnyElementThat - should work as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_populated_threePageElements)

    when:
    assertion = assertion.hasPageContentWithAnyElementThat {
      hasAge(2)
    }

    then:
    assertion
  }

  void "hasPageContentWithAnyElementThat - should fail as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_populated_threePageElements)

    when:
    assertion.hasPageContentWithAnyElementThat {
      hasAge(10)
    }

    then:
    thrown(AssertionError)
  }

  void "hasPageContentWithAnyElementThat - should fail as expected at the time of closure dispatch"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMapParam)

    when:
    assertion.hasPageContentWithAnyElementThat {
    }

    then:
    thrown(AssertionError)

    where:
    responseContentMapParam             | _
    [payload: [:]]                      | _
    [payload: [pageContent: null]]      | _
    [payload: [pageContent: [1, 2, 3]]] | _
  }

  void "hasPageContentWithAllElementsThat - should work as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion = assertion.hasPageContentWithAllElementsThat {
      hasAge(0)
    }

    then:
    assertion
  }

  void "hasPageContentWithAllElementsThat - should fail as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion.hasPageContentWithAllElementsThat {
      hasAge(10)
    }

    then:
    thrown(AssertionError)
  }

  void "hasPageContentWithAllElementsThat - should fail as expected at the time of closure dispatch"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMapParam)

    when:
    assertion.hasPageContentWithAllElementsThat {
    }

    then:
    thrown(AssertionError)

    where:
    responseContentMapParam             | _
    [payload: [:]]                      | _
    [payload: [pageContent: null]]      | _
    [payload: [pageContent: []]]        | _
    [payload: [pageContent: [1, 2, 3]]] | _
  }

  void "hasPageContentWithElementAtIndexThat - should work as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion = assertion.hasPageContentWithElementAtIndexThat(0) {
      hasAge(0)
    }

    then:
    assertion
  }

  void "hasPageContentWithElementAtIndexThat - should fail as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion.hasPageContentWithElementAtIndexThat(0) {
      hasAge(10)
    }

    then:
    thrown(AssertionError)
  }

  void "hasPageContentWithElementAtIndexThat - should fail as expected at the time of closure dispatch"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMapParam)

    when:
    assertion.hasPageContentWithElementAtIndexThat(0) {
    }

    then:
    thrown(AssertionError)

    where:
    responseContentMapParam          | _
    [payload: [:]]                   | _
    [payload: [pageContent: null]]   | _
    [payload: [pageContent: []]]     | _
    [payload: [pageContent: [null]]] | _
    [payload: [pageContent: [1]]]    | _
  }

  void "hasPageContentWithFirstElementThat - should work as expected"() {
    given:
    TestPageablePersonAssertion assertion = new TestPageablePersonAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion = assertion.hasPageContentWithFirstElementThat {
      hasAge(0)
    }

    then:
    assertion
  }
}
