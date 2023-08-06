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
package org.klokwrk.lib.xlang.groovy.base.transform

import groovy.transform.AnnotationCollector
import groovy.transform.AnnotationCollectorMode
import groovy.transform.EqualsAndHashCode
import groovy.transform.ImmutableBase
import groovy.transform.ImmutableOptions
import groovy.transform.KnownImmutable
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import groovy.transform.ToString
import org.klokwrk.lib.xlang.groovy.base.transform.options.RelaxedPropertyHandler

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Variation of Groovy {@code Immutable} meta-annotation that removes tuple constructor and uses {@link RelaxedPropertyHandler} instead of Groovy default {@code ImmutablePropertyHandler}.
 * <p/>
 * In addition, it includes {@link KwrkMapConstructorNoArgHideable} and {@link KwrkMapConstructorDefaultPostCheck} annotations.
 * <p/>
 * All other options are same as in Groovy {@code Immutable}.
 */
@ToString(cache = true, includeSuperProperties = true)
@EqualsAndHashCode(cache = true)
@ImmutableBase
@ImmutableOptions
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true, includeSuperProperties = true, includeFields = true)
@KnownImmutable
@KwrkMapConstructorDefaultPostCheck
@KwrkMapConstructorNoArgHideable
@AnnotationCollector(mode = AnnotationCollectorMode.PREFER_EXPLICIT_MERGED)
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@interface KwrkImmutable {
}
