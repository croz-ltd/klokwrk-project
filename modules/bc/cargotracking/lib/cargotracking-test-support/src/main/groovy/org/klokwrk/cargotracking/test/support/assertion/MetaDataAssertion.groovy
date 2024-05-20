/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError

@CompileStatic
class MetaDataAssertion {
  /**
   * Entry point static assertion method for fluent-style top-level API.
   */
  static MetaDataAssertion assertResponseHasMetaDataThat(Map responseMap) {
    responseMap.with {
      assert size() == 2
      assert metaData
      assert metaData instanceof Map
      assert payload != null
      assert payload instanceof Map
    }

    return new MetaDataAssertion(responseMap.metaData as Map)
  }

  /**
   * Entry point static assertion method for closure-style top-level API.
   */
  static MetaDataAssertion assertResponseHasMetaDataThat(
      Map responseMap,
      @DelegatesTo(value = MetaDataAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracking.test.support.assertion.MetaDataAssertion"
      ) Closure aClosure)
  {
    MetaDataAssertion metaDataAssertion = assertResponseHasMetaDataThat(responseMap)
    aClosure.resolveStrategy = Closure.DELEGATE_FIRST
    aClosure.delegate = metaDataAssertion
    aClosure.call(metaDataAssertion)

    return metaDataAssertion
  }

  private final Map metaDataMap

  MetaDataAssertion(Map metaDataMap) {
    this.metaDataMap = metaDataMap
  }

  /**
   * Weather metadata contains all properties as returned from web controller.
   * <p/>
   * Those properties differ from ones returned from application facade API. For example, web interceptors will add {@code metaData.http} or {@code metaData.general.locale} properties.
   */
  MetaDataAssertion isSuccessful() {
    assert metaDataMap instanceof Map
    metaDataMap.with {
      assert size() == 2

      assert general instanceof Map
      (general as Map).with {
        assert size() == 3

        assert locale
        assert timestamp
        assert severity == "info"
      }

      assert http instanceof Map
      (http as Map).with {
        assert size() == 2

        assert message == "OK"
        assert status == "200"
      }
    }

    return this
  }

  /**
   * Weather metadata contains basic properties as returned from application facade API.
   * <p/>
   * Basic properties do not include properties added by interceptors specific for outbound channel like {@code metaData.http} or {@code metaData.general.locale}
   */
  MetaDataAssertion isSuccessful_asReturnedFromFacadeApi() {
    assert metaDataMap instanceof Map
    metaDataMap.with {
      assert size() == 1

      assert general instanceof Map
      (general as Map).with {
        assert size() == 2

        assert timestamp
        assert severity == "info"
      }
    }

    return this
  }

