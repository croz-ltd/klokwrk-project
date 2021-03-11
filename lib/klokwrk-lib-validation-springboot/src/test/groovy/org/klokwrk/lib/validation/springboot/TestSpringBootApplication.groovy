package org.klokwrk.lib.validation.springboot

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@EnableConfigurationProperties(ValidationConfigurationProperties)
@SpringBootApplication
@CompileStatic
class TestSpringBootApplication {
  static void main(String[] args) {
    SpringApplication.run(TestSpringBootApplication, args)
  }

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  ValidationService validationService(ValidationConfigurationProperties validationConfigurationProperties) {
    return new ValidationService(validationConfigurationProperties)
  }
}
