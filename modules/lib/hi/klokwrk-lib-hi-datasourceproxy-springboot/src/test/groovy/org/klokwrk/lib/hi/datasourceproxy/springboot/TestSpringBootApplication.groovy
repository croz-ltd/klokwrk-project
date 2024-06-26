/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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

import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@EnableConfigurationProperties(DataSourceProxyConfigurationProperties)
@SpringBootApplication(proxyBeanMethods = false)
class TestSpringBootApplication {
  static void main(String[] args) {
    SpringApplication.run(TestSpringBootApplication, args)
  }

  @Bean
  static BeanPostProcessor dataSourceProxyBeanPostProcessor(ObjectProvider<DataSourceProxyConfigurationProperties> dataSourceProxyConfigurationPropertiesObjectProvider) {
    return new DataSourceProxyBeanPostProcessor(dataSourceProxyConfigurationPropertiesObjectProvider)
  }
}
