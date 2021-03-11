package org.klokwrk.lib.validation.springboot

import groovy.transform.CompileStatic
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.HibernateValidatorFactory
import org.hibernate.validator.cfg.ConstraintMapping
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

import javax.validation.ClockProvider
import javax.validation.Configuration

/**
 * Extension of Spring {@link LocalValidatorFactoryBean} with customized configuration.
 */
@CompileStatic
class KlokwrkLocalValidatorFactoryBean extends LocalValidatorFactoryBean {
  Map<Class, Class> validatorImplementationToConstraintAnnotationMapping

  KlokwrkLocalValidatorFactoryBean(Map<Class, Class> validatorImplementationToConstraintAnnotationMapping = [:]) {
    this.validatorImplementationToConstraintAnnotationMapping = validatorImplementationToConstraintAnnotationMapping
  }

  @Override
  ClockProvider getClockProvider() {
    return unwrap(HibernateValidatorFactory).clockProvider
  }

  @Override
  protected void postProcessConfiguration(Configuration configuration) {
    HibernateValidatorConfiguration hibernateValidatorConfiguration = configuration as HibernateValidatorConfiguration
    ConstraintMapping hibernateValidatorConstraintMapping = hibernateValidatorConfiguration.createConstraintMapping()

    validatorImplementationToConstraintAnnotationMapping.each { entry ->
      Class constraintAnnotationClass = entry.value
      Class validatorImplementationClass = entry.key

      hibernateValidatorConstraintMapping.constraintDefinition(constraintAnnotationClass).validatedBy(validatorImplementationClass)
    }

    hibernateValidatorConfiguration.addMapping(hibernateValidatorConstraintMapping)
  }
}
