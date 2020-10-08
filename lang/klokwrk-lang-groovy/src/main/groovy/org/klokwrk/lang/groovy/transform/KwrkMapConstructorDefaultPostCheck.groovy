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
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@interface KwrkMapConstructorDefaultPostCheck {
}
