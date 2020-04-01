package net.croz.cargotracker.infrastructure.shared.springboot.datasourceproxy

import net.croz.cargotracker.infrastructure.shared.datasourceproxy.Slf4jFilterableQueryLoggingListener
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.util.ReflectionUtils

import javax.sql.DataSource
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

/**
 * AOP alliance proxy interceptor that decorates all invocations of {@link DataSource} instance methods with dataSourceProxy features.
 */
class DataSourceProxyInterceptor implements MethodInterceptor {
  DataSource dataSource

  DataSourceProxyInterceptor(DataSource dataSource, DataSourceProxyConfigurationProperties dataSourceProxyConfigurationProperties) {
    Slf4jFilterableQueryLoggingListener slf4jFilterableQueryLoggingListener =
        new Slf4jFilterableQueryLoggingListener(dataSourceProxyConfigurationProperties.queryLogger.filteringOutRegularExpressionList)

    slf4jFilterableQueryLoggingListener.setLogLevel(dataSourceProxyConfigurationProperties.queryLogger.logLevel)
    slf4jFilterableQueryLoggingListener.setLogger(dataSourceProxyConfigurationProperties.queryLogger.name)

    this.dataSource = ProxyDataSourceBuilder
        .create(dataSource)
        .name(dataSourceProxyConfigurationProperties.dataSourceName)
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
