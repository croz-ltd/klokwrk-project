package org.klokwrk.lang.groovy.transform

import groovyjarjarasm.asm.Opcodes
import spock.lang.Specification

import java.lang.reflect.Constructor

class KwrkMapConstructorNoArgHideableAstTransformationSpecification extends Specification {

  void "should work with MapConstructor when noArg is true"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
        import groovy.transform.MapConstructor
        import org.klokwrk.lang.groovy.transform.KwrkMapConstructorNoArgHideable

        @MapConstructor(noArg = true)
        @KwrkMapConstructorNoArgHideable
        class MyClass {
          String first
          String last
        }
    """)

    when:
    def myClassInstance = myClass.newInstance(first: "Some first", last: "Some last")

    then:
    myClassInstance
    myClass.declaredConstructors.size() == 2
    myClass.declaredConstructors.find { Constructor constructor -> constructor.parameters.size() == 0 }.modifiers == Opcodes.ACC_PRIVATE
    myClass.declaredConstructors.find { Constructor constructor -> constructor.parameters.size() != 0 }.modifiers == Opcodes.ACC_PUBLIC
  }

  void "should work with default Immutable"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
        import groovy.transform.Immutable
        import org.klokwrk.lang.groovy.transform.KwrkMapConstructorNoArgHideable

        @Immutable
        @KwrkMapConstructorNoArgHideable
        class MyClass {
          String first
          String last
        }
    """)

    when:
    def myClassInstance = myClass.newInstance(first: "Some first", last: "Some last")

    then:
    myClassInstance
    myClass.declaredConstructors.size() == 3
    myClass.declaredConstructors.find { Constructor constructor -> constructor.parameters.size() == 0 }.modifiers == Opcodes.ACC_PRIVATE
    myClass.declaredConstructors.find { Constructor constructor -> constructor.parameters.size() == 1 }.modifiers == Opcodes.ACC_PUBLIC
    myClass.declaredConstructors.find { Constructor constructor -> constructor.parameters.size() == 2 }.modifiers == Opcodes.ACC_PUBLIC
  }

  void "should skip when not used from KwrkMapConstructorNoArgHideable annotation"() {
    given:
    def myClassInstance = new GroovyShell().evaluate("""
        import groovy.transform.MapConstructor
        import org.klokwrk.lang.groovy.transform.stub.KwrkFakeMapConstructorNoArgHideable

        @MapConstructor(noArg = true)
        @KwrkFakeMapConstructorNoArgHideable
        class MyClass {
          String first
          String last
        }

        return new MyClass(first: "Some first", last: "Some last")
    """)

    expect:
    myClassInstance
    myClassInstance.getClass().declaredConstructors.find { Constructor constructor -> constructor.parameters.size() == 0 }.modifiers == Opcodes.ACC_PUBLIC
  }

  void "should skip when enableNoArgHiding is set to false"() {
    given:
    def myClassInstance = new GroovyShell().evaluate("""
        import groovy.transform.MapConstructor
        import org.klokwrk.lang.groovy.transform.KwrkMapConstructorNoArgHideable

        @MapConstructor(noArg = true)
        @KwrkMapConstructorNoArgHideable(enableNoArgHiding = false)
        class MyClass {
          String first
          String last
        }

        return new MyClass(first: "Some first", last: "Some last")
    """)

    expect:
    myClassInstance
    myClassInstance.getClass().declaredConstructors.find { Constructor constructor -> constructor.parameters.size() == 0 }.modifiers == Opcodes.ACC_PUBLIC
  }

  void "should skip when target class does not have declared constructors"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
        import org.klokwrk.lang.groovy.transform.KwrkMapConstructorNoArgHideable

        @KwrkMapConstructorNoArgHideable
        class MyClass {
          String first
          String last
        }
    """)

    when:
    def myClassInstance = myClass.newInstance(first: "Some first", last: "Some last")

    then:
    myClassInstance
    myClass.declaredConstructors.size() == 1
    myClass.declaredConstructors.find { Constructor constructor -> constructor.parameters.size() == 0 }.modifiers == Opcodes.ACC_PUBLIC
  }

  void "should skip when target class have declared default constructor"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
        import org.klokwrk.lang.groovy.transform.KwrkMapConstructorNoArgHideable

        @KwrkMapConstructorNoArgHideable
        class MyClass {
          String first
          String last

          MyClass() {
            first = "n/a"
            last = "n/a"
          }
        }
    """)

    when:
    def myClassInstance = myClass.getDeclaredConstructor(null).newInstance()

    then:
    myClassInstance
    myClass.declaredConstructors.size() == 1
    myClass.getDeclaredConstructor(null).modifiers == Opcodes.ACC_PUBLIC
  }

  void "should skip with default MapConstructor (noArg is false by default)"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
        import groovy.transform.MapConstructor
        import org.klokwrk.lang.groovy.transform.KwrkMapConstructorNoArgHideable

        @MapConstructor(noArg = false)
        @KwrkMapConstructorNoArgHideable
        class MyClass {
          String first
          String last
        }
    """)

    when:
    def myClassInstance = myClass.newInstance(first: "Some first", last: "Some last")

    then:
    myClassInstance
    myClass.declaredConstructors.size() == 1
    myClass.declaredConstructors.find { Constructor constructor -> constructor.parameters.size() != 0 }.modifiers == Opcodes.ACC_PUBLIC
  }

  void "should skip with MapConstructor when noArg is false"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
        import groovy.transform.MapConstructor
        import org.klokwrk.lang.groovy.transform.KwrkMapConstructorNoArgHideable

        @MapConstructor(noArg = false)
        @KwrkMapConstructorNoArgHideable
        class MyClass {
          String first
          String last
        }
    """)

    when:
    def myClassInstance = myClass.newInstance(first: "Some first", last: "Some last")

    then:
    myClassInstance
    myClass.declaredConstructors.size() == 1
    myClass.declaredConstructors.find { Constructor constructor -> constructor.parameters.size() != 0 }.modifiers == Opcodes.ACC_PUBLIC
  }

  void "should skip for declared (non-generated) noArg constructor"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
        import groovy.transform.MapConstructor
        import org.klokwrk.lang.groovy.transform.KwrkMapConstructorNoArgHideable

        @MapConstructor(noArg = true)
        @KwrkMapConstructorNoArgHideable
        class MyClass {
          String first
          String last

          MyClass() {
            first = "n/a"
            last = "n/a"
          }
        }
    """)

    when:
    def myClassInstance = myClass.newInstance(first: "Some first", last: "Some last")

    then:
    myClassInstance
    myClass.declaredConstructors.size() == 2
    myClass.declaredConstructors.find { Constructor constructor -> constructor.parameters.size() == 0 }.modifiers == Opcodes.ACC_PUBLIC
    myClass.declaredConstructors.find { Constructor constructor -> constructor.parameters.size() != 0 }.modifiers == Opcodes.ACC_PUBLIC
  }
}
