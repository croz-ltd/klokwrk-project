package org.klokwrk.cargotracker.lib.axon.cqrs.commandgateway

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway

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
    sendAndWait(command, null)
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
    assert command != null

    GenericCommandMessage<C> cargoBookCommandMessage = new GenericCommandMessage(command, metaData)

    R commandResponse
    try {
      commandResponse = commandGateway.sendAndWait(cargoBookCommandMessage)
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
