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
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.context.propagation.ContextPropagators
import net.ttddyy.observation.tracing.QueryContext
import org.axonframework.tracing.MultiSpanFactory
import org.axonframework.tracing.NestingSpanFactory
import org.axonframework.tracing.SpanFactory
import org.axonframework.tracing.attributes.MetadataSpanAttributesProvider
import org.axonframework.tracing.opentelemetry.OpenTelemetrySpanFactory
import org.springframework.boot.actuate.autoconfigure.observation.ObservationRegistryCustomizer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.util.regex.Pattern

@ConditionalOnProperty(prefix = "management.tracing", name = "enabled", matchIfMissing = true)
@Configuration
@CompileStatic
class OpenTelemetryConfig {
  static final List<Pattern> IGNORED_QUERIES = [~/^update token_entry.*$/, ~/select.*from token_entry.*$/]

  @Bean
  SpanFactory axonTracingSpanFactory(OpenTelemetry openTelemetry, ContextPropagators contextPropagators) {
    OpenTelemetrySpanFactory axonOpenTelemetrySpanFactory = OpenTelemetrySpanFactory.builder()
        .tracer(openTelemetry.getTracer("AxonFramework"))
        .contextPropagators(contextPropagators.textMapPropagator)
        .build()

    SpanFactory axonSpanFactory = new MultiSpanFactory([axonOpenTelemetrySpanFactory as SpanFactory])
    axonSpanFactory.registerSpanAttributeProvider(new MetadataSpanAttributesProvider())
    NestingSpanFactory axonNestingSpanFactory = NestingSpanFactory.builder().delegate(axonSpanFactory).build()

    return axonNestingSpanFactory
  }

  @SuppressWarnings("CodeNarc.Instanceof")
  @Bean
  ObservationRegistryCustomizer<ObservationRegistry> myObservationRegistryCustomizer() {
    return (ObservationRegistry observationRegistry) -> {
      observationRegistry.observationConfig()
          .observationPredicate((String observationName, Observation.Context observationContext) -> {
            if (observationContext instanceof QueryContext) {
              QueryContext queryContext = observationContext
              boolean shouldIgnore = queryContext.queries.every({ String query -> IGNORED_QUERIES.any({ Pattern pattern -> query.matches(pattern) }) })
              boolean shouldObserve = !shouldIgnore
              return shouldObserve
            }
            return true
          })
    }
  }
}
