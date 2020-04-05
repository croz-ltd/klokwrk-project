package net.croz.cargotracker.infrastructure.shared.springboot.jackson

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@EnableConfigurationProperties(EssentialJacksonCustomizerConfigurationProperties)
@SpringBootApplication
class TestSpringBootApplication {
  static void main(String[] args) {
    SpringApplication.run(TestSpringBootApplication, args)
  }

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  EssentialJacksonCustomizer essentialJacksonCustomizer(EssentialJacksonCustomizerConfigurationProperties essentialJacksonCustomizerConfigurationProperties) {
    return new EssentialJacksonCustomizer(essentialJacksonCustomizerConfigurationProperties)
  }
}
