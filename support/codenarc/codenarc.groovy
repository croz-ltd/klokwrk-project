final TEST_FILES = ".*/(test|testIntegration)/.*\\.groovy"

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
  EmptyMethod {
    doNotApplyToFilesMatching = TEST_FILES
  }
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
  ParameterAssignmentInFilterClosure
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
  CompileStatic {
    doNotApplyToFilesMatching = TEST_FILES
  }
  ConfusingTernary
  CouldBeElvis
  CouldBeSwitchStatement
  FieldTypeRequired
  HashtableIsObsolete
  IfStatementCouldBeTernary { enabled = false }
  ImplicitClosureParameter { enabled = false }
  ImplicitReturnStatement
  InvertedCondition { enabled = false }
  InvertedIfElse
  LongLiteralWithLowerCaseL
  MethodParameterTypeRequired
  MethodReturnTypeRequired
  NoDef {
    doNotApplyToFilesMatching = TEST_FILES
  }
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
  VariableTypeRequired {
    doNotApplyToFilesMatching = TEST_FILES
  }
  VectorIsObsolete

  // rulesets/design.xml
  AbstractClassWithPublicConstructor
  AbstractClassWithoutAbstractMethod {
    doNotApplyToFilesMatching = TEST_FILES
  }
  AssignmentToStaticFieldFromInstanceMethod
  BooleanMethodReturnsNull
  BuilderMethodWithSideEffects {
    doNotApplyToFilesMatching = TEST_FILES
  }
  CloneableWithoutClone
  CloseWithoutCloseable
  CompareToWithoutComparable
  ConstantsOnlyInterface
  EmptyMethodInAbstractClass
  FinalClassWithProtectedMember
  ImplementationAsType
  Instanceof {
    doNotApplyToFilesMatching = TEST_FILES
  }
  LocaleSetDefault
  NestedForLoop
  OptionalCollectionReturnType
  OptionalField
  OptionalMethodParameter
  PrivateFieldCouldBeFinal
  PublicInstanceField
  ReturnsNullInsteadOfEmptyArray
  ReturnsNullInsteadOfEmptyCollection
  SimpleDateFormatMissingLocale
  StatelessSingleton
  ToStringReturnsNull

  // rulesets/dry.xml
  DuplicateListLiteral {
    doNotApplyToFilesMatching = TEST_FILES
  }
  DuplicateMapLiteral {
    doNotApplyToFilesMatching = TEST_FILES
  }
  DuplicateNumberLiteral {
    doNotApplyToFilesMatching = TEST_FILES
    ignoreNumbers = "0,1,1024"
  }
  DuplicateStringLiteral {
    doNotApplyToFilesMatching = TEST_FILES
    ignoreStrings = "|.|,|0|1|-1|/"
    ignoreStringsDelimiter = "|"
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
  BracesForMethod {
    sameLine = true
    allowBraceOnNextLineForMultilineDeclarations = true
  }
  BracesForTryCatchFinally
  ClassEndsWithBlankLine { enabled = false }
  ClassStartsWithBlankLine { enabled = false }
  ClosureStatementOnOpeningLineOfMultipleLineClosure
  ConsecutiveBlankLines
  FileEndsWithoutNewline
  Indentation { spacesPerIndentLevel = 2 }
  LineLength {
    name = "LineLength - main"
    description = "For 'main' source set, checks the maximum length for each line of source code, which is 210 characters. However, 'only' 200 characters should be really used. Extra 10 characters " +
                  "should be used only for special cases."
    doNotApplyToFilesMatching = TEST_FILES
    length = 210
  }
  LineLength {
    name = "LineLength - test"
    description = "For all 'test*' source sets, checks the maximum length for each line of source code., which is 210 characters. However, 'only' 200 characters should be really used. Extra 10 " +
                  "characters should be used only for special cases. Besides, expressions used for equality comparison and line comments are ignored and can have any length."
    applyToFilesMatching = TEST_FILES
    length = 210
    ignoreLineRegex = /^.*(==~?|\/\/).*$/ // ignore for equality comparisons, matches comparisons and line comments in tests
  }
  MissingBlankLineAfterImports
  MissingBlankLineAfterPackage
  MissingBlankLineBeforeAnnotatedField
  SpaceAfterCatch
  SpaceAfterClosingBrace
  SpaceAfterComma
  SpaceAfterFor
  SpaceAfterIf
  SpaceAfterNotOperator
  SpaceAfterMethodCallName
  SpaceAfterMethodDeclarationName
  SpaceAfterOpeningBrace {
    ignoreEmptyBlock = true
  }
  SpaceAfterSemicolon
  SpaceAfterSwitch
  SpaceAfterWhile
  SpaceAroundClosureArrow
  SpaceAroundMapEntryColon {
    characterBeforeColonRegex = /\S/
    characterAfterColonRegex = /\s/
  }
  SpaceAroundOperator
  SpaceBeforeClosingBrace {
    ignoreEmptyBlock = true
  }
  SpaceBeforeOpeningBrace
  SpaceInsideParentheses
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
  GrailsDomainGormMethods { enabled = false }
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
  FactoryMethodName
  FieldName {
    ignoreFieldNames = "serialVersionUID,log,logger"
  }
  InterfaceName
  InterfaceNameSameAsSuperInterface
  MethodName {
    name = "MethodName - main"
    description = "For 'main' source set, verifies that the name of each method matches a regular expression. It checks that the method name starts with a lowercase letter and contains only 'word' " +
                  "characters."
    doNotApplyToFilesMatching = TEST_FILES
  }
  MethodName {
    name = "MethodName - test"
    description = "For all 'test*' source sets, verifies that the name of each method matches a regular expression. It checks that the method name starts with a lowercase letter and, beside " +
                  "'word characters', allows special characters like parenthesis, square brackets, hash sign, colon, white space, comma, dot and dash."
    applyToFilesMatching = TEST_FILES
    regex = /[a-z]([\w\[\]#:\s(),.-]|\'|\/)*/ // Test method names can include various unusual characters like []#:(),.-' . This is especially true for unrolled Spock methods.
  }
  ObjectOverrideMisspelledMethodName
  PackageName
  PackageNameMatchesFilePath
  ParameterName
  PropertyName
  VariableName

  // rulesets/security.xml
  FileCreateTempFile
  InsecureRandom
  JavaIoPackageAccess {
    doNotApplyToFilesMatching = TEST_FILES
  }
  NonFinalPublicField
  NonFinalSubclassOfSensitiveInterface
  ObjectFinalize
  PublicFinalizeMethod
  SystemExit
  UnsafeArrayDeclaration

  // rulesets/serialization.xml
  EnumCustomSerializationIgnored
  SerialPersistentFields
  SerialVersionUID
  SerializableClassMustDefineSerialVersionUID

  // rulesets/size.xml
  AbcMetric   // Requires the GMetrics jar
  ClassSize
  CrapMetric {   // Requires the GMetrics jar and a Cobertura coverage file
    enabled = false
  }
  CyclomaticComplexity   // Requires the GMetrics jar
  MethodCount
  MethodSize
  NestedBlockDepth
  ParameterCount

  // rulesets/unnecessary.xml
  AddEmptyString
  ConsecutiveLiteralAppends
  ConsecutiveStringConcatenation
  UnnecessaryBigDecimalInstantiation
  UnnecessaryBigIntegerInstantiation
  UnnecessaryBooleanExpression
  UnnecessaryBooleanInstantiation
  UnnecessaryCallForLastElement
  UnnecessaryCallToSubstring
  UnnecessaryCast
  UnnecessaryCatchBlock
  UnnecessaryCollectCall { enabled = false }
  UnnecessaryCollectionCall
  UnnecessaryConstructor
  UnnecessaryDefInFieldDeclaration
  UnnecessaryDefInMethodDeclaration
  UnnecessaryDefInVariableDeclaration
  UnnecessaryDotClass
  UnnecessaryDoubleInstantiation
  UnnecessaryElseStatement
  UnnecessaryFinalOnPrivateMethod
  UnnecessaryFloatInstantiation
  UnnecessaryGString { enabled = false }
  UnnecessaryGetter {
    checkIsMethods = false
  }
  UnnecessaryIfStatement {
    checkLastStatementImplicitElse = false
  }
  UnnecessaryInstanceOfCheck
  UnnecessaryInstantiationToGetClass
  UnnecessaryIntegerInstantiation
  UnnecessaryLongInstantiation
  UnnecessaryModOne
  UnnecessaryNullCheck
  UnnecessaryNullCheckBeforeInstanceOf
  UnnecessaryObjectReferences
  UnnecessaryOverridingMethod
  UnnecessaryPackageReference
  UnnecessaryParenthesesForMethodCallWithClosure
  UnnecessaryPublicModifier
  UnnecessaryReturnKeyword { enabled = false }
  UnnecessarySafeNavigationOperator
  UnnecessarySelfAssignment
  UnnecessarySemicolon
  UnnecessarySetter
  UnnecessaryStringInstantiation
  UnnecessarySubstring
  UnnecessaryTernaryExpression
  UnnecessaryToString
  UnnecessaryTransientModifier { enabled = false }

  // rulesets/unused.xml
  UnusedArray
  UnusedMethodParameter
  UnusedObject {
    doNotApplyToFilesMatching = TEST_FILES
  }
  UnusedPrivateField
  UnusedPrivateMethod
  UnusedPrivateMethodParameter
  UnusedVariable
}
