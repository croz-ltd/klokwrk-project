/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.lib.boundary.api.domain.violation

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.domain.severity.Severity
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import static org.hamcrest.Matchers.notNullValue

/**
 * Defines an immutable data structure that describes the reason for the <code>DomainException</code> exception.
 * <p/>
 * It contains the violation's severity and data structure describing the code of the violation. Both members need to be specified at construction time.
 *
 * @see org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
 * @see ViolationCode
 * @see Severity
 */
@KwrkImmutable
@CompileStatic
class ViolationInfo implements PostMapConstructorCheckable {
  static final ViolationInfo UNKNOWN = new ViolationInfo(severity: Severity.ERROR, violationCode: ViolationCode.UNKNOWN)
  static final ViolationInfo BAD_REQUEST = new ViolationInfo(severity: Severity.WARNING, violationCode: ViolationCode.BAD_REQUEST)
  static final ViolationInfo NOT_FOUND = new ViolationInfo(severity: Severity.WARNING, violationCode: ViolationCode.NOT_FOUND)

  static ViolationInfo createForBadRequestWithCustomCodeKey(String customCodeKey) {
    ViolationCode violationCode = new ViolationCode(code: ViolationCode.BAD_REQUEST.code, codeKey: customCodeKey, codeMessage: ViolationCode.BAD_REQUEST.codeMessage)
    return new ViolationInfo(severity: ViolationInfo.BAD_REQUEST.severity, violationCode: violationCode)
  }

  Severity severity
  ViolationCode violationCode

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(severity, notNullValue())
    requireMatch(violationCode, notNullValue())
  }
}
