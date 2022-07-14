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

import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.RetryScheduler
import org.axonframework.common.AxonConfigurationException
import org.axonframework.common.AxonNonTransientException
import spock.lang.Specification

import java.lang.reflect.Field
import java.time.Instant
import java.util.concurrent.FutureTask
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class CustomIntervalRetrySchedulerSpecification extends Specification {
  @SuppressWarnings("CodeNarc.FactoryMethodName")
  void "builder - should throw when scheduler is not configured"() {
    when:
    CustomIntervalRetryScheduler.builder().build()

    then:
    thrown(AxonConfigurationException)
  }

  @SuppressWarnings(["GroovyAccessibility", "CodeNarc.FactoryMethodName"])
  void "builder - should create CustomIntervalRetryScheduler instance with expected defaults"() {
    given:
    ScheduledExecutorService scheduledExecutorServiceStub = Stub()

    when:
    CustomIntervalRetryScheduler customIntervalRetryScheduler = CustomIntervalRetryScheduler.builder().retryExecutor(scheduledExecutorServiceStub).build()

    Field[] abstractRetrySchedulerDeclaredFields = customIntervalRetryScheduler.getClass().superclass.declaredFields
    Field retryExecutorField = abstractRetrySchedulerDeclaredFields.find({ it.name == "retryExecutor" })
    retryExecutorField.accessible = true

    Field maxRetryCountField = abstractRetrySchedulerDeclaredFields.find({ it.name == "maxRetryCount" })
    maxRetryCountField.accessible = true

    then:
    retryExecutorField.get(customIntervalRetryScheduler) === scheduledExecutorServiceStub
    maxRetryCountField.get(customIntervalRetryScheduler) == 1

    customIntervalRetryScheduler.@retryInterval == 100L
    customIntervalRetryScheduler.@nonTransientFailurePredicate == null

    customIntervalRetryScheduler.@nonTransientFailures.size() == 1
    customIntervalRetryScheduler.@nonTransientFailures.contains(AxonNonTransientException)
  }

  void "isExplicitlyNonTransient - defaults should work as expected"() {
    given:
    ScheduledExecutorService scheduledExecutorServiceStub = Stub()
    CustomIntervalRetryScheduler customIntervalRetryScheduler = CustomIntervalRetryScheduler.builder().retryExecutor(scheduledExecutorServiceStub).build()

    when:
    boolean isNonTransientResult = customIntervalRetryScheduler.isExplicitlyNonTransient(failureToTestParam)

    then:
    isNonTransientResult == isNonTransientParam

    where:
    isNonTransientParam | failureToTestParam
    true                | new AxonNonTransientException("message") {}
    false               | new Exception()
    false               | new RuntimeException()
    false               | new IllegalArgumentException() // or any other exception
  }

  void "isExplicitlyNonTransient - should work as expected with configured nonTransientFailures list"() {
    given:
    ScheduledExecutorService scheduledExecutorServiceStub = Stub()
    CustomIntervalRetryScheduler customIntervalRetryScheduler = CustomIntervalRetryScheduler
        .builder()
        .retryExecutor(scheduledExecutorServiceStub)
        .nonTransientFailures([AxonNonTransientException, CommandExecutionException, IllegalStateException])
        .build()

    when:
    boolean isNonTransientResult = customIntervalRetryScheduler.isExplicitlyNonTransient(failureToTestParam)

    then:
    isNonTransientResult == isNonTransientParam

    where:
    isNonTransientParam | failureToTestParam
    true                | new AxonNonTransientException("message") {}
    true                | new CommandExecutionException("message", null, null)
    true                | new IllegalStateException()
    false               | new IllegalArgumentException() // or any other exception
  }

  void "isExplicitlyNonTransient - should work as expected with configured nonTransientFailurePredicate"() {
    given:
    ScheduledExecutorService scheduledExecutorServiceStub = Stub()
    CustomIntervalRetryScheduler customIntervalRetryScheduler = CustomIntervalRetryScheduler
        .builder()
        .retryExecutor(scheduledExecutorServiceStub)
        .nonTransientFailurePredicate({ Throwable failureToTest -> return true }) // For this predicate all exceptions are considered as non-transient
        .build()

    when:
    boolean isNonTransientResult = customIntervalRetryScheduler.isExplicitlyNonTransient(failureToTestParam)

    then:
    isNonTransientResult == isNonTransientParam

    where:
    isNonTransientParam | failureToTestParam
    true                | new Exception()
    true                | new RuntimeException()
    true                | new AxonNonTransientException("message") {}
    true                | new CommandExecutionException("message", null, null)
    true                | new IllegalStateException()
    true                | new IllegalArgumentException()
  }

  void "isExplicitlyNonTransient - should ignore nonTransientFailures list when nonTransientFailurePredicate is configured"() {
    given:
    ScheduledExecutorService scheduledExecutorServiceStub = Stub()
    CustomIntervalRetryScheduler customIntervalRetryScheduler = CustomIntervalRetryScheduler
        .builder()
        .retryExecutor(scheduledExecutorServiceStub)
        .nonTransientFailurePredicate({ Throwable failureToTest -> return false }) // For this predicate all exceptions are considered as transient
        .nonTransientFailures([AxonNonTransientException, CommandExecutionException, IllegalStateException])
        .build()

    when:
    boolean isNonTransientResult = customIntervalRetryScheduler.isExplicitlyNonTransient(failureToTestParam)

    then:
    isNonTransientResult == isNonTransientParam

    where:
    isNonTransientParam | failureToTestParam
    false               | new Exception()
    false               | new RuntimeException()
    false               | new AxonNonTransientException("message") {}
    false               | new CommandExecutionException("message", null, null)
    false               | new IllegalStateException()
    false               | new IllegalArgumentException()
  }

  void "isExplicitlyNonTransient - should work on failure causes"() {
    given:
    ScheduledExecutorService scheduledExecutorServiceStub = Stub()
    CustomIntervalRetryScheduler customIntervalRetryScheduler = CustomIntervalRetryScheduler
        .builder()
        .retryExecutor(scheduledExecutorServiceStub)
        .nonTransientFailures([AxonNonTransientException, CommandExecutionException, IllegalStateException])
        .build()

    when:
    boolean isNonTransientResult = customIntervalRetryScheduler.isExplicitlyNonTransient(failureToTestParam)

    then:
    isNonTransientResult == isNonTransientParam

    where:
    isNonTransientParam | failureToTestParam
    true                | new IllegalArgumentException("message", new AxonNonTransientException("message") {})
    true                | new IllegalArgumentException("message", new CommandExecutionException("message", null, null))
    true                | new IllegalArgumentException("message", new IllegalStateException())
    false               | new IllegalArgumentException("message", new IllegalArgumentException())
  }

  private Long doScheduleRetryForTest(RetryScheduler retryScheduler, List<Class<? extends Throwable>[]> completeFailureHistoryIncludingCauses) {
    CommandMessage<?> commandMessage = GenericCommandMessage.asCommandMessage("Hello world")
    Instant before = Instant.now()
    FutureTask<Instant> after = new FutureTask<>({ Instant.now() })

    if (retryScheduler.scheduleRetry(commandMessage, new IllegalArgumentException(), completeFailureHistoryIncludingCauses, after)) {
      Instant afterInstant = after.get()
      return afterInstant.toEpochMilli() - before.toEpochMilli()
    }

    return 0
  }

  void "should schedule a retry with expected delay"() {
    given:
    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1)
    CustomIntervalRetryScheduler customIntervalRetryScheduler = CustomIntervalRetryScheduler
        .builder()
        .retryExecutor(scheduledExecutorService)
        .nonTransientFailures([AxonNonTransientException, CommandExecutionException, IllegalStateException])
        .build()

    List<Class<? extends Throwable>[]> completeFailureHistoryIncludingCauses = []
    1.times {
      //noinspection GroovyAssignabilityCheck
      completeFailureHistoryIncludingCauses.add([IllegalArgumentException, NullPointerException])
    }

    when:
    long millisPassedUntilScheduling = doScheduleRetryForTest(customIntervalRetryScheduler, completeFailureHistoryIncludingCauses)

    then:
    millisPassedUntilScheduling >= CustomIntervalRetryScheduler.DEFAULT_RETRY_INTERVAL
  }

  void "should schedule a retry if maxRetryCount is reached"() {
    given:
    Integer maxRetryCount = 5
    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1)
    CustomIntervalRetryScheduler customIntervalRetryScheduler = CustomIntervalRetryScheduler
        .builder()
        .retryExecutor(scheduledExecutorService)
        .nonTransientFailures([AxonNonTransientException, CommandExecutionException, IllegalStateException])
        .maxRetryCount(maxRetryCount)
        .build()

    List<Class<? extends Throwable>[]> completeFailureHistoryIncludingCauses = []
    maxRetryCount.times {
      //noinspection GroovyAssignabilityCheck
      completeFailureHistoryIncludingCauses.add([IllegalArgumentException, NullPointerException])
    }

    when:
    long millisPassedUntilScheduling = doScheduleRetryForTest(customIntervalRetryScheduler, completeFailureHistoryIncludingCauses)

    then:
    millisPassedUntilScheduling >= CustomIntervalRetryScheduler.DEFAULT_RETRY_INTERVAL
  }

  void "should not schedule a retry if maxRetryCount is exceeded"() {
    given:
    Integer maxRetryCount = 5
    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1)
    CustomIntervalRetryScheduler customIntervalRetryScheduler = CustomIntervalRetryScheduler
        .builder()
        .retryExecutor(scheduledExecutorService)
        .nonTransientFailures([AxonNonTransientException, CommandExecutionException, IllegalStateException])
        .maxRetryCount(maxRetryCount)
        .build()

    List<Class<? extends Throwable>[]> completeFailureHistoryIncludingCauses = []
    (maxRetryCount + 1).times {
      //noinspection GroovyAssignabilityCheck
      completeFailureHistoryIncludingCauses.add([IllegalArgumentException, NullPointerException])
    }

    when:
    long millisPassedUntilScheduling = doScheduleRetryForTest(customIntervalRetryScheduler, completeFailureHistoryIncludingCauses)

    then:
    millisPassedUntilScheduling == 0
  }
}
