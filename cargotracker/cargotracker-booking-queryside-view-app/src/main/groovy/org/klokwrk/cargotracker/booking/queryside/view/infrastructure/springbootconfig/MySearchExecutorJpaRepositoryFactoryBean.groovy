/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
import net.croz.nrich.search.converter.DefaultStringToEntityPropertyMapConverter
import net.croz.nrich.search.converter.DefaultStringToTypeConverter
import net.croz.nrich.search.factory.SearchRepositoryFactorySupportFactory
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean
import org.springframework.data.repository.Repository
import org.springframework.data.repository.core.support.RepositoryFactorySupport

import javax.persistence.EntityManager

/**
 * The modification of {@code nrich-search} {@code SearchExecutorJpaRepositoryFactoryBean} that does not fail when {@code beanFactory.getBean()} methods are called during beanFactory initialization
 * (i.e., from Spring Boot auto-configurations executed after {@code nrich-search-spring-boot-starter} auto-configuration).
 * <p/>
 * Here are some more details useful for recreation of the issue.
 * <p/>
 * The {@code nrich-search} {@code SearchExecutorJpaRepositoryFactoryBean} contains a single constructor that requires two parameters, the {@code Class} for repository interface and
 * {@code nrich-search} specific {@code RepositoryFactorySupportFactory} instance. The problem is that Spring Data expects the class extended from {@code RepositoryFactoryBeanSupport} with the
 * constructor with a single parameter - the {@code Class} of repository interface (see {@code org.springframework.data.repository.config.RepositoryBeanDefinitionBuilder.build()}). That leaves the
 * second constructor unresolved when the corresponding bean definition is created during the initialization of Spring's bean factory.
 * <p/>
 * In some cases, this works, but if any of {@code beanFactory.getBean()} methods are called during bean factory initialization, creation of all defined beans is triggered. If we have a bean
 * definition with unresolved constructor parameters at this point, Spring throws {@code UnsatisfiedDependencyException} complaining bout ambiguous constructor argument values.
 * <p/>
 * In my case, above behavior is triggered from Axon Framework's {@code AbstractQualifiedBeanCondition.getMatchOutcome()} method, which calls {@code beanFactory.getBeanNamesForType()}.
 * <p/>
 * For those reasons, {@code MySearchExecutorJpaRepositoryFactoryBean} contains only a single argument constructor expected by Spring Data and resolves additional arguments in the
 * {@code afterPropertiesSet()} method.
 */
@CompileStatic
class MySearchExecutorJpaRepositoryFactoryBean<T extends Repository<S, I>, S, I> extends JpaRepositoryFactoryBean<T, S, I> {
  private final Class<? extends T> repositoryInterface

  private RepositoryFactorySupportFactory repositoryFactorySupportFactory
  private ListableBeanFactory listableBeanFactory

  MySearchExecutorJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
    super(repositoryInterface)
    this.repositoryInterface = repositoryInterface

    List<String> dateFormatList = ["yyyy-MM-dd'T'HH:mm:ss'Z'"]
    List<String> decimalFormatList = ["#0.00"]
    String booleanTrueRegexPattern = /^(?i)\s*(true|yes)\s*$/
    String booleanFalseRegexPattern = /^(?i)\s*(false|no)\s*$/
    StringToTypeConverter<Object> stringToTypeConverter = new DefaultStringToTypeConverter(dateFormatList, decimalFormatList, booleanTrueRegexPattern, booleanFalseRegexPattern)

    StringToEntityPropertyMapConverter stringToEntityPropertyMapConverter =
        new DefaultStringToEntityPropertyMapConverter([stringToTypeConverter] as List<StringToTypeConverter<?>>) // codenarc-disable-line UnnecessaryCast

    this.repositoryFactorySupportFactory = new SearchRepositoryFactorySupportFactory(stringToEntityPropertyMapConverter)
  }

  @Override
  void setBeanFactory(BeanFactory beanFactory) {
    this.listableBeanFactory = beanFactory as ListableBeanFactory
    super.setBeanFactory(beanFactory)
  }

  @Override
  void afterPropertiesSet() {
    RepositoryFactorySupportFactory repositoryFactorySupportFactory = listableBeanFactory
        .getBeanNamesForType(RepositoryFactorySupportFactory)
        .collect({ String beanName -> listableBeanFactory.getBean(beanName, RepositoryFactorySupportFactory) })
        .find({ it })

    if (repositoryFactorySupportFactory) {
      this.repositoryFactorySupportFactory = listableBeanFactory.getBean(RepositoryFactorySupportFactory) // Check and throw if there are multiple non-primary beans
    }

    super.afterPropertiesSet()
  }

  @Override
  protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
    return repositoryFactorySupportFactory.createRepositoryFactory(repositoryInterface, entityManager)
  }
}
