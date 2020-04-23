package net.croz.cargotracker.infrastructure.library.datasourceproxy.springboot

import groovy.transform.CompileStatic
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel
import org.springframework.boot.context.properties.ConfigurationProperties

import java.time.Duration

/**
 * Spring Boot configuration properties for {@link DataSourceProxyBeanPostProcessor} and {@link DataSourceProxyInterceptor}.
 * <p/>
 * To be able to use this from Spring Boot application minimal configuration is required that enables this configuration properties and configures accompanying bean post processor like in following
 * example:
 * <pre>
 * &#64;EnableConfigurationProperties(DataSourceProxyConfigurationProperties)
 * &#64;Configuration
 * class SpringBootConfig {
 *   &#64;SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
 *   &#64;Bean
 *   BeanPostProcessor dataSourceProxyBeanPostProcessor(DataSourceProxyConfigurationProperties dataSourceProxyConfigurationProperties) {
 *     return new DataSourceProxyBeanPostProcessor(dataSourceProxyConfigurationProperties)
 *   }
 * }
 * </pre>
 */
@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = "cargotracker.data-source-proxy")
@CompileStatic
class DataSourceProxyConfigurationProperties {
  /**
   * By default data source proxy is enabled. Set to <code>false</code> to disable it.
   */
  Boolean enabled = true

  /**
   * The name of proxied data source.
   * <p/>
   * Default value is <code>'dataSourceProxy'</code>.
   */
  String dataSourceName = "dataSourceProxy"

  @SuppressWarnings("unused")
  QueryLogger queryLogger = new QueryLogger()

  class QueryLogger {
    /**
     * The name of query logger as it will be seen in the log.
     * <p/>
     * Default value is <code>'cargotracker.data-source-proxy.queryLogger'</code>.
     */
    String name = "cargotracker.data-source-proxy.queryLogger"

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
     * Default value is <code>'cargotracker.datasource-proxy.slowQueryLogger'</code>.
     */
    String name = "cargotracker.data-source-proxy.slowQueryLogger"

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
