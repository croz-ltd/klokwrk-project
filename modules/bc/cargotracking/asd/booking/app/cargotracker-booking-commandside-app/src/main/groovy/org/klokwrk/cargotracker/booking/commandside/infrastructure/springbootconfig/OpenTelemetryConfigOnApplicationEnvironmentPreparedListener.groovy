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
package org.klokwrk.cargotracker.booking.commandside.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.context.ApplicationListener

/**
 * OpenTelemetry configurer triggered before application context is created (on <code>ApplicationEnvironmentPreparedEvent</code>).
 * <p>
 * Current implementation uses <code>otel.java.disabled.resource.providers</code> system property to disable <code>io.opentelemetry.instrumentation.resources.ProcessResourceProvider</code>. It only
 * does that when properties <code>management.tracing.enabled</code> and <code>grafana.otlp.enabled</code> are either not set (implicitly assumed) or explicitly set to <code>true</code>.
 * <p>
 * Problem with <code>ProcessResourceProvider</code> is that it generates to much information sent with each metric, log and trace. In some cases that can cause failure while sending observability
 * data to the OTEL backend (Grafana for our case). More specifically, <code>ProcessResourceProvider</code> collects and sends the application classpath (beside other things). As the classpath tends
 * to be quite large, we can end up with failures.<br>
 * <br>
 * Some related resources:
 * <li><a href="https://grafana.com/docs/opentelemetry/instrumentation/configuration/resource-attributes/#java-resource-providers">
 *   https://grafana.com/docs/opentelemetry/instrumentation/configuration/resource-attributes/#java-resource-providers
 * </a></li>
 * <li><a href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure#opentelemetry-resource">
 *   https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure#opentelemetry-resource
 * </a></li>
 * <li><a href="https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/resources/library#process">
 *   https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/resources/library#process
 * </a></li>
 * <li><a href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/process.md#process">
 *   https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/process.md#process
 * </a></li>
 * <p>
 * Implementation note: As this class manipulates system properties, it uses <code>ApplicationEnvironmentPreparedEvent</code> that is published before application context is created. For this reason
 * class is not marked as Spring component, but rather is configured as a listener in the application itself (see the source of <code>BookingCommandSideApplication</code>). Alternatively, we could do
 * the same thing via <code>META-INF/spring.factories</code> file, but we chose explicit listener configuration at the application since we believe it is more obvious.
 */
@CompileStatic
class OpenTelemetryConfigOnApplicationEnvironmentPreparedListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
  @SuppressWarnings("CodeNarc.DuplicateStringLiteral")
  @Override
  void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    Boolean tracingEnabledProperty = event.environment.getProperty("management.tracing.enabled", Boolean, true)
    Boolean grafanaOtlpEnabledProperty = event.environment.getProperty("grafana.otlp.enabled", Boolean, true)

    if (tracingEnabledProperty && grafanaOtlpEnabledProperty) {
      String otelDisabledResourceProvidersProperty = event.environment.getProperty("otel.java.disabled.resource.providers", String)?.trim()
      if (!otelDisabledResourceProvidersProperty) {
        System.setProperty("otel.java.disabled.resource.providers", "io.opentelemetry.instrumentation.resources.ProcessResourceProvider")
      }
    }
  }
}
