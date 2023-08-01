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
package org.klokwrk.cargotracker.lib.axon.cqrs.command

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData

import static org.hamcrest.Matchers.notNullValue

/**
 * Simplifies the API usage and exception handling of Axon <code>CommandGateway</code>.
 */
@CompileStatic
class CommandGatewayAdapter {
  private final CommandGateway commandGateway

  CommandGatewayAdapter(CommandGateway commandGateway) {
    this.commandGateway = commandGateway
  }

  /**
   * Delegates calls to the <code>CommandGateway.sendAndWait()</code> method with null metaData.
   *
   * @param command The command to dispatch.
   * @param <R> The type of result expected from command execution.
   * @return the result of command execution.
   * @throws CommandExecutionException when details exception is not available.
   * @throws Throwable when available as details of <code>CommandExecutionException</code>.
   */
  @SuppressWarnings("GrUnnecessaryPublicModifier")
  public <R, C> R sendAndWait(C command) {
    R commandResponse = sendAndWait(command, null)
    return commandResponse
  }

  /**
   * Delegates calls to the <code>CommandGateway.sendAndWait()</code> method.
   * <p/>
   * In case when an exception is thrown from <code>CommandGateway</code>, it unwraps details exception (if available), and rethrows it to the caller.
   *
   * @param command The command to dispatch.
   * @param metaData The metadata map to dispatch with the command.
   * @param <R> The type of result expected from command execution.
   * @return the result of command execution.
   * @throws AssertionError when command is null.
   * @throws CommandExecutionException when details exception is not available.
   * @throws Throwable when available as details of <code>CommandExecutionException</code>.
   */
  @SuppressWarnings("GrUnnecessaryPublicModifier")
  public <R, C> R sendAndWait(C command, Map metaData) {
    requireMatch(command, notNullValue())

    R commandResponse
    try {
      commandResponse = commandGateway.sendAndWait(command, MetaData.from(metaData))
    }
    catch (CommandExecutionException commandExecutionException) {
      if (commandExecutionException.details.isPresent()) {
        Throwable detailsThrowable = commandExecutionException.details.get() as Throwable
        throw detailsThrowable
      }

      throw commandExecutionException
    }

    return commandResponse
  }
}
