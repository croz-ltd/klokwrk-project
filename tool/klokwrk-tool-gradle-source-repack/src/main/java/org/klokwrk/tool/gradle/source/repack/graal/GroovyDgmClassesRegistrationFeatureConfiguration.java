package org.klokwrk.tool.gradle.source.repack.graal;

public class GroovyDgmClassesRegistrationFeatureConfiguration {
  private final boolean isEnabled;
  private final boolean isScanVerboseClassGraph;
  private final boolean isScanVerboseFeature;

  public GroovyDgmClassesRegistrationFeatureConfiguration(boolean isEnabled, boolean isScanVerboseClassGraph, boolean isScanVerboseFeature) {
    this.isEnabled = isEnabled;
    this.isScanVerboseClassGraph = isScanVerboseClassGraph;
    this.isScanVerboseFeature = isScanVerboseFeature;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public boolean isScanVerboseClassGraph() {
    return isScanVerboseClassGraph;
  }

  public boolean isScanVerboseFeature() {
    return isScanVerboseFeature;
  }
}
