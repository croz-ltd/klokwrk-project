package net.croz.cargotracker.infrastructure.project.axon.cqrs.messagehandler

import groovy.transform.CompileStatic
import net.croz.cargotracker.api.open.shared.exceptional.exception.CommandException
import org.axonframework.commandhandling.CommandExecutionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
trait CommandHandlerTrait extends MessageHandlerTrait {
  static private Logger log = LoggerFactory.getLogger(CommandHandlerTrait.name)

  void doThrow(CommandException domainException) {
    CommandExecutionException commandExecutionException = new CommandExecutionException("command execution failed", new ThrowAwayRuntimeException(), domainException)
    log.debug("Command execution in '${this.getClass().name}' failed.", commandExecutionException)

    throw commandExecutionException
  }
}
