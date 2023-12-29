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
package org.klokwrk.lib.hi.datasourceproxy.springboot

import groovy.transform.CompileStatic
import net.ttddyy.dsproxy.support.ProxyDataSource
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.BeanPostProcessor

import javax.sql.DataSource

/**
 * Spring bean post-processor that sets up <code>datasourceproxy</code> intercepting for all <code>DataSource</code> beans from the application context.
 * <p/>
 * To use datasourceproxy features from a Spring Boot application, this bean post processor needs to be configured as a bean, and accompanying {@link DataSourceProxyConfigurationProperties} needs
 * to be enabled similar to the following example:
 * <pre>
 * &#64;EnableConfigurationProperties(DataSourceProxyConfigurationProperties)
 * &#64;Configuration
 * class SpringBootConfig {
 *   &#64;Bean
 *   static BeanPostProcessor dataSourceProxyBeanPostProcessor(ObjectProvider&lt;DataSourceProxyConfigurationProperties&gt; dataSourceProxyConfigurationPropertiesObjectProvider) {
 *     return new DataSourceProxyBeanPostProcessor(dataSourceProxyConfigurationPropertiesObjectProvider)
 *   }
 * }
 * </pre>
 * This will configure and enable datasourceproxy's "query logging" and "slow query logging" features. Some aspects of these datasourceproxy features can be configured via
 * {@link DataSourceProxyConfigurationProperties} configuration properties. Usually, it is enough to configure appropriate loggers like in following example:
 * <pre>
 * logging.level.klokwrk.datasourceproxy.queryLogger: DEBUG
 * logging.level.klokwrk.datasourceproxy.slowQueryLogger: WARN
 * </pre>
 * For more configuration options, take a look at the documentation of {@link DataSourceProxyConfigurationProperties}.
 * <p/>
 * It is worth mentioning that datasourceproxy's "query logging" feature is expanded by supporting filtering of queries that one wants to remove from the log. This is useful when we have reoccurring
 * queries, like in polling-a-database scenarios. Queries that need to be filtered out are specified as a list of regular expressions. When query string matches regular expression, it won't be
 * present in the log output. For example, following configuration will prevent logging of any updates to the <code>token_entry</code> table.
 * <pre>
 * klokwrk.datasourceproxy.query-logger.filtering-out-regular-expression-list: &gt;
 *   ^update token_entry.*$
 * </pre>
 * This 'filterable query logging' feature is implemented in <code>org.klokwrk.lib.lo.datasourceproxy.Slf4jFilterableQueryLoggingListener</code>.
 *
 * @see DataSourceProxyConfigurationProperties
 * @see DataSourceProxyInterceptor
 */
@CompileStatic
class DataSourceProxyBeanPostProcessor implements BeanPostProcessor {
  ObjectProvider<DataSourceProxyConfigurationProperties> dataSourceProxyConfigurationPropertiesObjectProvider

  DataSourceProxyBeanPostProcessor(ObjectProvider<DataSourceProxyConfigurationProperties> dataSourceProxyConfigurationPropertiesObjectProvider) {
    this.dataSourceProxyConfigurationPropertiesObjectProvider = dataSourceProxyConfigurationPropertiesObjectProvider
  }

  @SuppressWarnings("CodeNarc.Instanceof")
  @Override
  Object postProcessAfterInitialization(Object bean, String beanName) {
    if (bean instanceof DataSource && !(bean instanceof ProxyDataSource)) {
      DataSourceProxyConfigurationProperties dataSourceProxyConfigurationProperties = dataSourceProxyConfigurationPropertiesObjectProvider.object
      if (!dataSourceProxyConfigurationProperties.enabled) {
        return bean
      }

      ProxyFactory aopProxyFactory = new ProxyFactory(bean)
      aopProxyFactory.proxyTargetClass = true
      aopProxyFactory.addAdvice(new DataSourceProxyInterceptor(beanName, bean, dataSourceProxyConfigurationProperties))

      return aopProxyFactory.proxy
    }

    return bean
  }
}
