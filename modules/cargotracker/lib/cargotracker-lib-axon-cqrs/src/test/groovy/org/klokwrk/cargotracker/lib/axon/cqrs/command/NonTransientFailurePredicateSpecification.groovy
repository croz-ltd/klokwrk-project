/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.lib.axon.cqrs.command

import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.common.AxonNonTransientException
import org.klokwrk.cargotracker.lib.boundary.api.application.exception.RemoteHandlerException
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo
import spock.lang.Specification

class NonTransientFailurePredicateSpecification extends Specification {
  void "should report transient exception correctly"() {
    given:
    NonTransientFailurePredicate predicate = new NonTransientFailurePredicate()

    when:
    boolean isNonTransient = predicate.test(new RuntimeException())

    then:
    !isNonTransient
  }

  void "should contain expected classes in NON_TRANSIENT_FAILURE_LIST"() {
    expect:
    NonTransientFailurePredicate.NON_TRANSIENT_FAILURE_LIST.containsAll([AxonNonTransientException])
  }

  void "should report non-transient exceptions from NON_TRANSIENT_FAILURE_LIST correctly"() {
    given:
    NonTransientFailurePredicate predicate = new NonTransientFailurePredicate()

    when:
    boolean isNonTransient = predicate.test(new AxonNonTransientException("message") {})

    then:
    isNonTransient
  }

  void "should report plain CommandException as transient"() {
    given:
    NonTransientFailurePredicate predicate = new NonTransientFailurePredicate()

    when:
    boolean isNonTransient = predicate.test(plainCommandExecutionException)

    then:
    !isNonTransient

    where:
    plainCommandExecutionException                            | _
    new CommandExecutionException("message", null, null)      | _
    new CommandExecutionException("message", null, "details") | _
  }

  void "should report CommandException with DomainException details as non-transient"() {
    given:
    NonTransientFailurePredicate predicate = new NonTransientFailurePredicate()

    when:
    boolean isNonTransient = predicate.test(ceeWithDomainExceptionDetails)

    then:
    isNonTransient

    where:
    ceeWithDomainExceptionDetails                                                               | _
    new CommandExecutionException("message", null, new DomainException(ViolationInfo.UNKNOWN))  | _
    new CommandExecutionException("message", null, new CommandException(ViolationInfo.UNKNOWN)) | _
  }

  void "should contain expected classes in REMOTE_HANDLER_NON_TRANSIENT_CAUSE_LIST"() {
    expect:
    NonTransientFailurePredicate.REMOTE_HANDLER_NON_TRANSIENT_CAUSE_LIST.containsAll([NullPointerException])
  }

  void "should report CommandException with RemoteHandlerException details with correct message as non-transient"() {
    given:
    NonTransientFailurePredicate predicate = new NonTransientFailurePredicate()

    when:
    boolean isNonTransient = predicate.test(
        new CommandExecutionException(
            "message",
            null,
            new RemoteHandlerException("1", "command failed because of ${ NullPointerException.name }", null)
        )
    )

    then:
    isNonTransient
  }

  void "should report CommandException with RemoteHandlerException details with unexpected message as transient"() {
    given:
    NonTransientFailurePredicate predicate = new NonTransientFailurePredicate()

    when:
    boolean isNonTransient = predicate.test(
        new CommandExecutionException(
            "message",
            null,
            new RemoteHandlerException("1", "message", null)
        )
    )

    then:
    !isNonTransient
  }
}
