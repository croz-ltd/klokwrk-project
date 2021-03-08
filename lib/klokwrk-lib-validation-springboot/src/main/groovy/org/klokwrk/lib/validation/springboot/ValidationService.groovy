package org.klokwrk.lib.validation.springboot

import groovy.transform.CompileStatic
import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource

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
class ValidationService {
  Boolean enabled
  String[] messageSourceBaseNames
  Validator validator

  ValidationService(ValidationConfigurationProperties validationConfigurationProperties) {
    this(validationConfigurationProperties.enabled, validationConfigurationProperties.messageSourceBaseNames)
  }

  ValidationService(Boolean enabled, String[] messageSourceBaseNames) {
    this.enabled = enabled
    this.messageSourceBaseNames = messageSourceBaseNames

    MessageSource messageSource = new ResourceBundleMessageSource()
    messageSource.basenames = this.messageSourceBaseNames
    messageSource.defaultEncoding = "UTF-8"

    KlokwrkLocalValidatorFactoryBean localValidatorFactoryBean = new KlokwrkLocalValidatorFactoryBean()
    localValidatorFactoryBean.validationMessageSource = messageSource
    localValidatorFactoryBean.afterPropertiesSet()

    this.validator = localValidatorFactoryBean
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
