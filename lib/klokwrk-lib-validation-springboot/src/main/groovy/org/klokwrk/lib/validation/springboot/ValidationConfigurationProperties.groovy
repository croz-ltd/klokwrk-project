package org.klokwrk.lib.validation.springboot

import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Spring Boot configuration properties for {@link ValidationService}.
 * <p/>
 * To be able to use this from Spring Boot application, minimal configuration is required that enables this configuration properties and creates {@link ValidationService} bean like in the following
 * example:
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
@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = "klokwrk.validation")
@CompileStatic
class ValidationConfigurationProperties {
  /**
   * By default ValidationService is enabled. Set to <code>false</code> to disable it.
   */
  Boolean enabled = true

  /**
   * The list of message source base names to be used with {@code ValidationService}. By default it includes only "klokwrkValidationHibernateMessages".
   */
  String[] messageSourceBaseNames = ["klokwrkValidationHibernateMessages"]
}
