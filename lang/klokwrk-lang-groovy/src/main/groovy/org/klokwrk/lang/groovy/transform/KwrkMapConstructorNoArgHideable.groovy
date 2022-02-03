/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lang.groovy.transform

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * AST transformation that changes visibility prom public to private (by default) for no-arg constructor generated by {@code @MapConstructor(noArg = true)}.
 * <p/>
 * In some cases (i.e., when working with Hibernate entity proxies), it might be necessary to use package-private or protected visibility for generated default constructor. For those
 * cases, annotation provides {@code makePackagePrivate} and {@code makeProtected} boolean attributes (both are {@code false} by default).
 * <p/>
 * If both {@code makePackagePrivate} and {@code makeProtected} are set to {@code true}, then {@code makePackagePrivate} takes precedence.
 * <p/>
 * It can be used standalone, but is commonly used as a part of {@code KwrkImmutable} meta-annotation.
 */
@GroovyASTTransformationClass("org.klokwrk.lang.groovy.transform.KwrkMapConstructorNoArgHideableAstTransformation")
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@interface KwrkMapConstructorNoArgHideable {
  /**
   * Set to {@code false} to disable transformation.
   */
  @SuppressWarnings("unused")
  boolean enableNoArgHiding() default true

  /**
   * Set to {@code true} for package private visibility of a default constructor.
   */
  @SuppressWarnings("unused")
  boolean makePackagePrivate() default false

  /**
   * Set to {@code true} for protected visibility of a default constructor.
   * <p/>
   * If {@code makePackagePrivate} is also set to {@code true}, then {@code makePackagePrivate} takes precedence.
   */
  @SuppressWarnings("unused")
  boolean makeProtected() default false
}
