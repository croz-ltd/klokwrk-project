package org.klokwrk.cargotracker.booking.queryside.view.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories(
    basePackages = ["org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.adapter.out.persistence", "org.klokwrk.lib.springframework.data.jpa.repository.hibernate"]
)
@EntityScan(basePackages = ["org.klokwrk.cargotracker.booking.queryside.model.rdbms.jpa"])
@Configuration(proxyBeanMethods = false)
@CompileStatic
class SpringDataJpaConfig {
}
