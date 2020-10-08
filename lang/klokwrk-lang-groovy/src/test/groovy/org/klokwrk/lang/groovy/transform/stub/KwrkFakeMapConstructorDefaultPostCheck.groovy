package org.klokwrk.lang.groovy.transform.stub

import groovy.transform.CompileStatic
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@SuppressWarnings("unused")
@GroovyASTTransformationClass("org.klokwrk.lang.groovy.transform.KwrkMapConstructorDefaultPostCheckAstTransformation")
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@CompileStatic
@interface KwrkFakeMapConstructorDefaultPostCheck {
}
