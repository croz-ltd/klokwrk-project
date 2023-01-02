/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
 * Convenient AST transformation annotation that automatically adds default map-constructor post check code into generated map-constructor.
 * <p/>
 * Generated code just calls {@code PostMapConstructorCheckable.postMapConstructorCheckProtocol(Map originalMapConstructorArguments)}.
 * <p/>
 * Annotation is intended to be used with classes that are annotated with {@code @MapConstructor} and that implement {@code PostMapConstructorCheckable} interface. Transformation only applies if both
 * of these conditions are satisfied.
 * <p/>
 * Transformation is not applied if map-constructor postcondition is declared directly on {@code @MapConstructor} via {@code post} attribute.
 * <p/>
 * Commonly it is not used standalone, but rather as a part of {@code KwrkImmutable} meta-annotation.
 */
@GroovyASTTransformationClass("org.klokwrk.lang.groovy.transform.KwrkMapConstructorDefaultPostCheckAstTransformation")
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@interface KwrkMapConstructorDefaultPostCheck {
}
