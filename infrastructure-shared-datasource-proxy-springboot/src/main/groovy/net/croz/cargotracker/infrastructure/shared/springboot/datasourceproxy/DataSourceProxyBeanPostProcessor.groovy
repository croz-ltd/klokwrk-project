package net.croz.cargotracker.infrastructure.shared.springboot.datasourceproxy

import net.ttddyy.dsproxy.support.ProxyDataSource
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.config.BeanPostProcessor

import javax.sql.DataSource

/**
 * Spring bean post-processor that sets up dataSourceProxy for intercepting all {@link DataSource} beans from the application context.
 */
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
      aopProxyFactory.addAdvice(new DataSourceProxyInterceptor(bean, dataSourceProxyConfigurationProperties))

      return aopProxyFactory.proxy
    }

    return bean
  }
}
