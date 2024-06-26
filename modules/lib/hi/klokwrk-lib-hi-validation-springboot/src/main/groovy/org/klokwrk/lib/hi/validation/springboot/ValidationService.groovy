/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.hi.validation.springboot

import groovy.transform.CompileStatic
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ClassInfoList
import io.github.classgraph.ClassRefTypeSignature
import io.github.classgraph.ScanResult
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validator

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
  boolean enabled
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

    Map<Class, Set<Class>> constraintAnnotationToValidatorImplementationListMapping = makeConstraintAnnotationToValidatorImplementationListMapping(validatorImplementationPackagesToScan)

    KlokwrkLocalValidatorFactoryBean localValidatorFactoryBean = new KlokwrkLocalValidatorFactoryBean(constraintAnnotationToValidatorImplementationListMapping)
    localValidatorFactoryBean.validationMessageSource = makeMessageSource(this.messageSourceBaseNames)
    localValidatorFactoryBean.afterPropertiesSet()

    this.validator = localValidatorFactoryBean
  }

  protected MessageSource makeMessageSource(String[] messageSourceBaseNames) {
    MessageSource messageSource = new ResourceBundleMessageSource()
    messageSource.basenames = messageSourceBaseNames
    messageSource.defaultEncoding = "UTF-8"

    return messageSource
  }

  protected Map<Class, Set<Class>> makeConstraintAnnotationToValidatorImplementationListMapping(String[] validatorImplementationPackagesToScan) {
    Map<Class, Set<Class>> constraintAnnotationToValidatorImplementationListMapping = [:]

    ClassGraph validatorImplementationClassGraph = new ClassGraph()
        .enableClassInfo()
        .acceptPackages(validatorImplementationPackagesToScan)

    validatorImplementationClassGraph.scan().withCloseable { ScanResult scanResult ->
      ClassInfoList validatorImplementationClassInfoList = scanResult.getClassesImplementing(ConstraintValidator.name)
      validatorImplementationClassInfoList.each { ClassInfo validatorImplementationClassInfo ->
        String constraintAnnotationClassName = validatorImplementationClassInfo
            .typeSignature
            .superinterfaceSignatures
            .find({ ClassRefTypeSignature classRefTypeSignature -> classRefTypeSignature.baseClassName == ConstraintValidator.name })
            .typeArguments[0]

        Class validatorClass = validatorImplementationClassInfo.loadClass()
        Class constraintAnnotationClass = scanResult.loadClass(constraintAnnotationClassName, true)

        Set<Class> validatorImplementationList = constraintAnnotationToValidatorImplementationListMapping.get(constraintAnnotationClass, [] as Set)
        validatorImplementationList.add(validatorClass)
      }
    }

    return constraintAnnotationToValidatorImplementationListMapping
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
