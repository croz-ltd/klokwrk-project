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

import groovy.transform.CompileStatic
import groovy.transform.Generated
import groovy.transform.MapConstructor
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import java.lang.annotation.Annotation

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
class KwrkMapConstructorNoArgHideableAstTransformation extends AbstractASTTransformation {

  private static final Class<? extends Annotation> MY_ANNOTATION_CLASS = KwrkMapConstructorNoArgHideable
  static final ClassNode MY_ANNOTATION_CLASS_NODE = ClassHelper.make(MY_ANNOTATION_CLASS)

  @Override
  void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
    init(nodes, sourceUnit)

    AnnotatedNode myAnnotationTargetNode = nodes[1] as AnnotatedNode
    AnnotationNode myAnnotationNode = nodes[0] as AnnotationNode
    if (MY_ANNOTATION_CLASS_NODE != myAnnotationNode.classNode) {
      return
    }

    if (memberHasValue(myAnnotationNode, "enableNoArgHiding", false)) {
      return
    }

    ClassNode targetClassNode = myAnnotationTargetNode as ClassNode
    if (findMapConstructorAnnotation(targetClassNode) == null) {
      return
    }

    ConstructorNode noArgConstructorNode = findNoArgConstructor(targetClassNode)
    if (findGeneratedAnnotation(noArgConstructorNode) == null) {
      return
    }

    noArgConstructorNode.modifiers = ACC_PRIVATE
  }

  private AnnotationNode findMapConstructorAnnotation(ClassNode classNode) {
    AnnotationNode mapConstructorAnnotation = classNode.annotations.find { AnnotationNode annotationNode -> annotationNode.classNode.name == MapConstructor.name }
    if (mapConstructorAnnotation == null) {
      return null
    }

    //noinspection GroovyPointlessBoolean
    if (memberHasValue(mapConstructorAnnotation, "noArg", true) == false) {
      return null
    }

    return mapConstructorAnnotation
  }

  private ConstructorNode findNoArgConstructor(ClassNode classNode) {
    ConstructorNode noArgConstructorNode = classNode.declaredConstructors.find { ConstructorNode constructorNode -> constructorNode.parameters.size() == 0 }
    return noArgConstructorNode
  }

  private AnnotationNode findGeneratedAnnotation(ConstructorNode constructorNode) {
    if (constructorNode == null) {
      return null
    }

    AnnotationNode generatedAnnotationNode = constructorNode.annotations.find { AnnotationNode annotationNode -> annotationNode.classNode.name == Generated.name }
    return generatedAnnotationNode
  }
}
