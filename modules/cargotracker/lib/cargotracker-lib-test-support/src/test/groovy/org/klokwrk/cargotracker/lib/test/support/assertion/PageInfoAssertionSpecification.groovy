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

import spock.lang.Specification

class PageInfoAssertionSpecification extends Specification {
  static Map mergeRequestedPageRequirementMap(Object requestedPageRequirementMap) {
    Map mergedMap = [
        pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: 1, totalElementsCount: 1,
        requestedPageRequirement: [:],
        requestedSortRequirementList: []
    ]
    mergedMap.mergeDeep([requestedPageRequirement: requestedPageRequirementMap])
    return mergedMap
  }

  static Map mergeRequestedSortRequirementList(Object requestedSortRequirementList) {
    Map mergedMap = [
        pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: 1, totalElementsCount: 1,
        requestedPageRequirement: [ordinal: 0, size: 1],
        requestedSortRequirementList: []
    ]
    mergedMap.mergeDeep([requestedSortRequirementList: requestedSortRequirementList])
    return mergedMap
  }

  static Map mergeRequestedPageRequirementMap_empty(Object requestedPageRequirementMap) {
    Map mergedMap = [
        pageOrdinal: 0, pageElementsCount: 0, first: true, last: true, totalPagesCount: 0, totalElementsCount: 0,
        requestedPageRequirement: [:],
        requestedSortRequirementList: []
    ]
    mergedMap.mergeDeep([requestedPageRequirement: requestedPageRequirementMap])
    return mergedMap
  }

  static Map mergeRequestedSortRequirementList_empty(Object requestedSortRequirementList) {
    Map mergedMap = [
        pageOrdinal: 0, pageElementsCount: 0, first: true, last: true, totalPagesCount: 0, totalElementsCount: 0,
        requestedPageRequirement: [ordinal: 0, size: 1],
        requestedSortRequirementList: []
    ]
    mergedMap.mergeDeep([requestedSortRequirementList: requestedSortRequirementList])
    return mergedMap
  }

  static Map mergeRequestedPageRequirementMap_first(Object requestedPageRequirementMap) {
    Map mergedMap = [
        pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: 1, totalElementsCount: 1,
        requestedPageRequirement: [:],
        requestedSortRequirementList: []
    ]
    mergedMap.mergeDeep([requestedPageRequirement: requestedPageRequirementMap])
    return mergedMap
  }

  static Map mergeRequestedSortRequirementList_first(Object requestedSortRequirementList) {
    Map mergedMap = [
        pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: 1, totalElementsCount: 1,
        requestedPageRequirement: [ordinal: 0, size: 25],
        requestedSortRequirementList: []
    ]
    mergedMap.mergeDeep([requestedSortRequirementList: requestedSortRequirementList])
    return mergedMap
  }

  void "isSuccessful - should work as expected"() {
    given:
    Map pageInfoMap = [
        pageOrdinal: 0,
        pageElementsCount: 1,
        first: true,
        last: true,
        totalPagesCount: 1,
        totalElementsCount: 1,
        requestedPageRequirement: [ordinal: 0, size: 1],
        requestedSortRequirementList: [
            [propertyName: "someProperty", direction: directionParam]
        ]
    ]

    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion = assertion.isSuccessful()

    then:
    assertion

    where:
    directionParam | _
    "ASC"          | _
    "DESC"         | _
  }

  void "isSuccessful - should fail as expected"() {
    given:
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMapParam)

    when:
    assertion.isSuccessful()

    then:
    thrown(AssertionError)

