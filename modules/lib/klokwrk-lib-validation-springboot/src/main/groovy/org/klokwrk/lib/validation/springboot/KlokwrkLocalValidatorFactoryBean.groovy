/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.validation.springboot

import groovy.transform.CompileStatic
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.HibernateValidatorFactory
import org.hibernate.validator.cfg.ConstraintMapping
import org.hibernate.validator.cfg.context.ConstraintDefinitionContext
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

import javax.validation.ClockProvider
import javax.validation.Configuration

/**
 * Extension of Spring {@link LocalValidatorFactoryBean} with customized configuration.
 */
@CompileStatic
class KlokwrkLocalValidatorFactoryBean extends LocalValidatorFactoryBean {
  Map<Class, Set<Class>> constraintAnnotationToValidatorImplementationListMapping

  KlokwrkLocalValidatorFactoryBean(Map<Class, Set<Class>> constraintAnnotationToValidatorImplementationListMapping = [:]) {
    this.constraintAnnotationToValidatorImplementationListMapping = constraintAnnotationToValidatorImplementationListMapping
  }

  @Override
  ClockProvider getClockProvider() {
    return unwrap(HibernateValidatorFactory).clockProvider
  }

  @Override
  protected void postProcessConfiguration(Configuration configuration) {
    HibernateValidatorConfiguration hibernateValidatorConfiguration = configuration as HibernateValidatorConfiguration
    ConstraintMapping hibernateValidatorConstraintMapping = hibernateValidatorConfiguration.createConstraintMapping()

    constraintAnnotationToValidatorImplementationListMapping.each { entry ->
      Class constraintAnnotationClass = entry.key
      Set<Class> validatorImplementationClassList = entry.value

      ConstraintDefinitionContext constraintDefinitionContext = hibernateValidatorConstraintMapping.constraintDefinition(constraintAnnotationClass)
      validatorImplementationClassList.each({ Class validatorImplementationClass -> constraintDefinitionContext.validatedBy(validatorImplementationClass) })
    }

    hibernateValidatorConfiguration.addMapping(hibernateValidatorConstraintMapping)
  }
}
