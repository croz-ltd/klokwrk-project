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
package org.klokwrk.cargotracking.lib.axon.cqrs.command

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.common.AxonNonTransientException
import org.klokwrk.cargotracking.lib.boundary.api.application.exception.RemoteHandlerException
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.DomainException

import java.util.function.Predicate

/**
 * Predicate for checking if provided failure is considered non-transient (returns {@code true}) or not (returns {@code false}).
 * <p/>
 * Current implementation reports following failures as non-transient:
 * <ul>
 *   <li>instance of {@link AxonNonTransientException}</li>
 *   <li>instance of {@link CommandExecutionException} when its {@code details} property contains an instance of {@link DomainException}</li>
 *   <li>
 *     instance of {@link CommandExecutionException} when its {@code details} property contains an instance of {@link RemoteHandlerException} whose message indicates that original cause was
 *     {@link NullPointerException}
 *   </li>
 *
 * </ul>
 * This predicate is intended to be used as {@code nonTransientFailurePredicate} property of Axon's {@code AbstractRetryScheduler}.
 */
@CompileStatic
class NonTransientFailurePredicate implements Predicate<Throwable> {
  @SuppressWarnings("CodeNarc.UnnecessaryCast")
  static final List<Class<? extends Throwable>> NON_TRANSIENT_FAILURE_LIST = [AxonNonTransientException] as List<Class<? extends Throwable>>

  @SuppressWarnings("CodeNarc.UnnecessaryCast")
  static final List<Class<? extends Throwable>> REMOTE_HANDLER_NON_TRANSIENT_CAUSE_LIST = [NullPointerException] as List<Class<? extends Throwable>>

  @Override
  boolean test(Throwable failureToTest) {
    // First, check a list of coarse grained exceptions.
    boolean isNonTransient = NON_TRANSIENT_FAILURE_LIST.any({ Class<? extends Throwable> nonTransientFailure -> nonTransientFailure.isAssignableFrom(failureToTest.getClass()) })

    // Second, check details of CommandExecutionException
    if (!isNonTransient && CommandExecutionException.isAssignableFrom(failureToTest.getClass())) {
      CommandExecutionException commandExecutionExceptionToTest = failureToTest as CommandExecutionException
      Object exceptionDetails = commandExecutionExceptionToTest.details.orElse(null)

      // Domain exceptions are always non-transient
      if (exceptionDetails && DomainException.isAssignableFrom(exceptionDetails.getClass())) {
        isNonTransient = true
      }

      // RemoteHandlerException is caused by any exception that is not Domain exception. Therefore it might be non-transient in the case of programming errors (i.e. when cause is something like
      // NullPointerException), but might also be a transient.
      // We can detect the cause based on RemoteHandlerException message as it contains the class name of original cause.
      if (!isNonTransient && exceptionDetails && RemoteHandlerException.isAssignableFrom(exceptionDetails.getClass())) {
        RemoteHandlerException remoteHandlerExceptionToTest = exceptionDetails as RemoteHandlerException

        String remoteHandlerExceptionMessageToTest = remoteHandlerExceptionToTest.exceptionMessage
        isNonTransient = REMOTE_HANDLER_NON_TRANSIENT_CAUSE_LIST.any({ Class<?> causeExceptionClass ->
          // Expects a message format as it was created by CommandHandlerExceptionInterceptor
          remoteHandlerExceptionMessageToTest.contains("command failed because of ${ causeExceptionClass.name }")
        })
      }
    }

    return isNonTransient
  }
}
