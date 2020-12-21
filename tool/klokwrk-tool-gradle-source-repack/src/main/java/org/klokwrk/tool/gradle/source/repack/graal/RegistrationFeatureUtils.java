/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
