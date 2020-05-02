ruleset {

  // rulesets/basic.xml
  AssertWithinFinallyBlock
  AssignmentInConditional
  BigDecimalInstantiation
  BitwiseOperatorInConditional
  BooleanGetBoolean
  BrokenNullCheck
  BrokenOddnessCheck
  ClassForName
  ComparisonOfTwoConstants
  ComparisonWithSelf
  ConstantAssertExpression
  ConstantIfExpression
  ConstantTernaryExpression
  DeadCode
  DoubleNegative
  DuplicateCaseStatement
  DuplicateMapKey
  DuplicateSetValue
  EmptyCatchBlock
  EmptyClass
  EmptyElseBlock
  EmptyFinallyBlock
  EmptyForStatement
  EmptyIfStatement
  EmptyInstanceInitializer
  EmptyMethod
  EmptyStaticInitializer
  EmptySwitchStatement
  EmptySynchronizedStatement
  EmptyTryBlock
  EmptyWhileStatement
  EqualsAndHashCode
  EqualsOverloaded
  ExplicitGarbageCollection
  ForLoopShouldBeWhileLoop
  HardCodedWindowsFileSeparator
  HardCodedWindowsRootDirectory
  IntegerGetInteger
  MultipleUnaryOperators
  RandomDoubleCoercedToZero
  RemoveAllOnSelf
  ReturnFromFinallyBlock
  ThrowExceptionFromFinallyBlock

  // rulesets/braces.xml
  ElseBlockBraces
  ForStatementBraces
  IfStatementBraces
  WhileStatementBraces

  // rulesets/comments.xml
  ClassJavadoc { enabled = false }
  JavadocConsecutiveEmptyLines
  JavadocEmptyAuthorTag
  JavadocEmptyExceptionTag
  JavadocEmptyFirstLine
  JavadocEmptyLastLine
  JavadocEmptyParamTag
  JavadocEmptyReturnTag
  JavadocEmptySeeTag
  JavadocEmptySinceTag
  JavadocEmptyThrowsTag
  JavadocEmptyVersionTag
  JavadocMissingExceptionDescription
  JavadocMissingParamDescription
  JavadocMissingThrowsDescription

  // rulesets/concurrency.xml
  BusyWait
  DoubleCheckedLocking
  InconsistentPropertyLocking
  InconsistentPropertySynchronization
  NestedSynchronization
  StaticCalendarField
  StaticConnection
  StaticDateFormatField
  StaticMatcherField
  StaticSimpleDateFormatField
  SynchronizedMethod
  SynchronizedOnBoxedPrimitive
  SynchronizedOnGetClass
  SynchronizedOnReentrantLock
  SynchronizedOnString
  SynchronizedOnThis
  SynchronizedReadObjectMethod
  SystemRunFinalizersOnExit
  ThisReferenceEscapesConstructor
  ThreadGroup
  ThreadLocalNotStaticFinal
  ThreadYield
  UseOfNotifyMethod
  VolatileArrayField
  VolatileLongOrDoubleField
  WaitOutsideOfWhileLoop

  // rulesets/convention.xml
  CompileStatic
  ConfusingTernary
  CouldBeElvis
  CouldBeSwitchStatement
  FieldTypeRequired
  HashtableIsObsolete
  IfStatementCouldBeTernary { enabled = false }
  ImplicitClosureParameter { enabled = false }
  InvertedCondition { enabled = false }
  InvertedIfElse
  LongLiteralWithLowerCaseL
  MethodParameterTypeRequired
  MethodReturnTypeRequired
  NoDef
  NoDouble
  NoFloat
  NoJavaUtilDate
  NoTabCharacter
  ParameterReassignment
  PublicMethodsBeforeNonPublicMethods { enabled = false }
  StaticFieldsBeforeInstanceFields
  StaticMethodsBeforeInstanceMethods
  TernaryCouldBeElvis
  TrailingComma { enabled = false }
  VariableTypeRequired
  VectorIsObsolete

  // rulesets/design.xml
  AbstractClassWithPublicConstructor
  AbstractClassWithoutAbstractMethod
  AssignmentToStaticFieldFromInstanceMethod
  BooleanMethodReturnsNull
  BuilderMethodWithSideEffects
  CloneableWithoutClone
  CloseWithoutCloseable
  CompareToWithoutComparable
  ConstantsOnlyInterface
  EmptyMethodInAbstractClass
  FinalClassWithProtectedMember
  ImplementationAsType
  Instanceof
  LocaleSetDefault
  NestedForLoop
  PrivateFieldCouldBeFinal
  PublicInstanceField
  ReturnsNullInsteadOfEmptyArray
  ReturnsNullInsteadOfEmptyCollection
  SimpleDateFormatMissingLocale
  StatelessSingleton
  ToStringReturnsNull
}
