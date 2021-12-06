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
package org.klokwrk.cargotracker.lib.boundary.api.application.operation

/**
 * Defines the basic format of messages exchanged over domain facade boundary.
 * <p/>
 * Any inbound channel (i.e., web, CLI, messaging, etc.) must convert its accepted messages to this format to be able to send a message into a domain facade.
 * <p/>
 * Metadata is a map containing any non-functional information that must be conveyed from inbound channels into the domain. These might be security-related information, quality-of-service information,
 * metrics and tracing related information, etc. Names of commonly used metadata map keys are enumerated in <code>MetaDataConstant</code> class.
 * <p/>
 * The payload conveys information that might be considered as domain facade's API data structure. Usually, the payload corresponds (at least to some degree) to the data sent by the end-user.
 */
interface OperationMessage<P, M extends Map<String, ?>> {
  M getMetaData()
  P getPayload()
}
