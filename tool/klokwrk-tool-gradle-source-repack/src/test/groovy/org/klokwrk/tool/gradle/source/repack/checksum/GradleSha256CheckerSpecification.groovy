package org.klokwrk.tool.gradle.source.repack.checksum

import io.micronaut.core.io.ResourceResolver
import io.micronaut.core.io.scan.ClassPathResourceLoader
import spock.lang.Specification

class GradleSha256CheckerSpecification extends Specification {
  void "should match for provided valid sha-256 checksum"() {
    given:
    ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader).get()
    File testSlimGradleDistributionSha256File = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip.sha256").get().file)
    File testSlimGradleDistributionFile = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip").get().file)

    when:
    GradleSha256CheckInfo gradleSha256CheckInfo = GradleSha256Checker.checkSha256(testSlimGradleDistributionSha256File, testSlimGradleDistributionFile)

    then:
    gradleSha256CheckInfo.isMatch()
  }

  void "should fail for provided invalid sha-256 checksum"() {
    given:
    ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader).get()
    File testSlimGradleDistributionSha256File = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip.sha256-invalid").get().file)
    File testSlimGradleDistributionFile = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip").get().file)

    when:
    GradleSha256CheckInfo gradleSha256CheckInfo = GradleSha256Checker.checkSha256(testSlimGradleDistributionSha256File, testSlimGradleDistributionFile)

    then:
    !gradleSha256CheckInfo.isMatch()
  }
}
