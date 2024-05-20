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
package org.klokwrk.lib.hi.validation.springboot

import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Spring Boot configuration properties for {@link ValidationService}.
 * <p/>
 * To be able to use this from Spring Boot application, minimal configuration is required that enables this configuration properties and creates {@link ValidationService} bean like in the following
 * example:
 * <pre>
 * &#64;EnableConfigurationProperties(ValidationConfigurationProperties)
 * &#64;Configuration
 * class SpringBootConfig {
 *   &#64;SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
 *   &#64;Bean
 *   ValidationService validationService(ValidationConfigurationProperties validationConfigurationProperties) {
 *     return new ValidationService(validationConfigurationProperties)
 *   }
 * }
 * </pre>
 */
@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = "klokwrk.validation")
@CompileStatic
class ValidationConfigurationProperties {
  /**
   * By default ValidationService is enabled. Set to <code>false</code> to disable it.
   */
  boolean enabled = true

  /**
   * The list of message source base names to be used with {@code ValidationService}. By default it includes only "klokwrkValidationConstraintMessages".
   */
  String[] messageSourceBaseNames = ["klokwrkValidationConstraintMessages"]

  /**
   * The list of packages containing custom validator implementations to be configured for hibernate validator. By default it includes {@code org.klokwrk.lib.lo.validation.validator} package and all
   * its subpackages.
   */
  String[] validatorImplementationPackages = ["org.klokwrk.lib.lo.validation.validator.."]
}
