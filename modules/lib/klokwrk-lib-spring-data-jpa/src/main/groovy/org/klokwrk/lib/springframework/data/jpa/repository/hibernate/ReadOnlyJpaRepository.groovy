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
package org.klokwrk.lib.springframework.data.jpa.repository.hibernate

import groovy.transform.CompileStatic

@CompileStatic
interface ReadOnlyJpaRepository<T> {
  <S extends T> S save(S entity)
  <S extends T> List<S> saveAll(Iterable<S> entities)
  <S extends T> S saveAndFlush(S entity)
  <S extends T> List<S> saveAllAndFlush(Iterable<S> entities)

  <S extends T> S persist(S entity)
  <S extends T> S persistAndFlush(S entity)
  <S extends T> List<S> persistAll(Iterable<S> entities)
  <S extends T> List<S> persistAllAndFlush(Iterable<S> entities)

  <S extends T> S merge(S entity)
  <S extends T> S mergeAndFlush(S entity)
  <S extends T> List<S> mergeAll(Iterable<S> entities)
  <S extends T> List<S> mergeAllAndFlush(Iterable<S> entities)

  <S extends T> S update(S entity)
  <S extends T> S updateAndFlush(S entity)
  <S extends T> List<S> updateAll(Iterable<S> entities)
  <S extends T> List<S> updateAllAndFlush(Iterable<S> entities)
}
