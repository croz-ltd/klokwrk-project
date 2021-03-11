package org.klokwrk.lib.validation.springboot

import groovy.transform.CompileStatic
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ClassInfoList
import io.github.classgraph.ClassRefTypeSignature
import io.github.classgraph.ScanResult
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource

import javax.validation.ConstraintValidator
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Validator

/**
 * Validation service intended to be configured and used as a singleton bean from Spring Boot application.
 * <p/>
 * It creates and configures internal {@link KlokwrkLocalValidatorFactoryBean} that provides {@link Validator} interface which is used as underlying resource to which all validation requests are
 * delegated to.
 * <p/>
 * To be able to use this service from Spring Boot application, minimal configuration is required that creates {@link ValidationService} bean and enables corresponding
 * {@link ValidationConfigurationProperties}, like in the following example:
  * <pre>
  * &#64;EnableConfigurationProperties(ValidationConfigurationProperties)
  * &#64;Configuration
  * class SpringBootConfig {
  *   &#64;SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  *   &#64;Bean
  *   ValidationService validationService(ValidationConfigurationProperties validationConfigurationProperties) {
  *     return new ValidationService(validationConfigurationProperties)
  *   }
  * }
  * </pre>
 */
@CompileStatic
class ValidationService implements InitializingBean {
  Boolean enabled
  String[] messageSourceBaseNames
  String[] validatorImplementationPackagesToScan

  Validator validator

  ValidationService(ValidationConfigurationProperties validationConfigurationProperties) {
    this.enabled = validationConfigurationProperties.enabled
    this.messageSourceBaseNames = validationConfigurationProperties.messageSourceBaseNames
    this.validatorImplementationPackagesToScan = validationConfigurationProperties.validatorImplementationPackages
  }

  @Override
  void afterPropertiesSet() throws Exception {
    if (!enabled) {
      return
    }

    Map<Class, Class> validatorImplementationToConstraintAnnotationMapping = createValidatorImplementationToConstraintAnnotationMapping(validatorImplementationPackagesToScan)

    KlokwrkLocalValidatorFactoryBean localValidatorFactoryBean = new KlokwrkLocalValidatorFactoryBean(validatorImplementationToConstraintAnnotationMapping)
    localValidatorFactoryBean.validationMessageSource = createMessageSource(this.messageSourceBaseNames)
    localValidatorFactoryBean.afterPropertiesSet()

    this.validator = localValidatorFactoryBean
  }

  protected MessageSource createMessageSource(String[] messageSourceBaseNames) {
    MessageSource messageSource = new ResourceBundleMessageSource()
    messageSource.basenames = messageSourceBaseNames
    messageSource.defaultEncoding = "UTF-8"

    return messageSource
  }

  protected Map<Class, Class> createValidatorImplementationToConstraintAnnotationMapping(String[] validatorImplementationPackagesToScan) {
    Map<Class, Class> validatorImplementationToConstraintAnnotationMapping = [:]

    ClassGraph gradleSourceRepackClassGraph = new ClassGraph()
        .enableClassInfo()
        .acceptPackages(validatorImplementationPackagesToScan)

    gradleSourceRepackClassGraph.scan().withCloseable { ScanResult scanResult ->
      ClassInfoList generatedGroovyClosureClassInfoList = scanResult.getClassesImplementing(ConstraintValidator.name)
      generatedGroovyClosureClassInfoList.each { ClassInfo classInfo ->
        String constraintAnnotationClassName = classInfo
            .typeSignature
            .superinterfaceSignatures
            .find({ ClassRefTypeSignature classRefTypeSignature -> classRefTypeSignature.baseClassName == ConstraintValidator.name })
            .typeArguments[0]

        Class validatorClass = classInfo.loadClass()
        Class constraintAnnotationClass = scanResult.loadClass(constraintAnnotationClassName, true)

        validatorImplementationToConstraintAnnotationMapping.put(validatorClass, constraintAnnotationClass)
      }
    }

    return validatorImplementationToConstraintAnnotationMapping
  }

  /**
   * Validates provided {@code objectToValidate} by delegating to the internal {@link Validator} instance.
   */
  @SuppressWarnings('GrUnnecessaryPublicModifier')
  public <T> void validate(T objectToValidate) {
    if (!enabled) {
      return
    }

    Set<ConstraintViolation<T>> constraintViolationSet = validator.validate(objectToValidate)
    if (!constraintViolationSet.isEmpty()) {
      throw new ConstraintViolationException(constraintViolationSet)
    }
  }
}
