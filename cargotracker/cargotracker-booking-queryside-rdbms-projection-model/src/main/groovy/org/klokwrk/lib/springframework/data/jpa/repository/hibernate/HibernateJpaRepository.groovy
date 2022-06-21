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

/**
 * This interface is intended to add Hibernate-specific methods into Spring Data JPA repositories to resolve some Spring Data JPA implementation inefficiencies.
 * <p/>
 * The main problem with Spring Data JPA stems from the generic implementation of {@code saveXXX()} methods because they are falling back to the EntityManager's {@code mergeXXX()} methods for
 * entities with assigned identifiers.
 * <p/>
 * Note: since this interface and the corresponding implementation are expected to be published as part of the {@code hibernate-types library}, I'm not performing complete testing here. So instead, I
 * will just replace it with the published version in the future. This is also the reason why the implementation class is marked with {@code Generated} annotation.
 * <p/>
 * Reference: https://vladmihalcea.com/best-spring-data-jparepository/
 */
// TODO dmurat: Replace with hibernate-types library once Spring Data JPA Hibernate optimized repository implementation is published (https://github.com/vladmihalcea/hibernate-types)
@CompileStatic
interface HibernateJpaRepository<T> {
  // Save methods will trigger an UnsupportedOperationException
  @Deprecated <S extends T> S save(S entity)
  @Deprecated <S extends T> List<S> saveAll(Iterable<S> entities)
  @Deprecated <S extends T> S saveAndFlush(S entity)
  @Deprecated <S extends T> List<S> saveAllAndFlush(Iterable<S> entities)

  // Persist methods are meant to save newly created entities
  <S extends T> S persist(S entity)
  <S extends T> S persistAndFlush(S entity)
  <S extends T> List<S> persistAll(Iterable<S> entities)
  <S extends T> List<S> persistAllAndFlush(Iterable<S> entities)

  // Merge methods are meant to propagate detached entity state changes if they are really needed
  <S extends T> S merge(S entity)
  <S extends T> S mergeAndFlush(S entity)
  <S extends T> List<S> mergeAll(Iterable<S> entities)
  <S extends T> List<S> mergeAllAndFlush(Iterable<S> entities)

  //Update methods are meant to force the detached entity state changes
  <S extends T> S update(S entity)
  <S extends T> S updateAndFlush(S entity)
  <S extends T> List<S> updateAll(Iterable<S> entities)
  <S extends T> List<S> updateAllAndFlush(Iterable<S> entities)
}
