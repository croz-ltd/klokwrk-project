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
package org.klokwrk.lib.lo.datasourceproxy

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import net.ttddyy.dsproxy.ExecutionInfo
import net.ttddyy.dsproxy.QueryInfo
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener
import org.slf4j.LoggerFactory
import spock.lang.Specification

class Slf4jFilterableQueryLoggingListenerSpecification extends Specification {
  void "should have expected default logger name of a super class"() {
    given:
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener()

    expect:
    listener.logger.name == SLF4JQueryLoggingListener.name
  }

  void "should fail for null regex pattern list"() {
    when:
    new Slf4jFilterableQueryLoggingListener(null)

    then:
    thrown(AssertionError)
  }

  private List configureLoggerAndListAppender(Level loggerLevel) {
    Logger logger = LoggerFactory.getLogger(SLF4JQueryLoggingListener.name) as Logger
    logger.level = loggerLevel
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)

    return [logger, listAppender]
  }

  private void cleanupLogger(Logger logger, ListAppender listAppender) {
    logger.detachAppender(listAppender)
  }

  void "should not log anything on INFO level"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender(Level.INFO)
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener([/^update token_entry.*$/])

    when:
    listener.afterQuery(new ExecutionInfo(), [new QueryInfo("select * from myTable")])

    then:
    listAppender.list.size() == 0

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "should not log anything for empty query info list"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender(Level.INFO)
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener([/^update token_entry.*$/])

    when:
    listener.afterQuery(new ExecutionInfo(), [])

    then:
    listAppender.list.size() == 0

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "should log normally for empty regex pattern list"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender(Level.DEBUG)
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener()

    when:
    listener.afterQuery(new ExecutionInfo(), [new QueryInfo("select * from myTable")])

    then:
    listAppender.list.size() == 1

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "should filter out matching queries at DEBUG level for a single query in the execution"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender(Level.DEBUG)
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener([/^update token_entry.*$/])

    when:
    listener.afterQuery(new ExecutionInfo(), [new QueryInfo("update token_entry set timestamp=? where processor_name=? and segment=? and owner=?")])

    then:
    listAppender.list.size() == 0

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "should filter out matching queries at DEBUG level for multiple queries in the execution"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender(Level.DEBUG)
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener([/^update token_entry.*$/])

    when:
    listener.afterQuery(
        new ExecutionInfo(),
        [
            new QueryInfo("select * from myTable"),
            new QueryInfo("update token_entry set timestamp=? where processor_name=? and segment=? and owner=?"),
            new QueryInfo("select * from myOtherTable")
        ]
    )

    then:
    listAppender.list.size() == 1
    verifyAll(listAppender.list[0]) {
      level == Level.DEBUG
      message.contains("select * from myTable")
      message.contains("select * from myOtherTable")
      //noinspection GroovyPointlessBoolean
      message.contains("update token_entry") == false
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "should not filter out matching queries at TRACE level for a single query in the execution"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender(Level.TRACE)
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener([/^update token_entry.*$/])

    when:
    listener.afterQuery(new ExecutionInfo(), [new QueryInfo("update token_entry set timestamp=? where processor_name=? and segment=? and owner=?")])

    then:
    listAppender.list.size() == 1
    verifyAll(listAppender.list[0]) {
      level == Level.TRACE
      message.contains("update token_entry")
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "should not filter out matching queries at TRACE level for multiple queries in the execution"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender(Level.TRACE)
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener([/^update token_entry.*$/])

    when:
    listener.afterQuery(
        new ExecutionInfo(),
        [
            new QueryInfo("select * from myTable"),
            new QueryInfo("update token_entry set timestamp=? where processor_name=? and segment=? and owner=?"),
            new QueryInfo("select * from myOtherTable")
        ]
    )

    then:
    listAppender.list.size() == 1
    verifyAll(listAppender.list[0]) {
      level == Level.TRACE
      message.contains("select * from myTable")
      message.contains("select * from myOtherTable")
      message.contains("update token_entry")
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }
}
