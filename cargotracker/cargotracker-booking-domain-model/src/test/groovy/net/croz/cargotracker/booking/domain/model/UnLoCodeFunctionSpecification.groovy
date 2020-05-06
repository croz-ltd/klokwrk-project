package net.croz.cargotracker.booking.domain.model

import spock.lang.Specification
import spock.lang.Unroll

class UnLoCodeFunctionSpecification extends Specification {
  @Unroll
  void "map constructor should work for correct input params: [functionEncoded: #functionParameter]"() {
    when:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    then:
    unLoCodeFunction.functionEncoded == functionParameter

    where:
    functionParameter | _
    "0-------"        | _
    "1-------"        | _
    "1234----"        | _
    "-------B"        | _
  }

  @Unroll
  void "map constructor should fail for invalid input params: [functionEncoded: #functionParameter]"() {
    when:
    new UnLoCodeFunction(functionEncoded: functionParameter)

    then:
    thrown(IllegalArgumentException)

    where:
    functionParameter | _
    null              | _
    ""                | _
    "   "             | _
    "1"               | _
    "--------"        | _
    "2-------"        | _
    "-1------"        | _
    "-0------"        | _
    "11------"        | _
    "1------"         | _
    "1--------"       | _
    "1------A"        | _
    "1------8"        | _
    "-------b"        | _
  }

  void "createWithPortClassifier() should work as expected"() {
    when:
    UnLoCodeFunction unLoCodeFunction = UnLoCodeFunction.createWithPortClassifier()

    then:
    unLoCodeFunction.functionEncoded == "1-------"
  }

  void "copyWithPortClassifier() should work as expected"() {
    given:
    UnLoCodeFunction unLoCodeFunctionOriginal = new UnLoCodeFunction(functionEncoded: "-234---B")

    when:
    UnLoCodeFunction unLoCodeFunctionCopy = UnLoCodeFunction.copyWithPortClassifier(unLoCodeFunctionOriginal)

    then:
    unLoCodeFunctionOriginal !== unLoCodeFunctionCopy
    unLoCodeFunctionCopy.functionEncoded == "1234---B"
  }

  void "copyWithPortClassifier() should return same instance when it already is a port"() {
    given:
    UnLoCodeFunction unLoCodeFunctionOriginal = new UnLoCodeFunction(functionEncoded: "1234---B")

    when:
    UnLoCodeFunction unLoCodeFunctionCopy = UnLoCodeFunction.copyWithPortClassifier(unLoCodeFunctionOriginal)

    then:
    unLoCodeFunctionOriginal === unLoCodeFunctionCopy
  }

  @Unroll
  void "isSpecified() should work as expected: [functionEncoded: #functionParameter, result: #result]"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isSpecified() == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | true
    "-2------"        | true
    "-23-----"        | true
    "-------B"        | true
  }

  @Unroll
  void "isPort() should work as expected: [functionEncoded: #functionParameter, result: #result]"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isPort() == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | true
    "-2------"        | false
    "-23-----"        | false
    "-------B"        | false
  }

  @Unroll
  void "isRailTerminal() should work as expected: [functionEncoded: #functionParameter, result: #result]"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isRailTerminal() == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | false
    "-2------"        | true
    "-23-----"        | true
    "-------B"        | false
  }

  @Unroll
  void "isRoadTerminal() should work as expected: [functionEncoded: #functionParameter, result: #result]"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isRoadTerminal() == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | false
    "-2------"        | false
    "-23-----"        | true
    "--3-----"        | true
    "-------B"        | false
  }

  @Unroll
  void "isAirport() should work as expected: [functionEncoded: #functionParameter, result: #result]"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isAirport() == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | false
    "-2------"        | false
    "-23-----"        | false
    "-234----"        | true
    "---4----"        | true
    "-------B"        | false
  }

  @Unroll
  void "isPostalExchangeOffice() should work as expected: [functionEncoded: #functionParameter, result: #result]"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isPostalExchangeOffice() == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | false
    "-2------"        | false
    "--3-----"        | false
    "---4----"        | false
    "----5---"        | true
    "-------B"        | false
  }

  @Unroll
  void "isBorderCrossing() should work as expected: [functionEncoded: #functionParameter, result: #result]"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isBorderCrossing() == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | false
    "-2------"        | false
    "--3-----"        | false
    "---4----"        | false
    "----5---"        | false
    "-------B"        | true
    "1------B"        | true
  }
}
