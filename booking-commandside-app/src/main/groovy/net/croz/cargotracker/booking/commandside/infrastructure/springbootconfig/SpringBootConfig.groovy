package net.croz.cargotracker.booking.commandside.infrastructure.springbootconfig


import net.croz.cargotracker.infrastructure.shared.springboot.jackson.EssentialJacksonCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringBootConfig {

  @Bean
  EssentialJacksonCustomizer essentialJacksonCustomizer() {
    return new EssentialJacksonCustomizer()
  }
}
