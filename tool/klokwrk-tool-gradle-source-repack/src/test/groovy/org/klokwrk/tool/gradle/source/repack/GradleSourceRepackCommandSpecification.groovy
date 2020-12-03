package org.klokwrk.tool.gradle.source.repack

import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class GradleSourceRepackCommandSpecification extends Specification {

  @Shared
  @AutoCleanup
  ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)

  void "should display help message"() {
    given:
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.out = new PrintStream(byteArrayOutputStream)

    String[] args = ["--help"] as String[]
    PicocliRunner.run(GradleSourceRepackCommand, ctx, args)
    String outputString = byteArrayOutputStream

    expect:
    outputString.contains("Usage: klokwrk-tool-gradle-source-repack")
    outputString.contains("Downloads Gradle source distribution")
  }

  void "should display version message"() {
    given:
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.out = new PrintStream(byteArrayOutputStream)

    String[] args = ["--version"] as String[]
    PicocliRunner.run(GradleSourceRepackCommand, ctx, args)
    String outputString = byteArrayOutputStream

    expect:
    outputString.contains("klokwrk-tool-gradle-source-repack")
  }

  void "should fail for invalid gradle version"() {
    given:
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.err = new PrintStream(byteArrayOutputStream)

    when:
    PicocliRunner.run(GradleSourceRepackCommand, ctx, [gradleVersion] as String[])
    String outputString = byteArrayOutputStream

    then:
    outputString.contains("Invalid value '${ gradleVersion }' for parameter '<gradle-version>'.")

    where:
    gradleVersion | _
    "6"           | _
    "6."          | _
    "6.7."        | _
    "6.7.1."      | _
    "6.7.1.1"     | _
    "a"           | _
    "6.a"         | _
    "6.7.a"       | _
  }
}
