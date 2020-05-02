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
  EmptyMethod { enabled = false }
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
  ClassJavadoc { enabled = false}
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
  CompileStatic { enabled = false }
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
  BuilderMethodWithSideEffects { enabled = false }
  CloneableWithoutClone
  CloseWithoutCloseable
  CompareToWithoutComparable
  ConstantsOnlyInterface
  EmptyMethodInAbstractClass
  FinalClassWithProtectedMember
  ImplementationAsType
  Instanceof { enabled = false }
  LocaleSetDefault
  NestedForLoop
  PrivateFieldCouldBeFinal
  PublicInstanceField
  ReturnsNullInsteadOfEmptyArray
  ReturnsNullInsteadOfEmptyCollection
  SimpleDateFormatMissingLocale
  StatelessSingleton
  ToStringReturnsNull

  // rulesets/dry.xml
  DuplicateListLiteral { enabled = false }
  DuplicateMapLiteral { enabled = false }
  DuplicateNumberLiteral { enabled = false }
  DuplicateStringLiteral { enabled = false }

  // rulesets/enhanced.xml
  CloneWithoutCloneable { enabled = false }
  JUnitAssertEqualsConstantActualValue { enabled = false }
  MissingOverrideAnnotation { enabled = false }
  UnsafeImplementationAsMap { enabled = false }

  // rulesets/exceptions.xml
  CatchArrayIndexOutOfBoundsException
  CatchError
  CatchException
  CatchIllegalMonitorStateException
  CatchIndexOutOfBoundsException
  CatchNullPointerException
  CatchRuntimeException
  CatchThrowable
  ConfusingClassNamedException
  ExceptionExtendsError
  ExceptionExtendsThrowable
  ExceptionNotThrown
  MissingNewInThrowStatement
  ReturnNullFromCatchBlock
  SwallowThreadDeath
  ThrowError
  ThrowException
  ThrowNullPointerException
  ThrowRuntimeException
  ThrowThrowable

  // rulesets/formatting.xml
  BlankLineBeforePackage
  BlockEndsWithBlankLine
  BlockStartsWithBlankLine
  BracesForClass
  BracesForForLoop
  BracesForIfElse
  BracesForMethod
  BracesForTryCatchFinally
  ClassEndsWithBlankLine { enabled = false }
  ClassStartsWithBlankLine { enabled = false }
  ClosureStatementOnOpeningLineOfMultipleLineClosure
  ConsecutiveBlankLines
  FileEndsWithoutNewline
  Indentation { spacesPerIndentLevel = 2 }
  LineLength { length = 210 }
  MissingBlankLineAfterImports
  MissingBlankLineAfterPackage
  SpaceAfterCatch
  SpaceAfterClosingBrace
  SpaceAfterComma
  SpaceAfterFor
  SpaceAfterIf
  SpaceAfterOpeningBrace
  SpaceAfterSemicolon
  SpaceAfterSwitch
  SpaceAfterWhile
  SpaceAroundClosureArrow
  SpaceAroundMapEntryColon
  SpaceAroundOperator
  SpaceBeforeClosingBrace
  SpaceBeforeOpeningBrace
  TrailingWhitespace
}