    where:
    pageInfoMapParam                                                                                                          | _
    null                                                                                                                      | _
    [a: 1]                                                                                                                    | _
    [a: 1, b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                                          | _
    [pageOrdinal: null, b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                             | _
    [pageOrdinal: "a", b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                              | _
    [pageOrdinal: -1, b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                               | _
    [pageOrdinal: 0, b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                                | _
    [pageOrdinal: 0, pageElementsCount: null, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                             | _
    [pageOrdinal: 0, pageElementsCount: "a", c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                              | _
    [pageOrdinal: 0, pageElementsCount: 0, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                | _
    [pageOrdinal: 0, pageElementsCount: 1, first: null, d: 1, e: 1, f: 1, g: 1, h: 1]                                         | _
    [pageOrdinal: 0, pageElementsCount: 1, first: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                            | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: null, e: 1, f: 1, g: 1, h: 1]                                   | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: 1, e: 1, f: 1, g: 1, h: 1]                                      | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: null, f: 1, g: 1, h: 1]                  | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: "a", f: 1, g: 1, h: 1]                   | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: 0, f: 1, g: 1, h: 1]                     | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: 1, totalElementsCount: null, g: 1, h: 1] | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: 1, totalElementsCount: "a", g: 1, h: 1]  | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: 1, totalElementsCount: 0, g: 1, h: 1]    | _

    [pageOrdinal: 0, pageElementsCount: 10, first: true, last: true, totalPagesCount: 1, totalElementsCount: 1, g: 1, h: 1]   | _

    mergeRequestedPageRequirementMap(null)                                                                                    | _
    mergeRequestedPageRequirementMap(1)                                                                                       | _
    mergeRequestedPageRequirementMap([:])                                                                                     | _
    mergeRequestedPageRequirementMap([a: 1, b: 1])                                                                            | _
    mergeRequestedPageRequirementMap([ordinal: null, b: 1])                                                                   | _
    mergeRequestedPageRequirementMap([ordinal: "a", b: 1])                                                                    | _
    mergeRequestedPageRequirementMap([ordinal: -1, b: 1])                                                                     | _
    mergeRequestedPageRequirementMap([ordinal: 0, size: null])                                                                | _
    mergeRequestedPageRequirementMap([ordinal: 0, size: "a"])                                                                 | _
    mergeRequestedPageRequirementMap([ordinal: 0, size: 0])                                                                   | _

    mergeRequestedSortRequirementList(null)                                                                                   | _
    mergeRequestedSortRequirementList(1)                                                                                      | _
    mergeRequestedSortRequirementList([])                                                                                     | _
    mergeRequestedSortRequirementList([null])                                                                                 | _
    mergeRequestedSortRequirementList([1])                                                                                    | _
    mergeRequestedSortRequirementList([[:]])                                                                                  | _
    mergeRequestedSortRequirementList([[a: 1]])                                                                               | _
    mergeRequestedSortRequirementList([[a: 1, b: 1]])                                                                         | _
    mergeRequestedSortRequirementList([[propertyName: null, b: 1]])                                                           | _
    mergeRequestedSortRequirementList([[propertyName: "", b: 1]])                                                             | _
    mergeRequestedSortRequirementList([[propertyName: "aProperty", direction: null]])                                         | _
    mergeRequestedSortRequirementList([[propertyName: "aProperty", direction: ""]])                                           | _
    mergeRequestedSortRequirementList([[propertyName: "aProperty", direction: "invalid"]])                                    | _
  }

  void "isSuccessfulForEmptyPageContent - should work as expected"() {
    given:
    Map pageInfoMap = [
        pageOrdinal: 0,
        pageElementsCount: 0,
        first: true,
        last: true,
        totalPagesCount: 0,
        totalElementsCount: 0,
        requestedPageRequirement: [ordinal: 0, size: 1],
        requestedSortRequirementList: [
            [propertyName: "someProperty", direction: directionParam]
        ]
    ]

    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion = assertion.isSuccessfulForEmptyPageContent()

    then:
    assertion

    where:
    directionParam | _
    "ASC"          | _
    "DESC"         | _
  }

  void "isSuccessfulForEmptyPageContent - should fail as expected"() {
    given:
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMapParam)

    when:
    assertion.isSuccessfulForEmptyPageContent()

    then:
    thrown(AssertionError)

