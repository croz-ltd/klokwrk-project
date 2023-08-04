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
package org.klokwrk.lib.xlang.groovy.base.extension

import spock.lang.Specification

class MapMergeDeepExtensionSpecification extends Specification {

  void "should return itself when there is no overrides"() {
    given:
    Map myMap = [:]

    expect:
    myMap.mergeDeep() === myMap
    myMap.mergeDeep(null) === myMap
    myMap.mergeDeep(null, null) === myMap
    myMap.mergeDeep([] as Map[]) === myMap
  }

  void "should work on empty map with single override"() {
    given:
    Map myMap = [:]
    Map override = [a: 1, b: [b1: 1, b2: 1], c: [:], d: 3, g: 1]

    when:
    Map mergedMap = myMap.mergeDeep(override)

    then:
    mergedMap === myMap
    mergedMap == override
  }

  void "should work on empty map with multiple overrides"() {
    given:
    Map myMap = [:]
    Map override1 = [a: 1, b: [b1: 1, b2: 1], c: [:], d: 3, g: 1]
    Map override2 = [a: 2, b: [b1: [b11: 1], b2: 2], c: "q", d: null, e: 6, f: [f1: 1]]

    Map expectedMerge = [a: 2, b: [b1: [b11: 1], b2: 2], c: "q", d: null, e: 6, f: [f1: 1], g: 1]

    when:
    Map mergedMap = myMap.mergeDeep(override1, override2)

    then:
    mergedMap === myMap
    mergedMap == expectedMerge
  }

  void "should work on empty map with multiple overrides including null overrides"() {
    given:
    Map myMap = [:]
    Map override1 = [a: 1, b: [b1: 1, b2: 1], c: [:], d: 3, g: 1]
    Map override2 = [a: 2, b: [b1: [b11: 1], b2: 2], c: "q", d: null, e: 6, f: [f1: 1]]

    Map expectedMerge = [a: 2, b: [b1: [b11: 1], b2: 2], c: "q", d: null, e: 6, f: [f1: 1], g: 1]

    when:
    Map mergedMap = myMap.mergeDeep(override1, null, override2)

    then:
    mergedMap === myMap
    mergedMap == expectedMerge
  }

  void "should work on empty map with multiple overrides including empty overrides"() {
    given:
    Map myMap = [:]
    Map override1 = [a: 1, b: [b1: 1, b2: 1], c: [:], d: 3, g: 1]
    Map override2 = [a: 2, b: [b1: [b11: 1], b2: 2], c: "q", d: null, e: 6, f: [f1: 1]]

    Map expectedMerge = [a: 2, b: [b1: [b11: 1], b2: 2], c: "q", d: null, e: 6, f: [f1: 1], g: 1]

    when:
    Map mergedMap = myMap.mergeDeep(override1, [:], override2)

    then:
    mergedMap === myMap
    mergedMap == expectedMerge
  }

  void "should work on populated map with single override"() {
    given:
    Map myMap = [a: 1, b: [b1: 1, b2: 1], c: [:], d: 3, g: 1]
    Map override = [a: 2, b: [b1: [b11: 1], b2: 2], c: 'q', d: null, e: 6, f: [f1: 1]]

    Map expectedMerge = [a: 2, b: [b1: [b11: 1], b2: 2], c: "q", d: null, e: 6, f: [f1: 1], g: 1]

    when:
    Map mergedMap = myMap.mergeDeep(override)

    then:
    mergedMap === myMap
    mergedMap == expectedMerge
  }

  void "should work on populated map with multiple overrides"() {
    given:
    Map myMap = [a: 1, b: [b1: 1, b2: 1], c: [:], d: 3, g: 1]
    Map override1 = [a: 2, b: [b1: [b11: 1], b2: 2], c: 'q', d: null, e: 6, f: [f1: 1]]
    Map override2 = [a: 3, b: [b1: [b11: 3]]]

    Map expectedMerge = [a: 3, b: [b1: [b11: 3], b2: 2], c: 'q', d: null, g: 1, e: 6, f: [f1: 1]]

    when:
    Map mergedMap = myMap.mergeDeep(override1, override2)

    then:
    mergedMap === myMap
    mergedMap == expectedMerge
  }

  void "should work on populated map with multiple overrides including null override"() {
    given:
    Map myMap = [a: 1, b: [b1: 1, b2: 1], c: [:], d: 3, g: 1]
    Map override1 = [a: 2, b: [b1: [b11: 1], b2: 2], c: 'q', d: null, e: 6, f: [f1: 1]]
    Map override2 = [a: 3, b: [b1: [b11: 3]]]

    Map expectedMerge = [a: 3, b: [b1: [b11: 3], b2: 2], c: 'q', d: null, g: 1, e: 6, f: [f1: 1]]

    when:
    Map mergedMap = myMap.mergeDeep(override1, null, override2)

    then:
    mergedMap === myMap
    mergedMap == expectedMerge
  }

  void "should work on populated map with multiple overrides including empty override"() {
    given:
    Map myMap = [a: 1, b: [b1: 1, b2: 1], c: [:], d: 3, g: 1]
    Map override1 = [a: 2, b: [b1: [b11: 1], b2: 2], c: 'q', d: null, e: 6, f: [f1: 1]]
    Map override2 = [a: 3, b: [b1: [b11: 3]]]

    Map expectedMerge = [a: 3, b: [b1: [b11: 3], b2: 2], c: 'q', d: null, g: 1, e: 6, f: [f1: 1]]

    when:
    Map mergedMap = myMap.mergeDeep(override1, [:], override2)

    then:
    mergedMap === myMap
    mergedMap == expectedMerge
  }

  void "should return new instances for the same override"() {
    Map override = [a: 1]

    // Map literal [:] always gives new map instance in Groovy
    Map myMap1 = [:].mergeDeep(override)
    Map myMap2 = [:].mergeDeep(override)

    expect:
    myMap1 !== myMap2
  }
}
