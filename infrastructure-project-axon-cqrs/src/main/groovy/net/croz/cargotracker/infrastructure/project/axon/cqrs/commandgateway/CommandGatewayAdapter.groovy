package net.croz.cargotracker.infrastructure.project.axon.cqrs.commandgateway

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway

@CompileStatic
class CommandGatewayAdapter {
  private final CommandGateway commandGateway

  CommandGatewayAdapter(CommandGateway commandGateway) {
    this.commandGateway = commandGateway
  }

  @SuppressWarnings("GrUnnecessaryPublicModifier")
  public <R, C> R sendAndWait(C command) {
    sendAndWait(command, null)
  }

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
