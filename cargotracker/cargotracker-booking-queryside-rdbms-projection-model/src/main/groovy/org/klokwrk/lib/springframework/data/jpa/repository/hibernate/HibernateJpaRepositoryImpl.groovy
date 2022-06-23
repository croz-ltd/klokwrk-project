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
import groovy.transform.Generated
import org.hibernate.Session
import org.hibernate.engine.jdbc.spi.JdbcServices
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.internal.AbstractSharedSessionContract

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import java.util.function.Supplier

/**
 * Hibernate optimized implementation of basic CRUD methods for Spring Data JPA repositories.
 *
 * @see HibernateJpaRepository
 */
@Generated
// I'm not interested in code coverage here since the plan is to replace this implementation with the one from hibernate-types library once it is available
@CompileStatic
class HibernateJpaRepositoryImpl<T> implements HibernateJpaRepository<T> {

  @PersistenceContext
  private EntityManager entityManager

  @SuppressWarnings("CodeNarc.UnusedMethodParameter")
  <S extends T> S save(S entity) { return unsupported() }

  @SuppressWarnings("CodeNarc.UnusedMethodParameter")
  <S extends T> List<S> saveAll(Iterable<S> entities) { return unsupported() }

  @SuppressWarnings("CodeNarc.UnusedMethodParameter")
  <S extends T> S saveAndFlush(S entity) { return unsupported() }

  @SuppressWarnings("CodeNarc.UnusedMethodParameter")
  <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) { return unsupported() }

  <S extends T> S persist(S entity) {
    entityManager.persist(entity)
    return entity
  }

  <S extends T> S persistAndFlush(S entity) {
    persist(entity)
    entityManager.flush()
    return entity
  }

  <S extends T> List<S> persistAll(Iterable<S> entities) {
    List<S> result = []
    for (S entity : entities) {
      result.add(persist(entity))
    }
    return result
  }

  @SuppressWarnings('DuplicatedCode')
  <S extends T> List<S> persistAllAndFlush(Iterable<S> entities) {
    return executeBatch({
      List<S> result = persistAll(entities)
      entityManager.flush()
      return result
    })
  }

  <S extends T> S merge(S entity) {
    return entityManager.merge(entity)
  }

  <S extends T> S mergeAndFlush(S entity) {
    S result = merge(entity)
    entityManager.flush()
    return result
  }

  @SuppressWarnings('DuplicatedCode')
  <S extends T> List<S> mergeAll(Iterable<S> entities) {
    List<S> result = []
    for (S entity : entities) {
      result.add(merge(entity))
    }
    return result
  }

  @SuppressWarnings('DuplicatedCode')
  <S extends T> List<S> mergeAllAndFlush(Iterable<S> entities) {
    return executeBatch({
      List<S> result = mergeAll(entities)
      entityManager.flush()
      return result
    })
  }

  <S extends T> S update(S entity) {
    session().update(entity)
    return entity
  }

  <S extends T> S updateAndFlush(S entity) {
    update(entity)
    entityManager.flush()
    return entity
  }

  @SuppressWarnings('DuplicatedCode')
  <S extends T> List<S> updateAll(Iterable<S> entities) {
    List<S> result = []
    for (S entity : entities) {
      result.add(update(entity))
    }
    return result
  }

  @SuppressWarnings('DuplicatedCode')
  <S extends T> List<S> updateAllAndFlush(Iterable<S> entities) {
    return executeBatch({
      List<S> result = updateAll(entities)
      entityManager.flush()
      return result
    })
  }

  protected <R> R executeBatch(Supplier<R> callback) {
    Session session = session()
    Integer jdbcBatchSize = getBatchSize(session)
    Integer originalSessionBatchSize = session.jdbcBatchSize
    try {
      if (jdbcBatchSize == null) {
        session.jdbcBatchSize = 10
      }
      return callback.get()
    }
    finally {
      session.jdbcBatchSize = originalSessionBatchSize
    }
  }

  protected Session session() {
    return entityManager.unwrap(Session)
  }

  protected Integer getBatchSize(Session session) {
    SessionFactoryImplementor sessionFactory = session.sessionFactory.unwrap(SessionFactoryImplementor)
    final JdbcServices jdbcServices = sessionFactory.serviceRegistry.getService(JdbcServices)

    if (!jdbcServices.extractedMetaDataSupport.supportsBatchUpdates()) {
      return Integer.MIN_VALUE
    }

    return session.unwrap(AbstractSharedSessionContract).configuredJdbcBatchSize
  }

  protected <S extends T> S unsupported() {
    throw new UnsupportedOperationException("There's no such thing as a save method in JPA, so don't use this hack!")
  }
}
