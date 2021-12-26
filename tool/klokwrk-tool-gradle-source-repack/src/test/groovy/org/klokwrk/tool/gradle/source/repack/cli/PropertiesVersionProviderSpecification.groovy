package org.klokwrk.tool.gradle.source.repack.cli

import spock.lang.Specification

class PropertiesVersionProviderSpecification extends Specification {
  void "should return expected version info"() {
    given:
    PropertiesVersionProvider propertiesVersionProvider = new PropertiesVersionProvider()

    when:
    String versionString = propertiesVersionProvider.version[0]

    then:
    versionString.startsWith("klokwrk-tool-gradle-source-repack")
  }

  void "should return expected message when properties file is missing"() {
    given:
    PropertiesVersionProvider propertiesVersionProvider = new PropertiesVersionProvider()
    PropertiesVersionProvider propertiesVersionProviderSpy = Spy(propertiesVersionProvider)
    propertiesVersionProviderSpy.fetchVersionPropertiesPath() >> "/non-existing-version.properties"

    when:
    String versionString = propertiesVersionProviderSpy.version[0]

    then:
    versionString == "Version info is not available."
  }
}
