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
package org.klokwrk.cargotracking.lib.axon.cqrs.command

import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.CommandException
import spock.lang.Specification

class CommandGatewayAdapterSpecification extends Specification {

  CommandGateway commandGatewayMock
  CommandGatewayAdapter commandGatewayAdapter

  void setup() {
    commandGatewayMock = Mock()
    commandGatewayAdapter = new CommandGatewayAdapter(commandGatewayMock)
  }

  void "sendAndWait(command) - should behave same as sendAndWait(command, null)"() {
    given:
    def command = "command"

    when:
    commandGatewayAdapter.sendAndWait(command)

    then:
    1 * commandGatewayMock.sendAndWait({ def commandParam -> commandParam instanceof String }, { def metaDataParam -> metaDataParam === MetaData.emptyInstance() })
  }

  void "sendAndWait(command, metaData) - should fail for null command"() {
    given:
    def command = null
    Map<String, ?> metaData = [:]

    when:
    commandGatewayAdapter.sendAndWait(command, metaData)

    then:
    thrown(AssertionError)
  }

  void "sendAndWait(command, metaData) - should work for null metaData"() {
    given:
    def command = "command"
    Map<String, ?> metaData = null

    when:
    commandGatewayAdapter.sendAndWait(command, metaData)

    then:
    1 * commandGatewayMock.sendAndWait({ def commandParam -> commandParam instanceof String }, { def metaDataParam -> metaDataParam === MetaData.emptyInstance() })
  }

  void "sendAndWait(command, metaData) - should delegate to the command gateway"() {
    given:
    def command = "command"
    Map<String, ?> metaData = [:]

    when:
    commandGatewayAdapter.sendAndWait(command, metaData)

    then:
    1 * commandGatewayMock.sendAndWait({ def commandParam -> commandParam instanceof String }, { def metaDataParam -> metaDataParam == metaData })
  }

  void "sendAndWait(command, metaData) - should propagate CommandExecutionException to the caller when details exception is not available"() {
    given:
    CommandExecutionException commandExecutionException = new CommandExecutionException("Command execution failed", null)

    CommandGateway commandGatewayStub = Stub()
    commandGatewayStub.sendAndWait(_ as Object, _ as MetaData) >> { throw commandExecutionException }

    CommandGatewayAdapter commandGatewayAdapter = new CommandGatewayAdapter(commandGatewayStub)

    def command = "command"
    Map<String, ?> metaData = [:]

    when:
    commandGatewayAdapter.sendAndWait(command, metaData)

    then:
    thrown(CommandExecutionException)
  }

  class MyException extends RuntimeException {
    MyException(String message) {
      super(message)
    }
  }

  void "sendAndWait(command, metaData) - should propagate details exception to the caller when details are available [details exception class: #exceptionDetailsParam.getClass().simpleName]"() {
    given:
    CommandExecutionException commandExecutionException = new CommandExecutionException("Command execution failed", null, exceptionDetailsParam)

    CommandGateway commandGatewayStub = Stub()
    commandGatewayStub.sendAndWait(_ as Object, _ as MetaData) >> { throw commandExecutionException }

    CommandGatewayAdapter commandGatewayAdapter = new CommandGatewayAdapter(commandGatewayStub)

    def command = "command"
    Map<String, ?> metaData = [:]

    when:
    commandGatewayAdapter.sendAndWait(command, metaData)

    then:
    //noinspection GroovyAssignabilityCheck
    thrown(exceptionDetailsParam.getClass())

    where:
    exceptionDetailsParam | _
    new MyException("my exception") | _
    new CommandException()          | _
  }
}
