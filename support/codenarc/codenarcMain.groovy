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

  // rulesets/dry.xml
  DuplicateListLiteral
  DuplicateMapLiteral
  DuplicateNumberLiteral
  DuplicateStringLiteral {
    ignoreStrings = ", ,."
  }

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
  ClosureStatementOnOpeningLineOfMultipleLineClosure {
    enabled = false // TODO dmurat: check when new CodeNarc is release. Currently it is not compatible with Closure Lambda syntax supported by Groovy 3.
  }
  ConsecutiveBlankLines
  FileEndsWithoutNewline
  Indentation { spacesPerIndentLevel = 2 }  // TODO dmurat: after new CodeNarc release, check SuppressWarnings for this rule. Currently it is not compatible with Closure Lambda syntax supported by Groovy 3.
  LineLength { length = 210 }
  MissingBlankLineAfterImports
  MissingBlankLineAfterPackage
  SpaceAfterCatch
  SpaceAfterClosingBrace
  SpaceAfterComma
  SpaceAfterFor
  SpaceAfterIf
  SpaceAfterOpeningBrace {
    enabled = false // TODO dmurat: check when new CodeNarc is release. Currently it is not compatible with Closure Lambda syntax supported by Groovy 3.
  }
  SpaceAfterSemicolon
  SpaceAfterSwitch
  SpaceAfterWhile
  SpaceAroundClosureArrow
  SpaceAroundMapEntryColon {
    enabled = false  // TODO dmurat: it looks like this rule does not work correctly with Groovy 3. Didn't tried, but maybe setting up codenarc compile classpath can help.
    characterBeforeColonRegex = /\S/
    characterAfterColonRegex = /\s/
  }
  SpaceAroundOperator
  SpaceBeforeClosingBrace
  SpaceBeforeOpeningBrace
  TrailingWhitespace

  // rulesets/generic.xml
  IllegalClassMember
  IllegalClassReference
  IllegalPackageReference
  IllegalRegex
  IllegalString
  IllegalSubclass
  RequiredRegex
  RequiredString
  StatelessClass

  // rulesets/grails.xml
  GrailsDomainHasEquals { enabled = false }
  GrailsDomainHasToString { enabled = false }
  GrailsDomainReservedSqlKeywordName { enabled = false }
  GrailsDomainStringPropertyMaxSize { enabled = false }
  GrailsDomainWithServiceReference { enabled = false }
  GrailsDuplicateConstraint { enabled = false }
  GrailsDuplicateMapping { enabled = false }
  GrailsMassAssignment { enabled = false }
  GrailsPublicControllerMethod { enabled = false }
  GrailsServletContextReference { enabled = false }
  GrailsStatelessService { enabled = false }

  // rulesets/groovyism.xml
  AssignCollectionSort
  AssignCollectionUnique
  ClosureAsLastMethodParameter { enabled = false }
  CollectAllIsDeprecated
  ConfusingMultipleReturns
  ExplicitArrayListInstantiation
  ExplicitCallToAndMethod
  ExplicitCallToCompareToMethod
  ExplicitCallToDivMethod
  ExplicitCallToEqualsMethod
  ExplicitCallToGetAtMethod
  ExplicitCallToLeftShiftMethod
  ExplicitCallToMinusMethod
  ExplicitCallToModMethod
  ExplicitCallToMultiplyMethod
  ExplicitCallToOrMethod
  ExplicitCallToPlusMethod
  ExplicitCallToPowerMethod
  ExplicitCallToPutAtMethod
  ExplicitCallToRightShiftMethod
  ExplicitCallToXorMethod
  ExplicitHashMapInstantiation
  ExplicitHashSetInstantiation
  ExplicitLinkedHashMapInstantiation
  ExplicitLinkedListInstantiation
  ExplicitStackInstantiation
  ExplicitTreeSetInstantiation
  GStringAsMapKey
  GStringExpressionWithinString
  GetterMethodCouldBeProperty
  GroovyLangImmutable
  UseCollectMany
  UseCollectNested

  // rulesets/imports.xml
  DuplicateImport
  ImportFromSamePackage
  ImportFromSunPackages
  MisorderedStaticImports { comesBefore = false }
  NoWildcardImports
  UnnecessaryGroovyImport
  UnusedImport

  // rulesets/jdbc.xml
  DirectConnectionManagement
  JdbcConnectionReference
  JdbcResultSetReference
  JdbcStatementReference

  // rulesets/junit.xml
  ChainedTest
  CoupledTestCase
  JUnitAssertAlwaysFails
  JUnitAssertAlwaysSucceeds
  JUnitFailWithoutMessage
  JUnitLostTest
  JUnitPublicField
  JUnitPublicNonTestMethod
  JUnitPublicProperty
  JUnitSetUpCallsSuper
  JUnitStyleAssertions
  JUnitTearDownCallsSuper
  JUnitTestMethodWithoutAssert
  JUnitUnnecessarySetUp
  JUnitUnnecessaryTearDown
  JUnitUnnecessaryThrowsException
  SpockIgnoreRestUsed
  UnnecessaryFail
  UseAssertEqualsInsteadOfAssertTrue
  UseAssertFalseInsteadOfNegation
  UseAssertNullInsteadOfAssertEquals
  UseAssertSameInsteadOfAssertTrue
  UseAssertTrueInsteadOfAssertEquals
  UseAssertTrueInsteadOfNegation

  // rulesets/logging.xml
  LoggerForDifferentClass
  LoggerWithWrongModifiers
  LoggingSwallowsStacktrace
  MultipleLoggers
  PrintStackTrace
  Println
  SystemErrPrint
  SystemOutPrint

  // rulesets/naming.xml
  AbstractClassName
  ClassName
  ClassNameSameAsFilename
  ClassNameSameAsSuperclass
  ConfusingMethodName
  FactoryMethodName {
    regex = /(build.*|make.*)/
  }
  FieldName {
    ignoreFieldNames = "serialVersionUID,log,logger"
  }
  InterfaceName
  InterfaceNameSameAsSuperInterface
  MethodName
  ObjectOverrideMisspelledMethodName
  PackageName
  PackageNameMatchesFilePath
  ParameterName
  PropertyName
  VariableName
}
