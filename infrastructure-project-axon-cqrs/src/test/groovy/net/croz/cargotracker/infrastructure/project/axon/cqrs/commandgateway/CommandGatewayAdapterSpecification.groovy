package net.croz.cargotracker.infrastructure.project.axon.cqrs.commandgateway

import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.exception.CommandException
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import spock.lang.Specification
import spock.lang.Unroll

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
    1 * commandGatewayMock.sendAndWait({ def commandMessage ->
      verifyAll {
        commandMessage instanceof CommandMessage
        commandMessage.payload == command
        commandMessage.metaData === MetaData.emptyInstance()
      }
    })
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
    1 * commandGatewayMock.sendAndWait({ def commandMessage ->
      verifyAll {
        commandMessage instanceof CommandMessage
        commandMessage.payload == command
        commandMessage.metaData === MetaData.emptyInstance()
      }
    })
  }

  void "sendAndWait(command, metaData) - should delegate to the command gateway"() {
    given:
    def command = "command"
    Map<String, ?> metaData = [:]

    when:
    commandGatewayAdapter.sendAndWait(command, metaData)

    then:
    1 * commandGatewayMock.sendAndWait({ def commandMessage ->
      verifyAll {
        commandMessage instanceof CommandMessage
        commandMessage.payload == command
        commandMessage.metaData == metaData
      }
    })
  }

  void "sendAndWait(command, metaData) - should propagate CommandExecutionException to the caller when details exception is not available"() {
    given:
    CommandExecutionException commandExecutionException = new CommandExecutionException("Command execution failed", null)

    CommandGateway commandGatewayStub = Stub()
    commandGatewayStub.sendAndWait(_) >> { throw commandExecutionException }

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

  @Unroll
  void "sendAndWait(command, metaData) - should propagate details exception to the caller when details are available [details exception class: #exceptionDetailsParam.getClass().simpleName]"() {
    given:
    CommandExecutionException commandExecutionException = new CommandExecutionException("Command execution failed", null, exceptionDetailsParam)

    CommandGateway commandGatewayStub = Stub()
    commandGatewayStub.sendAndWait(_) >> { throw commandExecutionException }

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
