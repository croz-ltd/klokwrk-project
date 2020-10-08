package org.klokwrk.lang.groovy.transform.stub

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@SuppressWarnings("unused")
@GroovyASTTransformationClass("org.klokwrk.lang.groovy.transform.KwrkMapConstructorNoArgHideableAstTransformation")
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@interface KwrkFakeMapConstructorNoArgHideable {
}
