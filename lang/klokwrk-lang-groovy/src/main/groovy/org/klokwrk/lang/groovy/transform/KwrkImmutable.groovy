package org.klokwrk.lang.groovy.transform

import groovy.transform.AnnotationCollector
import groovy.transform.AnnotationCollectorMode
import groovy.transform.EqualsAndHashCode
import groovy.transform.ImmutableBase
import groovy.transform.ImmutableOptions
import groovy.transform.KnownImmutable
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import groovy.transform.ToString
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

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
