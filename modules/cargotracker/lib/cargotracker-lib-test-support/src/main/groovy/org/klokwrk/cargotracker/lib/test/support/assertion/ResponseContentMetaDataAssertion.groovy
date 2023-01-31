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

@CompileStatic
class ResponseContentMetaDataAssertion {
  /**
   * Entry point static assertion method for fluent-style top-level API.
   */
  static ResponseContentMetaDataAssertion assertResponseContentHasMetaDataThat(Map responseContentMap) {
    responseContentMap.with {
      assert size() == 2
      assert metaData
      assert metaData instanceof Map
      assert payload != null
      assert payload instanceof Map
    }

    return new ResponseContentMetaDataAssertion(responseContentMap.metaData as Map)
  }

  /**
   * Entry point static assertion method for closure-style top-level API.
   */
  static ResponseContentMetaDataAssertion assertResponseContentHasMetaDataThat(
      Map responseContentMap,
      @DelegatesTo(value = ResponseContentMetaDataAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracker.lib.test.support.assertion.ResponseContentMetaDataAssertion"
      ) Closure aClosure)
  {
    ResponseContentMetaDataAssertion metaDataAssertion = assertResponseContentHasMetaDataThat(responseContentMap)
    aClosure.resolveStrategy = Closure.DELEGATE_FIRST
    aClosure.delegate = metaDataAssertion
    aClosure.call(metaDataAssertion)

    return metaDataAssertion
  }

  private final Map metaDataMap

  ResponseContentMetaDataAssertion(Map metaDataMap) {
    this.metaDataMap = metaDataMap
  }

  ResponseContentMetaDataAssertion isSuccessful() {
    metaDataMap.with {
      assert size() == 2

      (general as Map).with {
        assert size() == 3

        assert locale
        assert timestamp
        assert severity == "info"
      }

      (http as Map).with {
        assert size() == 2

        assert message == "OK"
        assert status == "200"
      }
    }

    return this
  }

  ResponseContentMetaDataAssertion isViolationOfValidation() {
    metaDataMap.with {
      assert size() == 3

      (general as Map).with {
        assert size() == 3
        assert locale
        assert timestamp
        assert severity == "warning"
      }

      (http as Map).with {
        assert size() == 2
        assert message == "Bad Request"
        assert status == "400"
      }

      (violation as Map).with {
        assert size() == 4
        assert message
        assert code == "400"
        assert type == "validation"
        assert validationReport

        (validationReport as Map).with {
          assert size() == 2
          assert root
          assert constraintViolations

          (root as Map).with {
            assert type
          }

          (constraintViolations as List<Map>).each { Map constraintViolation ->
            constraintViolation.with {
              assert type
              assert scope
              assert path
              assert message
            }
          }
        }
      }
    }

    return this
  }

  ResponseContentMetaDataAssertion isViolationOfDomain_badRequest() {
    metaDataMap.with {
      assert size() == 3

      (general as Map).with {
        assert size() == 3
        assert locale
        assert timestamp
        assert severity == "warning"
      }

      (http as Map).with {
        assert size() == 2
        assert message == "Bad Request"
        assert status == "400"
      }

      (violation as Map).with {
        assert size() == 3
        assert message
        assert code == "400"
        assert type == "domain"
      }
    }

    return this
  }

  ResponseContentMetaDataAssertion isViolationOfDomain_notFound() {
    metaDataMap.with {
      assert size() == 3

      (general as Map).with {
        assert size() == 3
        assert locale
        assert timestamp
        assert severity == "warning"
      }

      (http as Map).with {
        assert size() == 2
        assert message == "Not Found"
        assert status == "404"
      }

      (violation as Map).with {
        assert size() == 3
        assert message
        assert code == "404"
        assert type == "domain"
      }
    }

    return this
  }

  ResponseContentMetaDataAssertion isViolationOfInfrastructureWeb_methodNotAllowed() {
    metaDataMap.with {
      assert size() == 3

      (general as Map).with {
        assert size() == 3
        assert locale
        assert timestamp
        assert severity == "warning"
      }

      (http as Map).with {
        assert size() == 2
        assert message == "Method Not Allowed"
        assert status == "405"
      }

      (violation as Map).with {
        assert size() == 4
        assert message
        assert code == "405"
        assert type == "infrastructure_web"
        assert logUuid
      }
    }

    return this
  }

  ResponseContentMetaDataAssertion has_general_locale(String expectedLocale) {
    (metaDataMap.general as Map).with {
      assert locale == expectedLocale
    }

    return this
  }

  ResponseContentMetaDataAssertion has_violation_message(String expectedMessage) {
    (metaDataMap.violation as Map).with {
      assert message == expectedMessage
    }

    return this
  }
}
