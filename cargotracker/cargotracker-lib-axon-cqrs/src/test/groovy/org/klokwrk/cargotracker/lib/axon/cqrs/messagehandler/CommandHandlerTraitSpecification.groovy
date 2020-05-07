package org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler

import org.axonframework.commandhandling.CommandExecutionException
import org.klokwrk.cargotracker.lib.boundary.api.exceptional.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.exceptional.violation.ViolationCode
import org.klokwrk.cargotracker.lib.boundary.api.exceptional.violation.ViolationInfo
import spock.lang.Specification

class CommandHandlerTraitSpecification extends Specification {

  class MyAggregate implements CommandHandlerTrait {
    void handleCommand() {
      doThrow(new CommandException(ViolationInfo.BAD_REQUEST, "My bad request"))
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
}
