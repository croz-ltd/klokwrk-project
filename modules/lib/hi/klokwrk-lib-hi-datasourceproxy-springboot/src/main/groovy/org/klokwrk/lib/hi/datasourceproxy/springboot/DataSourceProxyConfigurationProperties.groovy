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
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel
import org.springframework.boot.context.properties.ConfigurationProperties

import java.time.Duration

/**
 * Spring Boot configuration properties for configuring some aspects of datasourceproxy features that are enabled via {@link DataSourceProxyBeanPostProcessor} and {@link DataSourceProxyInterceptor}.
 *
 * @see DataSourceProxyBeanPostProcessor
 * @see DataSourceProxyInterceptor
 */
@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = "klokwrk.datasourceproxy")
@CompileStatic
class DataSourceProxyConfigurationProperties {
  /**
   * By default data source proxy is enabled. Set to <code>false</code> to disable it.
   */
  boolean enabled = true

  /**
   * The prefix of a proxying data source name.
   * <p/>
   * Constructed full data source name will consist of this prefix and original bean name of a proxied data source.
   * <p/>
   * Default value is <code>'datasourceproxy:::'</code>.
   */
  String dataSourceNamePrefix = "datasourceproxy:::"

  @SuppressWarnings("unused")
  QueryLogger queryLogger = new QueryLogger()

  class QueryLogger {
    /**
     * The name of query logger as it will be seen in the log.
     * <p/>
     * Default value is <code>'klokwrk.datasourceproxy.queryLogger'</code>.
     */
    String name = "klokwrk.datasourceproxy.queryLogger"

    /**
     * SLF4j log level to be used for queryLogger.
     * <p/>
     * Default value is <code>DEBUG</code>.
     */
    SLF4JLogLevel logLevel = SLF4JLogLevel.DEBUG

    /**
     * The list of regular expression strings that will filter out matching query string from the log.
     * <p/>
     * Default value is empty list.
     * <p/>
     * Do note that filtered out entries are still logged if query logger's log level is configured to <code>TRACE</code>.
     */
    List<String> filteringOutRegularExpressionList = []
  }

  @SuppressWarnings("unused")
  SlowQueryLogger slowQueryLogger = new SlowQueryLogger()

  class SlowQueryLogger {
    /**
     * The name of slow query logger as it will be seen in the log.
     * <p/>
     * Default value is <code>'klokwrk.datasourceproxy.slowQueryLogger'</code>.
     */
    String name = "klokwrk.datasourceproxy.slowQueryLogger"

    /**
     * SLF4j log level to be used for slowQueryLogger.
     * <p/>
     * Default value is <code>WARN</code>.
     */
    SLF4JLogLevel logLevel = SLF4JLogLevel.WARN

    /**
     * Duration that represents a threshold after which query is interpreted as slow query.
     * <p/>
     * Default value is 1000 millis.
     */
    Duration threshold = Duration.ofMillis(1000)
  }
}