    where:
    pageInfoMapParam                                                                                                          | _
    null                                                                                                                      | _
    [a: 1]                                                                                                                    | _
    [a: 1, b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                                          | _
    [pageOrdinal: null, b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                             | _
    [pageOrdinal: "a", b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                              | _
    [pageOrdinal: -1, b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                               | _
    [pageOrdinal: 0, b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                                | _
    [pageOrdinal: 0, pageElementsCount: null, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                             | _
    [pageOrdinal: 0, pageElementsCount: "a", c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                              | _
    [pageOrdinal: 0, pageElementsCount: -1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                               | _
    [pageOrdinal: 0, pageElementsCount: 0, first: null, d: 1, e: 1, f: 1, g: 1, h: 1]                                         | _
    [pageOrdinal: 0, pageElementsCount: 0, first: null, d: 1, e: 1, f: 1, g: 1, h: 1]                                         | _
    [pageOrdinal: 0, pageElementsCount: 0, first: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                            | _
    [pageOrdinal: 0, pageElementsCount: 0, first: true, last: null, e: 1, f: 1, g: 1, h: 1]                                   | _
    [pageOrdinal: 0, pageElementsCount: 0, first: true, last: 1, e: 1, f: 1, g: 1, h: 1]                                      | _
    [pageOrdinal: 0, pageElementsCount: 0, first: true, last: true, totalPagesCount: null, f: 1, g: 1, h: 1]                  | _
    [pageOrdinal: 0, pageElementsCount: 0, first: true, last: true, totalPagesCount: "a", f: 1, g: 1, h: 1]                   | _
    [pageOrdinal: 0, pageElementsCount: 0, first: true, last: true, totalPagesCount: -1, f: 1, g: 1, h: 1]                    | _
    [pageOrdinal: 0, pageElementsCount: 0, first: true, last: true, totalPagesCount: 0, totalElementsCount: null, g: 1, h: 1] | _
    [pageOrdinal: 0, pageElementsCount: 0, first: true, last: true, totalPagesCount: 0, totalElementsCount: "a", g: 1, h: 1]  | _
    [pageOrdinal: 0, pageElementsCount: 0, first: true, last: true, totalPagesCount: 0, totalElementsCount: -1, g: 1, h: 1]   | _

    mergeRequestedPageRequirementMap_empty(null)                                                                              | _
    mergeRequestedPageRequirementMap_empty(1)                                                                                 | _
    mergeRequestedPageRequirementMap_empty([:])                                                                               | _
    mergeRequestedPageRequirementMap_empty([a: 1, b: 1])                                                                      | _
    mergeRequestedPageRequirementMap_empty([ordinal: null, b: 1])                                                             | _
    mergeRequestedPageRequirementMap_empty([ordinal: "a", b: 1])                                                              | _
    mergeRequestedPageRequirementMap_empty([ordinal: -1, b: 1])                                                               | _
    mergeRequestedPageRequirementMap_empty([ordinal: 0, size: null])                                                          | _
    mergeRequestedPageRequirementMap_empty([ordinal: 0, size: "a"])                                                           | _
    mergeRequestedPageRequirementMap_empty([ordinal: 0, size: 0])                                                             | _

    mergeRequestedSortRequirementList_empty(null)                                                                             | _
    mergeRequestedSortRequirementList_empty(1)                                                                                | _
    mergeRequestedSortRequirementList_empty([])                                                                               | _
    mergeRequestedSortRequirementList_empty([null])                                                                           | _
    mergeRequestedSortRequirementList_empty([1])                                                                              | _
    mergeRequestedSortRequirementList_empty([[:]])                                                                            | _
    mergeRequestedSortRequirementList_empty([[a: 1]])                                                                         | _
    mergeRequestedSortRequirementList_empty([[a: 1, b: 1]])                                                                   | _
    mergeRequestedSortRequirementList_empty([[propertyName: null, b: 1]])                                                     | _
    mergeRequestedSortRequirementList_empty([[propertyName: "", b: 1]])                                                       | _
    mergeRequestedSortRequirementList_empty([[propertyName: "aProperty", direction: null]])                                   | _
    mergeRequestedSortRequirementList_empty([[propertyName: "aProperty", direction: ""]])                                     | _
    mergeRequestedSortRequirementList_empty([[propertyName: "aProperty", direction: "invalid"]])                              | _
  }

  void "isFirstPageWithDefaults - should work as expected"() {
    given:
    Map pageInfoMap = [
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
    ]

    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion = assertion.isFirstPageWithDefaults()

    then:
    assertion
  }

  void "isFirstPageWithDefaults - should fail as expected"() {
    given:
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMapParam)

    when:
    assertion.isFirstPageWithDefaults()

    then:
    thrown(AssertionError)

    where:
    pageInfoMapParam                                                                                                          | _
    null                                                                                                                      | _
    [a: 1]                                                                                                                    | _
    [a: 1, b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                                          | _
    [pageOrdinal: null, b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                             | _
    [pageOrdinal: "a", b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                              | _
    [pageOrdinal: -1, b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                               | _
    [pageOrdinal: 0, b: 1, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                                | _
    [pageOrdinal: 0, pageElementsCount: null, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                             | _
    [pageOrdinal: 0, pageElementsCount: "a", c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                              | _
    [pageOrdinal: 0, pageElementsCount: 0, c: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                                | _
    [pageOrdinal: 0, pageElementsCount: 1, first: null, d: 1, e: 1, f: 1, g: 1, h: 1]                                         | _
    [pageOrdinal: 0, pageElementsCount: 1, first: 1, d: 1, e: 1, f: 1, g: 1, h: 1]                                            | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: null, e: 1, f: 1, g: 1, h: 1]                                   | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: 1, e: 1, f: 1, g: 1, h: 1]                                      | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: null, f: 1, g: 1, h: 1]                  | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: "a", f: 1, g: 1, h: 1]                   | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: 0, f: 1, g: 1, h: 1]                     | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: 1, totalElementsCount: null, g: 1, h: 1] | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: 1, totalElementsCount: "a", g: 1, h: 1]  | _
    [pageOrdinal: 0, pageElementsCount: 1, first: true, last: true, totalPagesCount: 1, totalElementsCount: 0, g: 1, h: 1]    | _

    [pageOrdinal: 0, pageElementsCount: 10, first: true, last: true, totalPagesCount: 1, totalElementsCount: 1, g: 1, h: 1]   | _

    mergeRequestedPageRequirementMap_first(null)                                                                              | _
    mergeRequestedPageRequirementMap_first(1)                                                                                 | _
    mergeRequestedPageRequirementMap_first([:])                                                                               | _
    mergeRequestedPageRequirementMap_first([a: 1, b: 1])                                                                      | _
    mergeRequestedPageRequirementMap_first([ordinal: null, b: 1])                                                             | _
    mergeRequestedPageRequirementMap_first([ordinal: "a", b: 1])                                                              | _
    mergeRequestedPageRequirementMap_first([ordinal: -1, b: 1])                                                               | _
    mergeRequestedPageRequirementMap_first([ordinal: 0, size: null])                                                          | _
    mergeRequestedPageRequirementMap_first([ordinal: 0, size: "a"])                                                           | _
    mergeRequestedPageRequirementMap_first([ordinal: 0, size: 0])                                                             | _

    mergeRequestedSortRequirementList_first(null)                                                                             | _
    mergeRequestedSortRequirementList_first(1)                                                                                | _
    mergeRequestedSortRequirementList_first([])                                                                               | _
    mergeRequestedSortRequirementList_first([null])                                                                           | _
    mergeRequestedSortRequirementList_first([1])                                                                              | _
    mergeRequestedSortRequirementList_first([[:]])                                                                            | _
    mergeRequestedSortRequirementList_first([[a: 1]])                                                                         | _
    mergeRequestedSortRequirementList_first([[a: 1, b: 1]])                                                                   | _
    mergeRequestedSortRequirementList_first([[propertyName: null, b: 1]])                                                     | _
    mergeRequestedSortRequirementList_first([[propertyName: "", b: 1]])                                                       | _
    mergeRequestedSortRequirementList_first([[propertyName: "invalid", b: 1]])                                                | _
    mergeRequestedSortRequirementList_first([[propertyName: "lastEventRecordedAt", direction: null]])                         | _
    mergeRequestedSortRequirementList_first([[propertyName: "lastEventRecordedAt", direction: ""]])                           | _
    mergeRequestedSortRequirementList_first([[propertyName: "lastEventRecordedAt", direction: "invalid"]])                    | _
  }

  void "hasPageOrdinal - should work as expected"() {
    given:
    Map pageInfoMap = [pageOrdinal: 1]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion = assertion.hasPageOrdinal(1)

    then:
    assertion
  }

  void "hasPageOrdinal - should fail as expected"() {
    given:
    Map pageInfoMap = [pageOrdinal: 1]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion.hasPageOrdinal(10)

    then:
    thrown(AssertionError)
  }

  void "hasPageElementsCount - should work as expected"() {
    given:
    Map pageInfoMap = [pageElementsCount: 1]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion = assertion.hasPageElementsCount(1)

    then:
    assertion
  }

  void "hasPageElementsCount - should fail as expected"() {
    given:
    Map pageInfoMap = [pageElementsCount: 1]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion.hasPageElementsCount(10)

    then:
    thrown(AssertionError)
  }

  void "hasPageElementsCountGreaterThenOrEqual - should work as expected"() {
    given:
    Map pageInfoMap = [pageElementsCount: 1]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion = assertion.hasPageElementsCountGreaterThenOrEqual(pageElementsCountParam)

    then:
    assertion

    where:
    pageElementsCountParam | _
    0                      | _
    1                      | _
  }

  void "hasPageElementsCountGreaterThenOrEqual - should fail as expected"() {
    given:
    Map pageInfoMap = [pageElementsCount: 1]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion.hasPageElementsCountGreaterThenOrEqual(2)

    then:
    thrown(AssertionError)
  }

  void "hasTotalElementsCount - should work as expected"() {
    given:
    Map pageInfoMap = [totalElementsCount: 1]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion = assertion.hasTotalElementsCount(1)

    then:
    assertion
  }

  void "hasTotalElementsCount - should fail as expected"() {
    given:
    Map pageInfoMap = [totalElementsCount: 1]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion.hasTotalElementsCount(10)

    then:
    thrown(AssertionError)
  }

  void "hasTotalElementsCountGreaterThanOrEqual - should work as expected"() {
    given:
    Map pageInfoMap = [totalElementsCount: 1]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion = assertion.hasTotalElementsCountGreaterThanOrEqual(pageElementsCountParam)

    then:
    assertion

    where:
    pageElementsCountParam | _
    0                      | _
    1                      | _
  }

  void "hasTotalElementsCountGreaterThanOrEqual - should fail as expected"() {
    given:
    Map pageInfoMap = [totalElementsCount: 1]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion.hasTotalElementsCountGreaterThanOrEqual(2)

    then:
    thrown(AssertionError)
  }

  void "hasFirstFlagOf - should work as expected"() {
    given:
    Map pageInfoMap = [first: firstParam]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion = assertion.hasFirstFlagOf(firstParam)

    then:
    assertion

    where:
    firstParam | _
    true       | _
    false      | _
  }

  void "hasFirstFlagOf - should fail as expected"() {
    given:
    Map pageInfoMap = [first: firstParam]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion.hasFirstFlagOf(!firstParam)

    then:
    thrown(AssertionError)

    where:
    firstParam | _
    true       | _
    false      | _
  }

  void "hasRequestedPageRequirementThat - should work as expected"() {
    given:
    Map pageInfoMap = [
        requestedPageRequirement: [
            ordinal: 1,
            size: 1
        ]
    ]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion = assertion.hasRequestedPageRequirementThat {
      hasOrdinal(1)
      hasSize(1)
    }

    then:
    assertion
  }

  void "hasRequestedPageRequirementThat - should fail as expected"() {
    given:
    Map pageInfoMap = [
        requestedPageRequirement: [
            ordinal: 1,
            size: 1
        ]
    ]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion.hasRequestedPageRequirementThat {
      hasOrdinal(10)
    }

    then:
    thrown(AssertionError)

    and:
    when:
    assertion.hasRequestedPageRequirementThat {
      hasSize(10)
    }

    then:
    thrown(AssertionError)
  }

  void "hasRequestedPageRequirementThat - should fail as expected at the time of closure dispatch"() {
    given:
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMapParam)

    when:
    assertion.hasRequestedPageRequirementThat {
    }

    then:
    thrown(AssertionError)

    where:
    pageInfoMapParam                 | _
    null                             | _
    [:]                              | _
    [a: 1]                           | _
    [requestedPageRequirement: null] | _
  }

  void "hasRequestedSortRequirementListWithElementAtIndexThat - should work as expected"() {
    given:
    Map pageInfoMap = [
        requestedSortRequirementList: [
            [
                propertyName: "aProperty",
                direction: "aDirection"
            ]
        ]
    ]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion = assertion.hasRequestedSortRequirementListWithElementAtIndexThat(0) {
      hasPropertyName("aProperty")
      hasDirection("aDirection")
    }

    then:
    assertion
  }

  void "hasRequestedSortRequirementListWithElementAtIndexThat - should fail as expected"() {
    given:
    Map pageInfoMap = [
        requestedSortRequirementList: [
            [
                propertyName: "aProperty",
                direction: "aDirection"
            ]
        ]
    ]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion.hasRequestedSortRequirementListWithElementAtIndexThat(0) {
      hasPropertyName("invalid")
    }

    then:
    thrown(AssertionError)

    and:
    when:
    assertion.hasRequestedSortRequirementListWithElementAtIndexThat(0) {
      hasDirection("invalid")
    }

    then:
    thrown(AssertionError)
  }

  void "hasRequestedSortRequirementListWithElementAtIndexThat - should fail as expected at the time of closure dispatch"() {
    given:
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMapParam)

    when:
    assertion.hasRequestedSortRequirementListWithElementAtIndexThat(0) {
    }

    then:
    thrown(AssertionError)

    where:
    pageInfoMapParam                       | _
    null                                   | _
    [:]                                    | _
    [a: 1]                                 | _
    [requestedSortRequirementList: null]   | _
    [requestedSortRequirementList: []]     | _
    [requestedSortRequirementList: [null]] | _
  }

  void "hasRequestedSortRequirementListWithFirstElementThat - should work as expected"() {
    given:
    Map pageInfoMap = [
        requestedSortRequirementList: [
            [
                propertyName: "aProperty",
                direction: "aDirection"
            ]
        ]
    ]
    PageInfoAssertion assertion = new PageInfoAssertion(pageInfoMap)

    when:
    assertion = assertion.hasRequestedSortRequirementListWithFirstElementThat {
      hasPropertyName("aProperty")
      hasDirection("aDirection")
    }

    then:
    assertion
  }
}
