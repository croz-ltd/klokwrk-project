package net.croz.cargotracker.infrastructure.shared.springboot.jackson

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class TestSpringBootApplication {
  static void main(String[] args) {
    SpringApplication.run(TestSpringBootApplication, args)
  }

  @Bean
  EssentialJacksonCustomizer essentialJacksonCustomizer() {
    return new EssentialJacksonCustomizer()
  }
}
