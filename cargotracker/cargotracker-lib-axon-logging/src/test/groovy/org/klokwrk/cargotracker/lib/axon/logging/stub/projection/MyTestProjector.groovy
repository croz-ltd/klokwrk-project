/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.lib.axon.logging.stub.projection

import org.axonframework.eventhandling.EventHandler
import org.klokwrk.cargotracker.lib.axon.logging.stub.event.MyTestAggregateCreatedEvent
import org.klokwrk.cargotracker.lib.axon.logging.stub.event.MyTestAggregateUpdatedEvent

class MyTestProjector {
  @SuppressWarnings(["unused", "CodeNarc.UnusedMethodParameter"])
  @EventHandler
  void handle(MyTestAggregateCreatedEvent event) {
    // do nothing
  }

  @SuppressWarnings(["unused", "CodeNarc.UnusedMethodParameter"])
  @EventHandler
  void handle(MyTestAggregateUpdatedEvent event) {
    // do nothing
  }
}
