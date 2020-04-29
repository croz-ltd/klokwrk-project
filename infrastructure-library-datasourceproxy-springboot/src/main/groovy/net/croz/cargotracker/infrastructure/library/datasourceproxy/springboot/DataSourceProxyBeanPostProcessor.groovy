package net.croz.cargotracker.infrastructure.library.datasourceproxy.springboot

import groovy.transform.CompileStatic
import net.ttddyy.dsproxy.support.ProxyDataSource
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.config.BeanPostProcessor

import javax.sql.DataSource

/**
 * Spring bean post-processor that sets up <code>datasource-proxy</code> intercepting for all <code>DataSource</code> beans from the application context.
 * <p/>
 * To use datasource-proxy features from a Spring Boot application, this bean post processor needs to be configured as a bean, and accompanying {@link DataSourceProxyConfigurationProperties} needs
 * to be enabled similar to the following example:
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
 * This will configure and enable datasource-proxy's "query logging" and "slow query logging" features. Some aspects of these datasource-proxy features can be configured via
 * {@link DataSourceProxyConfigurationProperties} configuration properties. Usually, it is enough to configure appropriate loggers like in following example:
 * <pre>
 * logging.level.cargotracker.data-source-proxy.queryLogger: DEBUG
 * logging.level.cargotracker.data-source-proxy.slowQueryLogger: WARN
 * </pre>
 * For more configuration options, take a look at the documentation of {@link DataSourceProxyConfigurationProperties}.
 * <p/>
 * It is worth mentioning that datasource-proxy's "query logging" feature is expanded by supporting filtering of queries that one wants to remove from the log. This is useful when we have reoccurring
 * queries, like in polling-a-database scenarios. Queries that need to be filtered out are specified as a list of regular expressions. When query string matches regular expression, it won't be
 * present in the log output. For example, following configuration will prevent logging of any updates to the <code>token_entry</code> table.
 * <pre>
 * cargotracker.data-source-proxy.query-logger.filtering-out-regular-expression-list: >
 *   ^update token_entry.*$
 * </pre>
 * This 'filterable query logging' feature is implemented in <code>net.croz.cargotracker.infrastructure.library.datasourceproxy.Slf4jFilterableQueryLoggingListener</code>.
 *
 * @see DataSourceProxyConfigurationProperties
 * @see DataSourceProxyInterceptor
 */
@CompileStatic
class DataSourceProxyBeanPostProcessor implements BeanPostProcessor {
  DataSourceProxyConfigurationProperties dataSourceProxyConfigurationProperties

  DataSourceProxyBeanPostProcessor(DataSourceProxyConfigurationProperties dataSourceProxyConfigurationProperties) {
    this.dataSourceProxyConfigurationProperties = dataSourceProxyConfigurationProperties
  }

  @Override
  Object postProcessAfterInitialization(Object bean, String beanName) {
    if (!dataSourceProxyConfigurationProperties.enabled) {
      return bean
    }

    if (bean instanceof DataSource && !(bean instanceof ProxyDataSource)) {
      ProxyFactory aopProxyFactory = new ProxyFactory(bean)
      aopProxyFactory.proxyTargetClass = true
      aopProxyFactory.addAdvice(new DataSourceProxyInterceptor(beanName, bean, dataSourceProxyConfigurationProperties))

      return aopProxyFactory.proxy
    }

    return bean
  }
}
