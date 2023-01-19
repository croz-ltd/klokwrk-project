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
package org.klokwrk.cargotracker.lib.test.support.web

import groovy.transform.CompileStatic

@CompileStatic
class WebResponseContentMetaDataAssertion {
  static WebResponseContentMetaDataAssertion assertWebResponseContentHasMetaDataThat(Map webResponseContentMap) {
    webResponseContentMap.with {
      assert size() == 2
      assert metaData
      assert metaData instanceof Map
      assert payload != null
      assert payload instanceof Map
    }

    return new WebResponseContentMetaDataAssertion(webResponseContentMap.metaData as Map)
  }

  private final Map metaDataMap

  WebResponseContentMetaDataAssertion(Map metaDataMap) {
    this.metaDataMap = metaDataMap
  }

  WebResponseContentMetaDataAssertion isSuccessful() {
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

  WebResponseContentMetaDataAssertion isViolationOfValidation() {
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
        }
      }
    }

    return this
  }

  WebResponseContentMetaDataAssertion isViolationOfDomain_badRequest() {
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

  WebResponseContentMetaDataAssertion isViolationOfDomain_notFound() {
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

  WebResponseContentMetaDataAssertion isViolationOfInfrastructureWeb_methodNotAllowed() {
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

  WebResponseContentMetaDataAssertion has_general_locale(String expectedLocale) {
    (metaDataMap.general as Map).with {
      assert locale == expectedLocale
    }

    return this
  }

  WebResponseContentMetaDataAssertion has_violation_message(String expectedMessage) {
    (metaDataMap.violation as Map).with {
      assert message == expectedMessage
    }

    return this
  }
}
