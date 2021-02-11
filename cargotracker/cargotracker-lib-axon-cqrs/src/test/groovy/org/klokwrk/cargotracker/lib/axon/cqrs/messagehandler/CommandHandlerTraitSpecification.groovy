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
package org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler

import org.axonframework.commandhandling.CommandExecutionException
import org.klokwrk.cargotracker.lib.boundary.api.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationCode
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationInfo
import spock.lang.Specification

class CommandHandlerTraitSpecification extends Specification {

  class MyAggregate implements CommandHandlerTrait {
    void handleCommand() {
      doThrow(new CommandException(ViolationInfo.BAD_REQUEST, "My bad request"))
    }

    void anotherHandleCommand() {
      doThrow(new CommandException(ViolationInfo.BAD_REQUEST, null))
    }
  }

  void "doThrow - should throw CommandExecutionException for passed in CommandException"() {
    given:
    MyAggregate myAggregate = new MyAggregate()

    when:
    myAggregate.handleCommand()

    then:
    CommandExecutionException commandExecutionException = thrown(CommandExecutionException)
    verifyAll(commandExecutionException) {
      cause instanceof MessageHandlerTrait.ThrowAwayRuntimeException
      details.get() instanceof CommandException
      (details.get() as CommandException).violationInfo.violationCode == ViolationCode.BAD_REQUEST
    }
  }

  void "doThrow - should throw CommandExecutionException for passed in CommandException without message"() {
    given:
    MyAggregate myAggregate = new MyAggregate()

    when:
    myAggregate.anotherHandleCommand()

    then:
    CommandExecutionException commandExecutionException = thrown(CommandExecutionException)
    verifyAll(commandExecutionException) {
      cause instanceof MessageHandlerTrait.ThrowAwayRuntimeException
      details.get() instanceof CommandException
      (details.get() as CommandException).violationInfo.violationCode == ViolationCode.BAD_REQUEST
      (details.get() as CommandException).message == (details.get() as CommandException).violationInfo.violationCode.codeMessage
    }
  }
}
