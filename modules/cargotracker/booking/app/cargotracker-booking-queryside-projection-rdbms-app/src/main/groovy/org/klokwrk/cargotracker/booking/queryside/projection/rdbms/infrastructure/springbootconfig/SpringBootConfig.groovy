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
package org.klokwrk.cargotracker.booking.queryside.projection.rdbms.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import org.axonframework.config.Configuration as AxonConfiguration
import org.axonframework.config.Configurer
import org.axonframework.config.ConfigurerModule
import org.axonframework.config.EventProcessingConfigurer
import org.axonframework.eventhandling.PropagatingErrorHandler
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.klokwrk.cargotracker.lib.axon.logging.LoggingEventHandlerEnhancerDefinition
import org.klokwrk.lib.datasourceproxy.springboot.DataSourceProxyBeanPostProcessor
import org.klokwrk.lib.datasourceproxy.springboot.DataSourceProxyConfigurationProperties
import org.klokwrk.lib.jackson.springboot.EssentialJacksonCustomizer
import org.klokwrk.lib.jackson.springboot.EssentialJacksonCustomizerConfigurationProperties
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties([DataSourceProxyConfigurationProperties, EssentialJacksonCustomizerConfigurationProperties])
@Configuration(proxyBeanMethods = false)
@CompileStatic
class SpringBootConfig {

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  BeanPostProcessor dataSourceProxyBeanPostProcessor(DataSourceProxyConfigurationProperties dataSourceProxyConfigurationProperties) {
    return new DataSourceProxyBeanPostProcessor(dataSourceProxyConfigurationProperties)
  }

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  EssentialJacksonCustomizer essentialJacksonCustomizer(EssentialJacksonCustomizerConfigurationProperties essentialJacksonCustomizerConfigurationProperties) {
    return new EssentialJacksonCustomizer(essentialJacksonCustomizerConfigurationProperties)
  }

  @Bean
  HandlerEnhancerDefinition loggingEventHandlerEnhancerDefinition() {
    return new LoggingEventHandlerEnhancerDefinition()
  }

  @Bean
  ConfigurerModule axonEventProcessingConfigurerModule() {
    return { Configurer configurer ->
      configurer
          .eventProcessing({ EventProcessingConfigurer eventProcessingConfigurer ->
            eventProcessingConfigurer
                .registerDefaultListenerInvocationErrorHandler({ AxonConfiguration configuration ->
                  return PropagatingErrorHandler.instance()
                })
                .usingPooledStreamingEventProcessors({ AxonConfiguration configuration, PooledStreamingEventProcessor.Builder builder ->
                  builder
                      .initialSegmentCount(2)
                      .batchSize(5)
                })
          })
    }
  }
}
