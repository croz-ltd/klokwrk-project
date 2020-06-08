package org.klokwrk.lang.groovy.transform.options

import spock.lang.Specification

class RelaxedPropertyHandlerSpecification extends Specification {
  void "should be strict with default MapConstructor"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
        import groovy.transform.MapConstructor

        @MapConstructor
        class MyClass {
          String first
          String last
        }
    """)

    Class myOtherClass = new GroovyClassLoader().parseClass("""
        class MyOtherClass {
          String first
          String last
          String address
        }
    """)

    def myOtherInstance = myOtherClass.newInstance(first: "someFirst", last: "someLast", address: "someAddress")

    when:
    myClass.newInstance(myOtherInstance.properties)

    then:
    def exception = thrown(MissingPropertyException)
    exception.message == "No such property: address for class: MyClass"
  }

  void "should be relaxed with RelaxedPropertyHandler applied"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
        import groovy.transform.MapConstructor
        import groovy.transform.PropertyOptions
        import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

        @PropertyOptions(propertyHandler = RelaxedPropertyHandler)
        @MapConstructor
        class MyClass {
          String first
          String last
        }
    """)

    Class myOtherClass = new GroovyClassLoader().parseClass("""
        class MyOtherClass {
          String first
          String last
          String address
        }
    """)

    def myOtherInstance = myOtherClass.newInstance(first: "someFirst", last: "someLast", address: "someAddress")

    when:
    def myClassInstance = myClass.newInstance(myOtherInstance.properties)

    then:
    myClassInstance.first == "someFirst"
    myClassInstance.last == "someLast"
  }

  void "should work with additional AST transformations affected by PropertyOptions"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
        import groovy.transform.MapConstructor
        import groovy.transform.PropertyOptions
        import groovy.transform.Immutable
        import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

        @Immutable
        @PropertyOptions(propertyHandler = RelaxedPropertyHandler)
        @MapConstructor
        class MyClass {
          String first
          String last
        }
    """)

    Class myOtherClass = new GroovyClassLoader().parseClass("""
        class MyOtherClass {
          String first
          String last
          String address
        }
    """)

    def myOtherInstance = myOtherClass.newInstance(first: "someFirst", last: "someLast", address: "someAddress")

    when:
    def myClassInstance = myClass.newInstance(myOtherInstance.properties)

    then:
    myClassInstance.first == "someFirst"
    myClassInstance.last == "someLast"
  }
}
