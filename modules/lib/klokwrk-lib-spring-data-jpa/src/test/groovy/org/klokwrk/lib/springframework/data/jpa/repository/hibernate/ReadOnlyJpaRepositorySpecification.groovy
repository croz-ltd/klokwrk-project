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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

import javax.persistence.EntityManager

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class ReadOnlyJpaRepositorySpecification extends Specification {
  @Autowired
  EntityManager entityManager

  @Autowired
  TestSpringDataJpaReadOnlyRepository testSpringDataRepository

  void "should inject autowired resources"() {
    expect:
    entityManager
    testSpringDataRepository
  }

  void "should fail on save"() {
    when:
    testSpringDataRepository.save(new TestEntity(id: 1L))

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on saveAll"() {
    when:
    testSpringDataRepository.saveAll([new TestEntity(id: 1L)])

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on saveAndFlush"() {
    when:
    testSpringDataRepository.saveAndFlush(new TestEntity(id: 1L))

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on saveAllAndFlush"() {
    when:
    testSpringDataRepository.saveAllAndFlush([new TestEntity(id: 1L)])

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on persist"() {
    when:
    testSpringDataRepository.persist(new TestEntity(id: 1L))

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on persistAndFlush"() {
    when:
    testSpringDataRepository.persistAndFlush(new TestEntity(id: 1L))

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on persistAll"() {
    when:
    testSpringDataRepository.persistAll([new TestEntity(id: 1L)])

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on persistAllAndFlush"() {
    when:
    testSpringDataRepository.persistAllAndFlush([new TestEntity(id: 1L)])

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on merge"() {
    when:
    testSpringDataRepository.merge(new TestEntity(id: 1L))

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on mergeAndFlush"() {
    when:
    testSpringDataRepository.mergeAndFlush(new TestEntity(id: 1L))

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on mergeAll"() {
    when:
    testSpringDataRepository.mergeAll([new TestEntity(id: 1L)])

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on mergeAllAndFlush"() {
    when:
    testSpringDataRepository.mergeAllAndFlush([new TestEntity(id: 1L)])

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on update"() {
    when:
    testSpringDataRepository.update(new TestEntity(id: 1L))

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on updateAndFlush"() {
    when:
    testSpringDataRepository.updateAndFlush(new TestEntity(id: 1L))

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on updateAll"() {
    when:
    testSpringDataRepository.updateAll([new TestEntity(id: 1L)])

    then:
    thrown(UnsupportedOperationException)
  }

  void "should fail on updateAllAndFlush"() {
    when:
    testSpringDataRepository.updateAllAndFlush([new TestEntity(id: 1L)])

    then:
    thrown(UnsupportedOperationException)
  }
}
