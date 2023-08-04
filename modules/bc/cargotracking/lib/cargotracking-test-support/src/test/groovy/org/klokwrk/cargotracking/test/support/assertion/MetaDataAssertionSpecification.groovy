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

import spock.lang.Specification

import static MetaDataAssertion.assertResponseHasMetaDataThat

class MetaDataAssertionSpecification extends Specification {
  @SuppressWarnings("CodeNarc.PropertyName")
  static Map generalMap_info = [locale: "en", timestamp: "123", severity: "info"]

  @SuppressWarnings("CodeNarc.PropertyName")
  static Map generalMap_warning = [locale: "en", timestamp: "123", severity: "warning"]

  @SuppressWarnings("CodeNarc.PropertyName")
  static Map httpMap_400 = [message: "Bad Request", status: "400"]

  @SuppressWarnings("CodeNarc.PropertyName")
  static Map httpMap_404 = [message: "Not Found", status: "404"]

  @SuppressWarnings("CodeNarc.PropertyName")
  static Map httpMap_405 = [message: "Method Not Allowed", status: "405"]

  static Map mergeValidationReport(Map validationReportMap) {
    Map mergedMap = [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: "400", type: "validation", validationReport: [:]]]
    mergedMap.mergeDeep(violation: [validationReport: validationReportMap])
    return mergedMap
  }

  void "static assertResponseHasMetaDataThat - should work as expected"() {
    given:
    Map responseMap = [
        metaData: [a: 1],
        payload: [a: 1]
    ]

    when:
    MetaDataAssertion assertionInstance = assertResponseHasMetaDataThat(responseMap)

    then:
    assertionInstance

    and:
    when:
    assertionInstance = assertResponseHasMetaDataThat(responseMap, {})

    then:
    assertionInstance
  }

  void "static assertResponseHasMetaDataThat - should fail as expected"() {
    when:
    assertResponseHasMetaDataThat(responseMapParam)

    then:
    thrown(AssertionError)

    and:
    when:
    assertResponseHasMetaDataThat(responseMapParam, {})

    then:
    thrown(AssertionError)

    where:
    responseMapParam               | _
    [:]                            | _
    [a: 1, b: 1]                   | _
    [metaData: 1, b: 1]            | _
    [metaData: [a: 1], b: 1]       | _
    [metaData: [a: 1], payload: 1] | _
  }

  void "isSuccessful - should work as expected"() {
    given:
    Map responseMap = [
        metaData: [
            general: [
                locale: "en",
                timestamp: "123",
                severity: "info"
            ],
            http: [
                message: "OK",
                status: "200"
            ]
        ],
        payload: [a: 1]
    ]

    MetaDataAssertion assertion = assertResponseHasMetaDataThat(responseMap)

    when:
    assertion = assertion.isSuccessful()

    then:
    assertion
  }

  void "isSuccessful - should fail as expected"() {
    given:
    MetaDataAssertion assertion = new MetaDataAssertion(responseMapParam)

    when:
    assertion.isSuccessful()

    then:
    thrown(AssertionError)

    where:
    responseMapParam                                                   | _
    null                                                               | _
    [:]                                                                | _
    [a: 1]                                                             | _
    [a: 1, b: 1]                                                       | _
    [general: null, b: 1]                                              | _
    [general: 1, b: 1]                                                 | _
    [general: [:], b: 1]                                               | _
    [general: [a: 1, b: 1, c: 1], b: 1]                                | _
    [general: [locale: null, b: 1, c: 1], b: 1]                        | _
    [general: [locale: "", b: 1, c: 1], b: 1]                          | _
    [general: [locale: "en", timestamp: null, c: 1], b: 1]             | _
    [general: [locale: "en", timestamp: "", c: 1], b: 1]               | _
    [general: [locale: "en", timestamp: "123", severity: null], b: 1]  | _
    [general: [locale: "en", timestamp: "123", severity: ""], b: 1]    | _
    [general: [locale: "en", timestamp: "123", severity: "bla"], b: 1] | _
    [general: generalMap_info, http: null]                             | _
    [general: generalMap_info, http: 1]                                | _
    [general: generalMap_info, http: [:]]                              | _
    [general: generalMap_info, http: [a: 1, b: 1]]                     | _
    [general: generalMap_info, http: [message: null, b: 1]]            | _
    [general: generalMap_info, http: [message: "", b: 1]]              | _
    [general: generalMap_info, http: [message: "OK", status: null]]    | _
    [general: generalMap_info, http: [message: "OK", status: ""]]      | _
    [general: generalMap_info, http: [message: "OK", status: "000"]]   | _
  }

  void "isSuccessful_asReturnedFromFacadeApi - should work as expected"() {
    given:
    Map responseMap = [
        metaData: [
            general: [
                timestamp: "123",
                severity: "info"
            ]
        ],
        payload: [a: 1]
    ]

    MetaDataAssertion assertion = assertResponseHasMetaDataThat(responseMap)

    when:
    assertion = assertion.isSuccessful_asReturnedFromFacadeApi()

    then:
    assertion
  }

  void "isSuccessful_asReturnedFromFacadeApi - should fail as expected"() {
    given:
    MetaDataAssertion assertion = new MetaDataAssertion(responseMapParam)

    when:
    assertion.isSuccessful_asReturnedFromFacadeApi()

    then:
    thrown(AssertionError)

    where:
    responseMapParam                             | _
    null                                         | _
    [:]                                          | _
    [a: 1]                                       | _
    [a: 1, b: 1]                                 | _
    [general: null]                              | _
    [general: 1]                                 | _
    [general: [:]]                               | _
    [general: [a: 1, b: 1, c: 1]]                | _
    [general: [a: 1, b: 1]]                      | _
    [general: [timestamp: 1, b: 1]]              | _
    [general: [timestamp: 1, severity: null]]    | _
    [general: [timestamp: 1, severity: 1]]       | _
    [general: [timestamp: 1, severity: "bla"]]   | _
    [general: [timestamp: 1, severity: "error"]] | _
  }

  void "isViolationOfValidation - should work as expected"() {
    given:
    Map responseMap = [
        metaData: [
            general: [
                locale: "en",
                timestamp: "123",
                severity: "warning"
            ],
            http: [
                message: "Bad Request",
                status: "400"
            ],
            violation: [
                message: "a message",
                code: "400",
                type: "validation",

                validationReport: [
                    root: [
                        type: "a type"
                    ],
                    constraintViolations: [
                        [
                            type: "a type",
                            scope: "a scope",
                            path: "a path",
                            message: "a message"
                        ]
                    ]
                ]
            ]
        ],
        payload: [a: 1]
    ]

    MetaDataAssertion assertion = assertResponseHasMetaDataThat(responseMap)

    when:
    assertion = assertion.isViolationOfValidation()

    then:
    assertion
  }

  void "isViolationOfValidation - should fail as expected"() {
    given:
    MetaDataAssertion assertion = new MetaDataAssertion(responseMapParam)

    when:
    assertion.isViolationOfValidation()

    then:
    thrown(AssertionError)

    where:
    responseMapParam                                                                                                                             | _
    null                                                                                                                                         | _
    [:]                                                                                                                                          | _
    [a: 1]                                                                                                                                       | _
    [a: 1, b: 1, c: 1]                                                                                                                           | _
    [general: null, b: 1, c: 1]                                                                                                                  | _
    [general: 1, b: 1, c: 1]                                                                                                                     | _
    [general: [:], b: 1, c: 1]                                                                                                                   | _
    [general: [a: 1, b: 1, c: 1], b: 1, c: 1]                                                                                                    | _
    [general: [locale: null, b: 1, c: 1], b: 1, c: 1]                                                                                            | _
    [general: [locale: "", b: 1, c: 1], b: 1, c: 1]                                                                                              | _
    [general: [locale: "en", timestamp: null, c: 1], b: 1, c: 1]                                                                                 | _
    [general: [locale: "en", timestamp: "", c: 1], b: 1, c: 1]                                                                                   | _
    [general: [locale: "en", timestamp: "123", severity: null], b: 1, c: 1]                                                                      | _
    [general: [locale: "en", timestamp: "123", severity: ""], b: 1, c: 1]                                                                        | _
    [general: [locale: "en", timestamp: "123", severity: "bla"], b: 1, c: 1]                                                                     | _
    [general: generalMap_warning, http: null, c: 1]                                                                                              | _
    [general: generalMap_warning, http: 1, c: 1]                                                                                                 | _
    [general: generalMap_warning, http: [:], c: 1]                                                                                               | _
    [general: generalMap_warning, http: [a: 1, b: 1], c: 1]                                                                                      | _
    [general: generalMap_warning, http: [message: null, b: 1], c: 1]                                                                             | _
    [general: generalMap_warning, http: [message: "", b: 1], c: 1]                                                                               | _
    [general: generalMap_warning, http: [message: "Bad Request", status: null], c: 1]                                                            | _
    [general: generalMap_warning, http: [message: "Bad Request", status: ""], c: 1]                                                              | _
    [general: generalMap_warning, http: [message: "Bad Request", status: "000"], c: 1]                                                           | _
    [general: generalMap_warning, http: httpMap_400, violation: null]                                                                            | _
    [general: generalMap_warning, http: httpMap_400, violation: 1]                                                                               | _
    [general: generalMap_warning, http: httpMap_400, violation: [:]]                                                                             | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: null, a: 1, b: 1, c: 1]]                                               | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "", a: 1, b: 1, c: 1]]                                                 | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: null, b: 1, c: 1]]                                  | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: "", b: 1, c: 1]]                                    | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: "000", b: 1, c: 1]]                                 | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: "400", type: null, c: 1]]                           | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: "400", type: "", c: 1]]                             | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: "400", type: "invalid", c: 1]]                      | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: "400", type: "validation", validationReport: null]] | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: "400", type: "validation", validationReport: [:]]]  | _
    mergeValidationReport([a: 1])                                                                                                                | _
    mergeValidationReport([a: 1, b: 1])                                                                                                          | _
    mergeValidationReport([root: null, b: 1])                                                                                                    | _
    mergeValidationReport([root: [:], b: 1])                                                                                                     | _
    mergeValidationReport([root: [type: null], b: 1])                                                                                            | _
    mergeValidationReport([root: [type: "a type"], constraintViolations: null])                                                                  | _
    mergeValidationReport([root: [type: "a type"], constraintViolations: []])                                                                    | _
    mergeValidationReport([root: [type: "a type"], constraintViolations: [null]])                                                                | _
    mergeValidationReport([root: [type: "a type"], constraintViolations: [[:]]])                                                                 | _
    mergeValidationReport([root: [type: "a type"], constraintViolations: [[a: 1, b: 1, c: 1, d: 1]]])                                            | _
    mergeValidationReport([root: [type: "a type"], constraintViolations: [[type: null, b: 1, c: 1, d: 1]]])                                      | _
    mergeValidationReport([root: [type: "a type"], constraintViolations: [[type: "", b: 1, c: 1, d: 1]]])                                        | _
    mergeValidationReport([root: [type: "a type"], constraintViolations: [[type: "a type", scope: null, c: 1, d: 1]]])                           | _
    mergeValidationReport([root: [type: "a type"], constraintViolations: [[type: "a type", scope: "", c: 1, d: 1]]])                             | _
    mergeValidationReport([root: [type: "a type"], constraintViolations: [[type: "a type", scope: "a scope", path: null, d: 1]]])                | _
    mergeValidationReport([root: [type: "a type"], constraintViolations: [[type: "a type", scope: "a scope", path: "", d: 1]]])                  | _
    mergeValidationReport([root: [type: "a type"], constraintViolations: [[type: "a type", scope: "a scope", path: "a path", message: null]]])   | _
    mergeValidationReport([root: [type: "a type"], constraintViolations: [[type: "a type", scope: "a scope", path: "a path", message: ""]]])     | _
  }

  void "isViolationOfDomain_badRequest - should work as expected"() {
    given:
    Map responseMap = [
        metaData: [
            general: [
                locale: "en",
                timestamp: "123",
                severity: "warning"
            ],
            http: [
                message: "Bad Request",
                status: "400"
            ],
            violation: [
                message: "a message",
                code: "400",
                type: "domain"
            ]
        ],
        payload: [a: 1]
    ]

    MetaDataAssertion assertion = assertResponseHasMetaDataThat(responseMap)

    when:
    assertion = assertion.isViolationOfDomain_badRequest()

    then:
    assertion
  }

  void "isViolationOfDomain_badRequest - should fail as expected"() {
    given:
    MetaDataAssertion assertion = new MetaDataAssertion(responseMapParam)

    when:
    assertion.isViolationOfDomain_badRequest()

    then:
    thrown(AssertionError)

    where:
    responseMapParam                                                                                                  | _
    null                                                                                                              | _
    [:]                                                                                                               | _
    [a: 1]                                                                                                            | _
    [a: 1, b: 1, c: 1]                                                                                                | _
    [general: null, b: 1, c: 1]                                                                                       | _
    [general: 1, b: 1, c: 1]                                                                                          | _
    [general: [:], b: 1, c: 1]                                                                                        | _
    [general: [a: 1, b: 1, c: 1], b: 1, c: 1]                                                                         | _
    [general: [locale: null, b: 1, c: 1], b: 1, c: 1]                                                                 | _
    [general: [locale: "", b: 1, c: 1], b: 1, c: 1]                                                                   | _
    [general: [locale: "en", timestamp: null, c: 1], b: 1, c: 1]                                                      | _
    [general: [locale: "en", timestamp: "", c: 1], b: 1, c: 1]                                                        | _
    [general: [locale: "en", timestamp: "123", severity: null], b: 1, c: 1]                                           | _
    [general: [locale: "en", timestamp: "123", severity: ""], b: 1, c: 1]                                             | _
    [general: [locale: "en", timestamp: "123", severity: "bla"], b: 1, c: 1]                                          | _
    [general: generalMap_warning, http: null, c: 1]                                                                   | _
    [general: generalMap_warning, http: 1, c: 1]                                                                      | _
    [general: generalMap_warning, http: [:], c: 1]                                                                    | _
    [general: generalMap_warning, http: [a: 1, b: 1], c: 1]                                                           | _
    [general: generalMap_warning, http: [message: null, b: 1], c: 1]                                                  | _
    [general: generalMap_warning, http: [message: "", b: 1], c: 1]                                                    | _
    [general: generalMap_warning, http: [message: "Bad Request", status: null], c: 1]                                 | _
    [general: generalMap_warning, http: [message: "Bad Request", status: ""], c: 1]                                   | _
    [general: generalMap_warning, http: [message: "Bad Request", status: "000"], c: 1]                                | _
    [general: generalMap_warning, http: httpMap_400, violation: null]                                                 | _
    [general: generalMap_warning, http: httpMap_400, violation: 1]                                                    | _
    [general: generalMap_warning, http: httpMap_400, violation: [:]]                                                  | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: null, a: 1, b: 1]]                          | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "", a: 1, b: 1]]                            | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: null, b: 1]]             | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: "", b: 1]]               | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: "000", b: 1]]            | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: "400", type: null]]      | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: "400", type: ""]]        | _
    [general: generalMap_warning, http: httpMap_400, violation: [message: "a message", code: "400", type: "invalid"]] | _
  }

  void "isViolationOfDomain_notFound - should work as expected"() {
    given:
    Map responseMap = [
        metaData: [
            general: [
                locale: "en",
                timestamp: "123",
                severity: "warning"
            ],
            http: [
                message: "Not Found",
                status: "404"
            ],
            violation: [
                message: "a message",
                code: "404",
                type: "domain"
            ]
        ],
        payload: [a: 1]
    ]

    MetaDataAssertion assertion = assertResponseHasMetaDataThat(responseMap)

    when:
    assertion = assertion.isViolationOfDomain_notFound()

    then:
    assertion
  }

  void "isViolationOfDomain_notFound - should fail as expected"() {
    given:
    MetaDataAssertion assertion = new MetaDataAssertion(responseMapParam)

    when:
    assertion.isViolationOfDomain_notFound()

    then:
    thrown(AssertionError)

    where:
    responseMapParam                                                                                                  | _
    null                                                                                                              | _
    [:]                                                                                                               | _
    [a: 1]                                                                                                            | _
    [a: 1, b: 1, c: 1]                                                                                                | _
    [general: null, b: 1, c: 1]                                                                                       | _
    [general: 1, b: 1, c: 1]                                                                                          | _
    [general: [:], b: 1, c: 1]                                                                                        | _
    [general: [a: 1, b: 1, c: 1], b: 1, c: 1]                                                                         | _
    [general: [locale: null, b: 1, c: 1], b: 1, c: 1]                                                                 | _
    [general: [locale: "", b: 1, c: 1], b: 1, c: 1]                                                                   | _
    [general: [locale: "en", timestamp: null, c: 1], b: 1, c: 1]                                                      | _
    [general: [locale: "en", timestamp: "", c: 1], b: 1, c: 1]                                                        | _
    [general: [locale: "en", timestamp: "123", severity: null], b: 1, c: 1]                                           | _
    [general: [locale: "en", timestamp: "123", severity: ""], b: 1, c: 1]                                             | _
    [general: [locale: "en", timestamp: "123", severity: "bla"], b: 1, c: 1]                                          | _
    [general: generalMap_warning, http: null, c: 1]                                                                   | _
    [general: generalMap_warning, http: 1, c: 1]                                                                      | _
    [general: generalMap_warning, http: [:], c: 1]                                                                    | _
    [general: generalMap_warning, http: [a: 1, b: 1], c: 1]                                                           | _
    [general: generalMap_warning, http: [message: null, b: 1], c: 1]                                                  | _
    [general: generalMap_warning, http: [message: "", b: 1], c: 1]                                                    | _
    [general: generalMap_warning, http: [message: "Not Found", status: null], c: 1]                                   | _
    [general: generalMap_warning, http: [message: "Not Found", status: ""], c: 1]                                     | _
    [general: generalMap_warning, http: [message: "Not Found", status: "000"], c: 1]                                  | _
    [general: generalMap_warning, http: httpMap_404, violation: null]                                                 | _
    [general: generalMap_warning, http: httpMap_404, violation: 1]                                                    | _
    [general: generalMap_warning, http: httpMap_404, violation: [:]]                                                  | _
    [general: generalMap_warning, http: httpMap_404, violation: [message: null, a: 1, b: 1]]                          | _
    [general: generalMap_warning, http: httpMap_404, violation: [message: "", a: 1, b: 1]]                            | _
    [general: generalMap_warning, http: httpMap_404, violation: [message: "a message", code: null, b: 1]]             | _
    [general: generalMap_warning, http: httpMap_404, violation: [message: "a message", code: "", b: 1]]               | _
    [general: generalMap_warning, http: httpMap_404, violation: [message: "a message", code: "000", b: 1]]            | _
    [general: generalMap_warning, http: httpMap_404, violation: [message: "a message", code: "404", type: null]]      | _
    [general: generalMap_warning, http: httpMap_404, violation: [message: "a message", code: "404", type: ""]]        | _
    [general: generalMap_warning, http: httpMap_404, violation: [message: "a message", code: "404", type: "invalid"]] | _
  }

  void "isViolationOfInfrastructureWeb_methodNotAllowed - should work as expected"() {
    given:
    Map responseMap = [
        metaData: [
            general: [
                locale: "en",
                timestamp: "123",
                severity: "warning"
            ],
            http: [
                message: "Method Not Allowed",
                status: "405"
            ],
            violation: [
                message: "a message",
                code: "405",
                type: "infrastructure_web",
                logUuid: "123"
            ]
        ],
        payload: [a: 1]
    ]

    MetaDataAssertion assertion = assertResponseHasMetaDataThat(responseMap)

    when:
    assertion = assertion.isViolationOfInfrastructureWeb_methodNotAllowed()

    then:
    assertion
  }

  void "isViolationOfInfrastructureWeb_methodNotAllowed - should fail as expected"() {
    given:
    MetaDataAssertion assertion = new MetaDataAssertion(responseMapParam)

    when:
    assertion.isViolationOfInfrastructureWeb_methodNotAllowed()

    then:
    thrown(AssertionError)

    where:
    responseMapParam                                                                                                                            | _
    null                                                                                                                                        | _
    [:]                                                                                                                                         | _
    [a: 1]                                                                                                                                      | _
    [a: 1, b: 1, c: 1]                                                                                                                          | _
    [general: null, b: 1, c: 1]                                                                                                                 | _
    [general: 1, b: 1, c: 1]                                                                                                                    | _
    [general: [:], b: 1, c: 1]                                                                                                                  | _
    [general: [a: 1, b: 1, c: 1], b: 1, c: 1]                                                                                                   | _
    [general: [locale: null, b: 1, c: 1], b: 1, c: 1]                                                                                           | _
    [general: [locale: "", b: 1, c: 1], b: 1, c: 1]                                                                                             | _
    [general: [locale: "en", timestamp: null, c: 1], b: 1, c: 1]                                                                                | _
    [general: [locale: "en", timestamp: "", c: 1], b: 1, c: 1]                                                                                  | _
    [general: [locale: "en", timestamp: "123", severity: null], b: 1, c: 1]                                                                     | _
    [general: [locale: "en", timestamp: "123", severity: ""], b: 1, c: 1]                                                                       | _
    [general: [locale: "en", timestamp: "123", severity: "bla"], b: 1, c: 1]                                                                    | _
    [general: generalMap_warning, http: null, c: 1]                                                                                             | _
    [general: generalMap_warning, http: 1, c: 1]                                                                                                | _
    [general: generalMap_warning, http: [:], c: 1]                                                                                              | _
    [general: generalMap_warning, http: [a: 1, b: 1], c: 1]                                                                                     | _
    [general: generalMap_warning, http: [message: null, b: 1], c: 1]                                                                            | _
    [general: generalMap_warning, http: [message: "", b: 1], c: 1]                                                                              | _
    [general: generalMap_warning, http: [message: "Method Not Allowed", status: null], c: 1]                                                    | _
    [general: generalMap_warning, http: [message: "Method Not Allowed", status: ""], c: 1]                                                      | _
    [general: generalMap_warning, http: [message: "Method Not Allowed", status: "000"], c: 1]                                                   | _
    [general: generalMap_warning, http: httpMap_405, violation: null]                                                                           | _
    [general: generalMap_warning, http: httpMap_405, violation: 1]                                                                              | _
    [general: generalMap_warning, http: httpMap_405, violation: [:]]                                                                            | _
    [general: generalMap_warning, http: httpMap_405, violation: [message: null, a: 1, b: 1, c: 1]]                                              | _
    [general: generalMap_warning, http: httpMap_405, violation: [message: "", a: 1, b: 1, c: 1]]                                                | _
    [general: generalMap_warning, http: httpMap_405, violation: [message: "a message", code: null, b: 1, c: 1]]                                 | _
    [general: generalMap_warning, http: httpMap_405, violation: [message: "a message", code: "", b: 1, c: 1]]                                   | _
    [general: generalMap_warning, http: httpMap_405, violation: [message: "a message", code: "000", b: 1, c: 1]]                                | _
    [general: generalMap_warning, http: httpMap_405, violation: [message: "a message", code: "405", type: null, c: 1]]                          | _
    [general: generalMap_warning, http: httpMap_405, violation: [message: "a message", code: "405", type: "", c: 1]]                            | _
    [general: generalMap_warning, http: httpMap_405, violation: [message: "a message", code: "405", type: "invalid", c: 1]]                     | _
    [general: generalMap_warning, http: httpMap_405, violation: [message: "a message", code: "405", type: "infrastructure_web", logUuid: null]] | _
    [general: generalMap_warning, http: httpMap_405, violation: [message: "a message", code: "405", type: "infrastructure_web", logUuid: ""]]   | _
  }

  void "has_general_locale - should work as expected"() {
    given:
    Map metaDataMap = [
        general: [
            locale: "en"
        ]
    ]

    MetaDataAssertion assertion = new MetaDataAssertion(metaDataMap)

    when:
    assertion = assertion.has_general_locale("en")

    then:
    assertion
  }

  void "has_general_locale - should fail as expected"() {
    given:
    Map metaDataMap = [
        general: [
            locale: "en"
        ]
    ]

    MetaDataAssertion assertion = new MetaDataAssertion(metaDataMap)

    when:
    assertion.has_general_locale("hr")

    then:
    thrown(AssertionError)
  }

  void "has_violation_message - should work as expected"() {
    given:
    Map metaDataMap = [
        violation: [
            message: "a message"
        ]
    ]

    MetaDataAssertion assertion = new MetaDataAssertion(metaDataMap)

    when:
    assertion = assertion.has_violation_message("a message")

    then:
    assertion
  }

  void "has_violation_message - should fail as expected"() {
    given:
    Map metaDataMap = [
        violation: [
            message: "a message"
        ]
    ]

    MetaDataAssertion assertion = new MetaDataAssertion(metaDataMap)

    when:
    assertion.has_violation_message("invalid expected message")

    then:
    thrown(AssertionError)
  }

  void "has_violation_validationReport_constraintViolationsOfSize - should work as expected"() {
    given:
    Map metaDataMap = [
        violation: [
            validationReport: [
                constraintViolations: [1, 2, 3]
            ]
        ]
    ]

    MetaDataAssertion assertion = new MetaDataAssertion(metaDataMap)

    when:
    assertion = assertion.has_violation_validationReport_constraintViolationsOfSize(3)

    then:
    assertion
  }

  void "has_violation_validationReport_constraintViolationsOfSize - should fail as expected"() {
    given:
    MetaDataAssertion assertion = new MetaDataAssertion(metaDataMapParam)

    when:
    assertion.has_violation_validationReport_constraintViolationsOfSize(2)

    then:
    thrown(AssertionError)

    where:
    metaDataMapParam                                                   | _
    null                                                               | _
    [:]                                                                | _
    [a: 1]                                                             | _
    [violation: null]                                                  | _
    [violation: [:]]                                                   | _
    [violation: [validationReport: null]]                              | _
    [violation: [validationReport: [:]]]                               | _
    [violation: [validationReport: [constraintViolations: null]]]      | _
    [violation: [validationReport: [constraintViolations: []]]]        | _
    [violation: [validationReport: [constraintViolations: [1, 2, 3]]]] | _
  }

  void "has_violation_validationReport_constraintViolationsWithAnyElementThat - should work as expected"() {
    given:
    Map metaDataMap = [
        violation: [
            validationReport: [
                constraintViolations: [
                    [type: "a type 1", scope: "a scope 1", path: "a path 1", message: "a message 1"],
                    [type: "a type 2", scope: "a scope 2", path: "a path 2", message: "a message 2"]
                ]
            ]
        ]
    ]

    MetaDataAssertion assertion = new MetaDataAssertion(metaDataMap)

    when:
    assertion = assertion.has_violation_validationReport_constraintViolationsWithAnyElementThat {
      hasType("a type 1")
      hasScope("a scope 1")
      hasPath("a path 1")
      hasMessage("a message 1")
    }

    then:
    assertion

    and:
    when:
    assertion = assertion.has_violation_validationReport_constraintViolationsWithAnyElementThat {
      hasType("a type 2")
      hasScope("a scope 2")
      hasPath("a path 2")
      hasMessage("a message 2")
    }

    then:
    assertion
  }

  void "has_violation_validationReport_constraintViolationsWithAnyElementThat - should fail as expected"() {
    given:
    Map metaDataMap = [
        violation: [
            validationReport: [
                constraintViolations: [
                    [type: "a type 1", scope: "a scope 1", path: "a path 1", message: "a message 1"],
                    [type: "a type 2", scope: "a scope 2", path: "a path 2", message: "a message 2"]
                ]
            ]
        ]
    ]

    MetaDataAssertion assertion = new MetaDataAssertion(metaDataMap)

    when:
    assertion.has_violation_validationReport_constraintViolationsWithAnyElementThat {
      hasType("non existing type")
    }

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Assertion failed - none of the list elements satisfies provided conditions."

    and:
    when:
    assertion.has_violation_validationReport_constraintViolationsWithAnyElementThat {
      hasScope("non existing scope")
    }

    then:
    assertionError = thrown()
    assertionError.message == "Assertion failed - none of the list elements satisfies provided conditions."

    and:
    when:
    assertion.has_violation_validationReport_constraintViolationsWithAnyElementThat {
      hasPath("non existing path")
    }

    then:
    assertionError = thrown()
    assertionError.message == "Assertion failed - none of the list elements satisfies provided conditions."

    and:
    when:
    assertion.has_violation_validationReport_constraintViolationsWithAnyElementThat {
      hasMessage("non existing message")
    }

    then:
    assertionError = thrown()
    assertionError.message == "Assertion failed - none of the list elements satisfies provided conditions."
  }

  void "has_violation_validationReport_constraintViolationsWithAnyElementThat - should fail as expected at the time of closure dispatch"() {
    given:
    MetaDataAssertion assertion = new MetaDataAssertion(metaDataMapParam)

    when:
    assertion.has_violation_validationReport_constraintViolationsWithAnyElementThat {
      hasType("non existing type")
    }

    then:
    thrown(AssertionError)

    where:
    metaDataMapParam                                                | _
    null                                                            | _
    [:]                                                             | _
    [a: 1]                                                          | _
    [violation: null]                                               | _
    [violation: [:]]                                                | _
    [violation: [validationReport: null]]                           | _
    [violation: [validationReport: [:]]]                            | _
    [violation: [validationReport: [constraintViolations: null]]]   | _
    [violation: [validationReport: [constraintViolations: 1]]]      | _
    [violation: [validationReport: [constraintViolations: []]]]     | _
    [violation: [validationReport: [constraintViolations: [null]]]] | _
    [violation: [validationReport: [constraintViolations: [1]]]]    | _
    [violation: [validationReport: [constraintViolations: [[:]]]]]  | _
  }
}
