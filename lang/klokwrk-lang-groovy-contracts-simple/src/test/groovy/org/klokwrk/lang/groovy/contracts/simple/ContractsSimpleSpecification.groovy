package org.klokwrk.lang.groovy.contracts.simple

import spock.lang.Specification

class ContractsSimpleSpecification extends Specification {
  String myTestClassString = """
      class MyTestClass {
        void nonThrowing() {
          requireTrue(true)
          requireTrue(1 < 10)
          requireTrue(valid())
        }

        void throwingForConstant() {
          requireTrue(false)
        }

        void throwingForExpression() {
          requireTrue(1 > 10)
        }

        void throwingForMethodCall() {
          requireTrue(notValid())
        }

        Boolean valid() {
          return true
        }

        Boolean notValid() {
          return false
        }
      }
  """

  void "should not throw for valid checks"() {
    given:
    Class myTestClass = new GroovyClassLoader().parseClass(myTestClassString)
    def myTestClassInstance = myTestClass.newInstance([] as Object[])

    when:
    //noinspection GrUnresolvedAccess
    myTestClassInstance.nonThrowing()

    then:
    true
  }

  void "should throw for invalid constant"() {
    given:
    Class myTestClass = new GroovyClassLoader().parseClass(myTestClassString)
    def myTestClassInstance = myTestClass.newInstance([] as Object[])

    when:
    //noinspection GrUnresolvedAccess
    myTestClassInstance.throwingForConstant()

    then:
    AssertionError error = thrown()
    error.message.contains("[condition: false]")
  }

  void "should throw for invalid expression"() {
    given:
    Class myTestClass = new GroovyClassLoader().parseClass(myTestClassString)
    def myTestClassInstance = myTestClass.newInstance([] as Object[])

    when:
    //noinspection GrUnresolvedAccess
    myTestClassInstance.throwingForExpression()

    then:
    AssertionError error = thrown()
    error.message.contains("[condition: (1 > 10)]")
  }

  void "should throw for invalid method call"() {
    given:
    Class myTestClass = new GroovyClassLoader().parseClass(myTestClassString)
    def myTestClassInstance = myTestClass.newInstance([] as Object[])

    when:
    //noinspection GrUnresolvedAccess
    myTestClassInstance.throwingForMethodCall()

    then:
    AssertionError error = thrown()
    error.message.contains("[condition: this.notValid()]")
  }

  @SuppressWarnings("ComparisonOfTwoConstants")
  void "should work without parsing and demonstrate IDEA GDSL support"() {
    when:
    requireTrue(1 < 10)

    then:
    true

    and:
    when:
    requireTrue(1 > 10)

    then:
    AssertionError error = thrown()
    error.message.contains("[condition: (1 > 10)]")
  }
}
