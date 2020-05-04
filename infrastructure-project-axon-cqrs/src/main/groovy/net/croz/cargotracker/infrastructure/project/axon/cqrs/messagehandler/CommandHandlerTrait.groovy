package net.croz.cargotracker.infrastructure.project.axon.cqrs.messagehandler

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.exception.CommandException
import org.axonframework.commandhandling.CommandExecutionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Simplifies some aspects of Axon API usage during command handling.
 */
@CompileStatic
trait CommandHandlerTrait extends MessageHandlerTrait {
  static private final Logger log = LoggerFactory.getLogger(CommandHandlerTrait.name)

  /**
   * Simplifies throwing a business exception making sure it is propagated back to the caller as a details field of Axon's <code>CommandExecutionException</code>.
   * </p>
   * It also logs the stacktrace of CommandExecutionException being thrown, which helps during development.
   */
  void doThrow(CommandException domainException) {
    CommandExecutionException commandExecutionException = new CommandExecutionException("command execution failed", new ThrowAwayRuntimeException(), domainException)
    log.debug("Command execution in '${this.getClass().name}' failed.", commandExecutionException)

    throw commandExecutionException
  }
}
