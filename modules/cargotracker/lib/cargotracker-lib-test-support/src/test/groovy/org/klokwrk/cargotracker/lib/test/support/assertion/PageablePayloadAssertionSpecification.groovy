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

import org.klokwrk.cargotracker.lib.test.support.assertion.testobject.TestPersonPageableResponseAssertion
import spock.lang.Shared
import spock.lang.Specification

class PageablePayloadAssertionSpecification extends Specification {
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
    TestPersonPageableResponseAssertion.assertResponseContent(responseContentMap)

    then:
    true
  }

  void "assertResponseContent - should fail as expected"() {
    when:
    TestPersonPageableResponseAssertion.assertResponseContent(responseContentMapParam)

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
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap)

    then:
    assertion
  }

  void "constructor - should fail as expected"() {
    when:
    new TestPersonPageableResponseAssertion(responseContentMapParam)

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
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion = assertion.isSuccessful()

    then:
    assertion
  }

  void "isSuccessful - should fail as expected"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMapParam)

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
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_empty)

    when:
    assertion = assertion.isSuccessfulAndEmpty()

    then:
    assertion
  }

  void "isSuccessfulAndEmpty - should fail as expected"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMapParam)

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
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap)

    when:
    assertion = assertion.isEmpty()

    then:
    assertion
  }

  void "isEmpty - should fail as expected"() {
    given:
    Map responseContentMap = [payload: [a: 1]]
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap)

    when:
    assertion.isEmpty()

    then:
    thrown(AssertionError)
  }

  void "hasPageInfoThat - should work as expected"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion = assertion.hasPageInfoThat {
      hasPageElementsCount(1)
    }

    then:
    assertion
  }

  void "hasPageInfoThat - should fail as expected"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion.hasPageInfoThat {
      hasPageElementsCount(10)
    }

    then:
    thrown(AssertionError)
  }

  void "hasPageInfoThat - should fail as expected at the time of closure dispatch"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMapParam)

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
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion = assertion.hasPageInfoOfFirstPageWithDefaults()

    then:
    assertion
  }

  void "hasPageInfoOfFirstPageWithDefaults - should fail as expected"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_empty)

    when:
    assertion.hasPageInfoOfFirstPageWithDefaults()

    then:
    thrown(AssertionError)
  }

  void "hasPageContentSizeGreaterThanOrEqual - should work as expected"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_populated_threePageElements)

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
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_populated_threePageElements)

    when:
    assertion.hasPageContentSizeGreaterThanOrEqual(4)

    then:
    thrown(AssertionError)
  }

  void "hasPageContentWithAnyItemThat - should work as expected"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_populated_threePageElements)

    when:
    assertion = assertion.hasPageContentWithAnyItemThat {
      hasAge(2)
    }

    then:
    assertion
  }

  void "hasPageContentWithAnyItemThat - should fail as expected"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_populated_threePageElements)

    when:
    assertion.hasPageContentWithAnyItemThat {
      hasAge(10)
    }

    then:
    thrown(AssertionError)
  }

  void "hasPageContentWithAnyItemThat - should fail as expected at the time of closure dispatch"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMapParam)

    when:
    assertion.hasPageContentWithAnyItemThat {
    }

    then:
    thrown(AssertionError)

    where:
    responseContentMapParam             | _
    [payload: [:]]                      | _
    [payload: [pageContent: null]]      | _
    [payload: [pageContent: [1, 2, 3]]] | _
  }

  void "hasPageContentWithAllItemsThat - should work as expected"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion = assertion.hasPageContentWithAllItemsThat {
      hasAge(0)
    }

    then:
    assertion
  }

  void "hasPageContentWithAllItemsThat - should fail as expected"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion.hasPageContentWithAllItemsThat {
      hasAge(10)
    }

    then:
    thrown(AssertionError)
  }

  void "hasPageContentWithAllItemsThat - should fail as expected at the time of closure dispatch"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMapParam)

    when:
    assertion.hasPageContentWithAllItemsThat {
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

  void "hasPageContentWithItemAtIndexThat - should work as expected"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion = assertion.hasPageContentWithItemAtIndexThat(0) {
      hasAge(0)
    }

    then:
    assertion
  }

  void "hasPageContentWithItemAtIndexThat - should fail as expected"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion.hasPageContentWithItemAtIndexThat(0) {
      hasAge(10)
    }

    then:
    thrown(AssertionError)
  }

  void "hasPageContentWithItemAtIndexThat - should fail as expected at the time of closure dispatch"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMapParam)

    when:
    assertion.hasPageContentWithItemAtIndexThat(0) {
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

  void "hasPageContentWithFirstItemThat - should work as expected"() {
    given:
    TestPersonPageableResponseAssertion assertion = new TestPersonPageableResponseAssertion(responseContentMap_populated_singlePageElement)

    when:
    assertion = assertion.hasPageContentWithFirstItemThat {
      hasAge(0)
    }

    then:
    assertion
  }
}
