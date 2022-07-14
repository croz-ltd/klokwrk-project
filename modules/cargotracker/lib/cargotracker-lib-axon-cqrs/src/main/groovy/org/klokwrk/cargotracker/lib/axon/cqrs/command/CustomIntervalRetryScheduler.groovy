/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.lib.axon.cqrs.command

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.gateway.AbstractRetryScheduler
import org.axonframework.common.AxonNonTransientException

import java.util.function.Predicate

import static org.axonframework.common.BuilderUtils.assertPositive

// TODO dmurat: remove and replace with Axon IntervalRetryScheduler when, and if, https://github.com/AxonFramework/AxonFramework/pull/1910 gets merged and released.
/**
 * Custom variant of Axon {@code IntervalRetryScheduler} that allows configurable logic for determining non transient exceptions.
 */
@CompileStatic
class CustomIntervalRetryScheduler extends AbstractRetryScheduler {
  static final Long DEFAULT_RETRY_INTERVAL = 100L

  private final Long retryInterval
  private final Predicate<Throwable> nonTransientFailurePredicate
  private final List<Class<? extends Throwable>> nonTransientFailures

  protected CustomIntervalRetryScheduler(Builder builder) {
    super(builder)

    retryInterval = builder.retryInterval
    nonTransientFailurePredicate = builder.nonTransientFailurePredicate
    nonTransientFailures = builder.nonTransientFailures
  }

  @Override
  protected long computeRetryInterval(CommandMessage commandMessage, RuntimeException lastFailure, List<Class<? extends Throwable>[]> failures) {
    return retryInterval
  }

  /**
   * Indicates whether the given {@code failure} is non-transient.
   * <p/>
   * This implementation will first try to use {@code nonTransientFailurePredicate} predicate. If not available, implementation will fall back to the {@code nonTransientFailures}.
   * Note that {@code nonTransientFailures} is ignored when {@code nonTransientFailurePredicate} is configured and available.
   *
   * @see AbstractRetryScheduler#isExplicitlyNonTransient(java.lang.Throwable)
   */
  protected boolean isExplicitlyNonTransient(Throwable failure) {
    Boolean isNonTransientFailure
    if (nonTransientFailurePredicate) {
      isNonTransientFailure = nonTransientFailurePredicate.test(failure)
    }
    else {
      isNonTransientFailure = nonTransientFailures.any({ Class<? extends Throwable> nonTransientFailure -> nonTransientFailure.isAssignableFrom(failure.getClass()) })
    }

    return isNonTransientFailure || (failure.cause != null && isExplicitlyNonTransient(failure.cause))
  }

  @SuppressWarnings("CodeNarc.FactoryMethodName")
  static Builder builder() {
    return new Builder()
  }

  /**
   * Builder class for instantiating {@link CustomIntervalRetryScheduler}.
   * <p/>
   * The default for {@code retryInterval} is set to 100ms.
   * <p/>
   * The default for {@code nonTransientFailures} is set to a list containing only {@code AxonNonTransientException} class.
   * <p/>
   * Other defaults are taken from {@link AbstractRetryScheduler}.
   */
  static class Builder extends AbstractRetryScheduler.Builder<Builder> {
    private Long retryInterval = DEFAULT_RETRY_INTERVAL

    private Predicate<Throwable> nonTransientFailurePredicate = null

    @SuppressWarnings("CodeNarc.UnnecessaryCast")
    private final List<Class<? extends Throwable>> nonTransientFailures = [AxonNonTransientException] as List<Class<? extends Throwable>>

    @SuppressWarnings("CodeNarc.ConfusingMethodName")
    Builder retryInterval(Long retryInterval) {
      assertPositive(retryInterval, "The retryInterval must be a positive number")
      this.retryInterval = retryInterval
      return this
    }

    /**
     * Allows configuring {@code Predicate<Throwable>} used to determining if provided exception is transient ({@code false}) or not ({@code true}).
     * <p/>
     * When configured, {@code nonTransientFailures} list is ignored.
     */
    @SuppressWarnings("CodeNarc.ConfusingMethodName")
    Builder nonTransientFailurePredicate(Predicate<Throwable> nonTransientFailurePredicate) {
      this.nonTransientFailurePredicate = nonTransientFailurePredicate
      return this
    }

    /**
     * Allows configuring a list of non-transient exceptions.
     * <p/>
     * It is ignored if {@code nonTransientFailurePredicate} is configured.
     */
    @SuppressWarnings("CodeNarc.ConfusingMethodName")
    Builder nonTransientFailures(List<Class<? extends Throwable>> nonTransientFailures) {
      this.nonTransientFailures.clear()
      this.nonTransientFailures.addAll(nonTransientFailures)
      return this
    }

    CustomIntervalRetryScheduler build() {
      return new CustomIntervalRetryScheduler(this)
    }
  }
}