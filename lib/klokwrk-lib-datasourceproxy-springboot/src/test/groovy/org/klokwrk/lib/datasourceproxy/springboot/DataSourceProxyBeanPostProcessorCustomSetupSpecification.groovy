/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.datasourceproxy.springboot

import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper
import org.springframework.context.ApplicationContext
import org.springframework.test.context.MergedContextConfiguration
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate
import org.springframework.test.context.support.AnnotationConfigContextLoader
import org.springframework.test.context.support.DefaultBootstrapContext
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

@JdbcTest
class DataSourceProxyBeanPostProcessorCustomSetupSpecification extends Specification {
  /**
   * Creates a new Spring Boot application context for this test class.
   * </p>
   * Creation of this new application context can be triggered from any test method which provides the ability to influence on the Environment. In combination with java system properties, we now
   * have the ability to exercise and test various combinations of configuration properties. This is in contrast with standard spring means where variations in properties used in tests are allowed
   * to be specified only on test class level.
   * <p/>
   * Should be used sparingly for performance reasons. It is much better to use standard Spring (Boot) means if appropriate.
   */
  ApplicationContext createNewTestApplicationContext() {
    SpringBootTestContextBootstrapper contextBootstrapper = new SpringBootTestContextBootstrapper()
    contextBootstrapper.bootstrapContext = new DefaultBootstrapContext(DataSourceProxyBeanPostProcessorCustomSetupSpecification, new DefaultCacheAwareContextLoaderDelegate())
    MergedContextConfiguration contextConfiguration = contextBootstrapper.buildMergedContextConfiguration()
    ApplicationContext applicationContext = new AnnotationConfigContextLoader().loadContext(contextConfiguration)

    return applicationContext
  }

  void "should be enabled by default"() {
    given:
    ApplicationContext applicationContext = createNewTestApplicationContext()

    expect:
    //noinspection GroovyAssignabilityCheck,GrUnresolvedAccess
    applicationContext.getBean("dataSource").properties.advisors[0].advice.getClass() == DataSourceProxyInterceptor
  }

  @RestoreSystemProperties
  void "should be disabled when configured so"() {
    given:
    System.setProperty("klokwrk.datasourceproxy.enabled", "false")
    ApplicationContext applicationContext = createNewTestApplicationContext()

    expect:
    applicationContext.getBean("dataSource").properties.advisors == null
  }
}
