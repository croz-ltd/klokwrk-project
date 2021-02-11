/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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

import groovy.transform.CompileStatic
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.klokwrk.lib.datasourceproxy.Slf4jFilterableQueryLoggingListener
import org.springframework.util.ReflectionUtils

import javax.sql.DataSource
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

/**
 * AOP alliance proxy interceptor that decorates all invocations of {@link DataSource} instance methods with datasourceproxy features.
 * <p/>
 * Some aspects of datasourceproxy features can be configured via {@link DataSourceProxyConfigurationProperties}.
 * <p/>
 * Integration with Spring Boot application is implemented via {@link DataSourceProxyBeanPostProcessor}
 *
 * @see DataSourceProxyConfigurationProperties
 * @see DataSourceProxyBeanPostProcessor
 */
@CompileStatic
class DataSourceProxyInterceptor implements MethodInterceptor {
  DataSource dataSource

  DataSourceProxyInterceptor(String originalDataSourceBeanName, DataSource dataSource, DataSourceProxyConfigurationProperties dataSourceProxyConfigurationProperties) {
    Slf4jFilterableQueryLoggingListener slf4jFilterableQueryLoggingListener =
        new Slf4jFilterableQueryLoggingListener(dataSourceProxyConfigurationProperties.queryLogger.filteringOutRegularExpressionList)

    slf4jFilterableQueryLoggingListener.logLevel = dataSourceProxyConfigurationProperties.queryLogger.logLevel
    slf4jFilterableQueryLoggingListener.logger = dataSourceProxyConfigurationProperties.queryLogger.name

    this.dataSource = ProxyDataSourceBuilder
        .create(dataSource)
        .name("${dataSourceProxyConfigurationProperties.dataSourceNamePrefix}${originalDataSourceBeanName}")
        .listener(slf4jFilterableQueryLoggingListener)
        .logSlowQueryBySlf4j(
            dataSourceProxyConfigurationProperties.slowQueryLogger.threshold.toMillis(), TimeUnit.MILLISECONDS,
            dataSourceProxyConfigurationProperties.slowQueryLogger.logLevel,
            dataSourceProxyConfigurationProperties.slowQueryLogger.name
        )
        .build()
  }

  @Override
  Object invoke(MethodInvocation invocation) {
    Method proxyMethod = ReflectionUtils.findMethod(dataSource.class, invocation.method.name)
    if (proxyMethod) {
      return proxyMethod.invoke(dataSource, invocation.arguments)
    }

    return invocation.proceed()
  }
}