  MetaDataAssertion isViolationOfValidation() {
    assert metaDataMap instanceof Map
    metaDataMap.with {
      assert size() == 3

      assert general instanceof Map
      (general as Map).with {
        assert size() == 3
        assert locale
        assert timestamp
        assert severity == "warning"
      }

      assert http instanceof Map
      (http as Map).with {
        assert size() == 2
        assert message == "Bad Request"
        assert status == "400"
      }

      assert violation instanceof Map
      (violation as Map).with {
        assert size() == 4
        assert message
        assert code == "400"
        assert type == "validation"

        assert validationReport instanceof Map
        (validationReport as Map).with {
          assert size() == 2

          assert root instanceof Map
          (root as Map).with {
            assert size() == 1
            assert type
          }

          assert constraintViolations instanceof List
          assert !(constraintViolations as List).isEmpty()
          (constraintViolations as List).each { def constraintViolation ->
            assert constraintViolation instanceof Map
            (constraintViolation as Map).with {
              assert size() == 4
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

  MetaDataAssertion isViolationOfDomain_badRequest() {
    assert metaDataMap instanceof Map
    metaDataMap.with {
      assert size() == 3

      assert general instanceof Map
      (general as Map).with {
        assert size() == 3
        assert locale
        assert timestamp
        assert severity == "warning"
      }

      assert http instanceof Map
      (http as Map).with {
        assert size() == 2
        assert message == "Bad Request"
        assert status == "400"
      }

      assert violation instanceof Map
      (violation as Map).with {
        assert size() == 3
        assert message
        assert code == "400"
        assert type == "domain"
      }
    }

    return this
  }

  MetaDataAssertion isViolationOfDomain_notFound() {
    assert metaDataMap instanceof Map
    metaDataMap.with {
      assert size() == 3

      assert general instanceof Map
      (general as Map).with {
        assert size() == 3
        assert locale
        assert timestamp
        assert severity == "warning"
      }

      assert http instanceof Map
      (http as Map).with {
        assert size() == 2
        assert message == "Not Found"
        assert status == "404"
      }

      assert violation instanceof Map
      (violation as Map).with {
        assert size() == 3
        assert message
        assert code == "404"
        assert type == "domain"
      }
    }

    return this
  }

  MetaDataAssertion isViolationOfInfrastructureWeb_methodNotAllowed() {
    assert metaDataMap instanceof Map
    metaDataMap.with {
      assert size() == 3

      assert general instanceof Map
      (general as Map).with {
        assert size() == 3
        assert locale
        assert timestamp
        assert severity == "warning"
      }

      assert http instanceof Map
      (http as Map).with {
        assert size() == 2
        assert message == "Method Not Allowed"
        assert status == "405"
      }

      assert violation instanceof Map
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

  MetaDataAssertion has_general_locale(String expectedLocale) {
    (metaDataMap.general as Map).with {
      assert locale == expectedLocale
    }

    return this
  }

  MetaDataAssertion has_violation_message(String expectedMessage) {
    (metaDataMap.violation as Map).with {
      assert message == expectedMessage
    }

    return this
  }

  MetaDataAssertion has_violation_validationReport_constraintViolationsOfSize(Integer expectedSize) {
    List<Map> constraintViolations = ((metaDataMap?.violation as Map)?.validationReport as Map)?.constraintViolations as List<Map>
    assert constraintViolations != null
    assert constraintViolations.size() == expectedSize
    return this
  }

  MetaDataAssertion has_violation_validationReport_constraintViolationsWithAnyElementThat(
      @DelegatesTo(value = ConstraintViolationAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = 'org.klokwrk.cargotracking.test.support.assertion.MetaDataAssertion$ConstraintViolationAssertion'
      ) Closure aClosure)
  {
    List<Map> constraintViolations = ((metaDataMap?.violation as Map)?.validationReport as Map)?.constraintViolations as List<Map>
    assert constraintViolations != null
    constraintViolations.each { constraintViolation ->
      assert constraintViolation instanceof Map
    }

    aClosure.resolveStrategy = Closure.DELEGATE_FIRST

    boolean isAnyElementFound = constraintViolations.any({ Map constraintViolationMap ->
      ConstraintViolationAssertion constraintViolationAssertion = new ConstraintViolationAssertion(constraintViolationMap)
      aClosure.delegate = constraintViolationAssertion
      try {
        aClosure.call(constraintViolationAssertion)
        return true
      }
      catch (PowerAssertionError ignore) {
      }

      return false
    })

    if (!isAnyElementFound) {
      throw new AssertionError("Assertion failed - none of the list elements satisfies provided conditions." as Object)
    }

    return this
  }

  static class ConstraintViolationAssertion {
    private final Map constraintViolationMap

    ConstraintViolationAssertion(Map constraintViolationMap) {
      this.constraintViolationMap = constraintViolationMap
    }

    ConstraintViolationAssertion hasType(String expectedType) {
      assert constraintViolationMap.type == expectedType
      return this
    }

    ConstraintViolationAssertion hasScope(String expectedScope) {
      assert constraintViolationMap.scope == expectedScope
      return this
    }

    ConstraintViolationAssertion hasPath(String expectedPath) {
      assert constraintViolationMap.path == expectedPath
      return this
    }

    ConstraintViolationAssertion hasMessage(String expectedMessage) {
      assert constraintViolationMap.message == expectedMessage
      return this
    }
  }
}
