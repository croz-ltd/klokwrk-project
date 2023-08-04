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
package org.klokwrk.cargotracker.booking.queryside.view.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import net.croz.nrich.search.api.converter.StringToEntityPropertyMapConverter
import net.croz.nrich.search.api.converter.StringToTypeConverter
import net.croz.nrich.search.api.factory.RepositoryFactorySupportFactory
import net.croz.nrich.search.api.factory.SearchExecutorJpaRepositoryFactoryBean
import net.croz.nrich.search.converter.DefaultStringToEntityPropertyMapConverter
import net.croz.nrich.search.converter.DefaultStringToTypeConverter
import net.croz.nrich.search.factory.SearchRepositoryFactorySupportFactory
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories(
    repositoryFactoryBeanClass = SearchExecutorJpaRepositoryFactoryBean,
    basePackages = [
        "org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.adapter.out.persistence",
        "org.klokwrk.lib.hi.spring.data.jpa.repository.hibernate"
    ]
)
@EntityScan(basePackages = ["org.klokwrk.cargotracker.booking.queryside.model.rdbms.jpa"])
@Configuration(proxyBeanMethods = false)
@CompileStatic
class SpringDataJpaConfig {
  // Technically RepositoryFactorySupportFactory bean is not needed because we are not using StringSearchExecutor at all, so we don't need StringToEntityPropertyMapConverter (it can be null).
  // However, it is useful as an example for customization.
  // Alternatively, spring boot configuration properties may be used as described in https://github.com/croz-ltd/nrich/blob/master/nrich-search-spring-boot-starter/README.md .
  @Bean
  RepositoryFactorySupportFactory repositoryFactorySupportFactory() {
    List<String> dateFormatList = ["yyyy-MM-dd'T'HH:mm:ss'Z'"]
    List<String> decimalFormatList = ["#0.00"]
    String booleanTrueRegexPattern = /^(?i)\s*(true|yes)\s*$/
    String booleanFalseRegexPattern = /^(?i)\s*(false|no)\s*$/
    StringToTypeConverter<?> stringToTypeConverter = new DefaultStringToTypeConverter(dateFormatList, decimalFormatList, booleanTrueRegexPattern, booleanFalseRegexPattern)

    StringToEntityPropertyMapConverter stringToEntityPropertyMapConverter =
        new DefaultStringToEntityPropertyMapConverter([stringToTypeConverter] as List<StringToTypeConverter<?>>) // codenarc-disable-line UnnecessaryCast

    RepositoryFactorySupportFactory repositoryFactorySupportFactory = new SearchRepositoryFactorySupportFactory(stringToEntityPropertyMapConverter)

    return repositoryFactorySupportFactory
  }
}
