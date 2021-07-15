package org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.Message
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.klokwrk.cargotracker.lib.boundary.api.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.exception.DomainException

@Slf4j
@CompileStatic
class CommandHandlerExceptionInterceptor<T extends Message<?>> implements MessageHandlerInterceptor<T> {
  @SuppressWarnings("CodeNarc.CatchException")
  @Override
  Object handle(UnitOfWork<? extends T> unitOfWork, InterceptorChain interceptorChain) throws Exception {
    try {
      Object returnValue = interceptorChain.proceed()
      return returnValue
    }
    catch (CommandException commandException) {
      String exceptionMessage = commandException.message

      CommandExecutionException commandExecutionExceptionToThrow =
          new CommandExecutionException("Command execution failed for business reasons (normal execution flow): $exceptionMessage", null, commandException)

      log.debug("Execution of command handler failed for business reasons (normal execution flow): $exceptionMessage", commandExecutionExceptionToThrow)

      throw commandExecutionExceptionToThrow
    }
    catch (Exception e) {
      CommandExecutionException commandExecutionExceptionToThrow = new CommandExecutionException("Command execution failed.", e, new DomainException())

      log.error("Execution of command handler failed.", commandExecutionExceptionToThrow)
      throw commandExecutionExceptionToThrow
    }
  }
}
