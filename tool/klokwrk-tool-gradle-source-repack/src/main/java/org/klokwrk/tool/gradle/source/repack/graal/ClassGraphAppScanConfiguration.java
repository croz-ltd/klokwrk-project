package org.klokwrk.tool.gradle.source.repack.graal;

public class ClassGraphAppScanConfiguration {
  private final boolean isClassGraphScanVerbose;
  private final String[] classGraphAppScanPackages;
  private final String[] classGraphAppScanIgnoredJars;

  public ClassGraphAppScanConfiguration(boolean isClassGraphScanVerbose, String[] classGraphAppScanPackages, String[] classGraphAppScanIgnoredJars) {
    this.isClassGraphScanVerbose = isClassGraphScanVerbose;
    this.classGraphAppScanPackages = classGraphAppScanPackages;
    this.classGraphAppScanIgnoredJars = classGraphAppScanIgnoredJars;
  }

  public boolean isClassGraphScanVerbose() {
    return isClassGraphScanVerbose;
  }

  public String[] getClassGraphAppScanPackages() {
    return classGraphAppScanPackages;
  }

  public String[] getClassGraphAppScanIgnoredJars() {
    return classGraphAppScanIgnoredJars;
  }
}
