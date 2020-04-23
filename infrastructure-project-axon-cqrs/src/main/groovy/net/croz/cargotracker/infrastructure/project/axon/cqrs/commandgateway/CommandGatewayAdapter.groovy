package net.croz.cargotracker.infrastructure.project.axon.cqrs.commandgateway

import groovy.transform.CompileStatic
import net.croz.cargotracker.api.open.shared.exceptional.exception.CommandException
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway

@CompileStatic
class CommandGatewayAdapter {
  private CommandGateway commandGateway

  CommandGatewayAdapter(CommandGateway commandGateway) {
    this.commandGateway = commandGateway
  }

  @SuppressWarnings("GrUnnecessaryPublicModifier")
  public <R, C> R sendAndWait(C command, Map metaData) {
    GenericCommandMessage<C> cargoBookCommandMessage = new GenericCommandMessage(command, metaData)

    R commandResponse
    try {
      commandResponse = commandGateway.sendAndWait(cargoBookCommandMessage)
    }
    catch (CommandExecutionException commandExecutionException) {
      if (commandExecutionException.details.isPresent()) {
        CommandException commandException = commandExecutionException.details.get() as CommandException
        throw commandException
      }
      else {
        throw commandExecutionException
      }

    }
    return commandResponse
  }
}
