package org.klokwrk.lib.validation.springboot

import groovy.transform.CompileStatic
import org.hibernate.validator.HibernateValidatorFactory
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

import javax.validation.ClockProvider
import javax.validation.Configuration

/**
 * Extension of Spring {@link LocalValidatorFactoryBean} with customized configuration.
 */
@CompileStatic
class KlokwrkLocalValidatorFactoryBean extends LocalValidatorFactoryBean {
  @Override
  ClockProvider getClockProvider() {
    return unwrap(HibernateValidatorFactory).clockProvider
  }

  @Override
  protected void postProcessConfiguration(Configuration configuration) {
    // TODO dmurat: implement changes to the HibernateValidatorConfiguration
  }
}
