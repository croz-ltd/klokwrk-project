package net.croz.cargotracker.lang.groovy.transform

import groovy.transform.AnnotationCollector
import groovy.transform.AnnotationCollectorMode
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Meta annotation for generating map constructor with relaxed property handling as described in {@link RelaxedPropertyHandler}.
 */
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor
@AnnotationCollector(mode = AnnotationCollectorMode.PREFER_EXPLICIT_MERGED)
@Retention(RetentionPolicy.RUNTIME)
@Target([ ElementType.TYPE ])
@interface MapConstructorRelaxed {
}
