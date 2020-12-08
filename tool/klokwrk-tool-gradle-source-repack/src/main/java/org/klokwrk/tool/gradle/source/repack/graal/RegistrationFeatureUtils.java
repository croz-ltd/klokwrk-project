package org.klokwrk.tool.gradle.source.repack.graal;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

public class RegistrationFeatureUtils {
  /**
   * Registers all supplied classes for runtime reflection.
   */
  public static void registerClasses(ClassInfoList classInfoToRegisterList) {
    classInfoToRegisterList
        .forEach((ClassInfo classInfo) -> {
          try {
            Class<?> someClass = Class.forName(classInfo.getName());
            RuntimeReflection.register(someClass);
            RuntimeReflection.register(someClass.getDeclaredConstructors());
            RuntimeReflection.register(someClass.getDeclaredMethods());
            RuntimeReflection.register(someClass.getDeclaredFields());
          }
          catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
        });
  }
}
