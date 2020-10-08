package org.klokwrk.lang.groovy.transform

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable

import java.lang.annotation.Annotation

import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt

@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
@CompileStatic
class KwrkMapConstructorDefaultPostCheckAstTransformation extends AbstractASTTransformation {
  private static final Class<? extends Annotation> MY_ANNOTATION_CLASS = KwrkMapConstructorDefaultPostCheck
  static final ClassNode MY_ANNOTATION_CLASS_NODE = ClassHelper.make(MY_ANNOTATION_CLASS)

  @Override
  void visit(ASTNode[] nodes, SourceUnit source) {
    init(nodes, sourceUnit)

    AnnotatedNode myAnnotationTargetNode = nodes[1] as AnnotatedNode
    AnnotationNode myAnnotationNode = nodes[0] as AnnotationNode
    if (MY_ANNOTATION_CLASS_NODE != myAnnotationNode.classNode) {
      return
    }

    ClassNode targetClassNode = myAnnotationTargetNode as ClassNode
    AnnotationNode mapConstructorAnnotationNode = findMapConstructorAnnotation(targetClassNode)
    if (mapConstructorAnnotationNode == null) {
      return
    }

    //noinspection GroovyPointlessBoolean
    if (shouldAddDefaultPostCheck(mapConstructorAnnotationNode, targetClassNode) == false) {
      return
    }

    ConstructorNode mapConstructorNode = findMapConstructorNode(targetClassNode)
    addDefaultPostCheck(mapConstructorNode)
  }

  private AnnotationNode findMapConstructorAnnotation(ClassNode classNode) {
    AnnotationNode mapConstructorAnnotation = classNode.annotations.find { AnnotationNode annotationNode -> annotationNode.classNode.name == MapConstructor.name }
    return mapConstructorAnnotation
  }

  private boolean shouldAddDefaultPostCheck(AnnotationNode mapConstructorAnnotation, ClassNode targetClassNode) {
    //noinspection GroovyPointlessBoolean
    if (mapConstructorAnnotation.getMember("post") != null) {
      return false
    }

    //noinspection GroovyPointlessBoolean
    if (targetClassNode.declaresInterface(new ClassNode(PostMapConstructorCheckable)) == false) {
      return false
    }

    return true
  }

  private ConstructorNode findMapConstructorNode(ClassNode classNode) {
    ConstructorNode mapConstructorNode = classNode.declaredConstructors.find { ConstructorNode constructorNode ->
      constructorNode.parameters.size() == 1 && constructorNode.parameters[0].type.name == Map.name
    }

    return mapConstructorNode
  }

  void addDefaultPostCheck(ConstructorNode mapConstructorNode) {
    Statement defaultPostCheckStatement = stmt(callThisX("postMapConstructorCheckProtocol", args(mapConstructorNode.parameters[0])))
    BlockStatement mapConstructorCode = mapConstructorNode.code as BlockStatement
    mapConstructorCode.addStatement(defaultPostCheckStatement)
  }
}
