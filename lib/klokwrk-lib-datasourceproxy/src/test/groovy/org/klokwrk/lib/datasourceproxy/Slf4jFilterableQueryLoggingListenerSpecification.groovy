/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.datasourceproxy

import com.google.common.collect.ImmutableList
import net.ttddyy.dsproxy.ExecutionInfo
import net.ttddyy.dsproxy.QueryInfo
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener
import spock.lang.Specification
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.LoggingEvent
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

class Slf4jFilterableQueryLoggingListenerSpecification extends Specification {
  TestLogger logger

  void setup() {
    TestLoggerFactory.clearAll()
//    TestLoggerFactory.getInstance().setPrintLevel(Level.TRACE) // uncomment if you want to see logging output during the test
    logger = TestLoggerFactory.getTestLogger(SLF4JQueryLoggingListener.name)
  }

  void "should have expected default logger name of a super class"() {
    given:
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener()

    expect:
    listener.logger.name == SLF4JQueryLoggingListener.name
  }

  void configureEnabledLevels(TestLogger testLogger, Level enabledLevel) {
    Level[] allLevels = [Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE]
    Level[] selectedLevels = allLevels[0 .. allLevels.findIndexOf({ Level level -> level == enabledLevel })]
    testLogger.enabledLevelsForAllThreads = selectedLevels
  }

  void "should fail for null regex pattern list"() {
    when:
    new Slf4jFilterableQueryLoggingListener(null)

    then:
    thrown(AssertionError)
  }

  void "should not log anything on INFO level"() {
    given:
    configureEnabledLevels(logger, Level.INFO)
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener([/^update token_entry.*$/])

    when:
    listener.afterQuery(new ExecutionInfo(), [new QueryInfo("select * from myTable")])
    ImmutableList<LoggingEvent> loggingEventList = logger.allLoggingEvents

    then:
    loggingEventList.size() == 0
  }

  void "should not log anything for empty query info list"() {
    given:
    configureEnabledLevels(logger, Level.INFO)
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener([/^update token_entry.*$/])

    when:
    listener.afterQuery(new ExecutionInfo(), [])
    ImmutableList<LoggingEvent> loggingEventList = logger.allLoggingEvents

    then:
    loggingEventList.size() == 0
  }

  void "should log normally for empty regex pattern list"() {
    given:
    configureEnabledLevels(logger, Level.DEBUG)
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener()

    when:
    listener.afterQuery(new ExecutionInfo(), [new QueryInfo("select * from myTable")])
    ImmutableList<LoggingEvent> loggingEventList = logger.allLoggingEvents

    then:
    loggingEventList.size() == 1
  }

  void "should filter out matching queries at debug DEBUG level for a single query in the execution"() {
    given:
    configureEnabledLevels(logger, Level.DEBUG)
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener([/^update token_entry.*$/])

    when:
    listener.afterQuery(new ExecutionInfo(), [new QueryInfo("update token_entry set timestamp=? where processor_name=? and segment=? and owner=?")])
    ImmutableList<LoggingEvent> loggingEventList = logger.allLoggingEvents

    then:
    loggingEventList.size() == 0
  }

  void "should filter out matching queries at debug DEBUG level for multiple queries in the execution"() {
    given:
    configureEnabledLevels(logger, Level.DEBUG)
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
    ImmutableList<LoggingEvent> loggingEventList = logger.allLoggingEvents

    then:
    loggingEventList.size() == 1
    loggingEventList[0].level == Level.DEBUG
    loggingEventList[0].message.contains("select * from myTable")
    loggingEventList[0].message.contains("select * from myOtherTable")
    //noinspection GroovyPointlessBoolean
    loggingEventList[0].message.contains("update token_entry") == false
  }

  void "should not filter out matching queries at TRACE level for a single query in the execution"() {
    given:
    configureEnabledLevels(logger, Level.TRACE)
    Slf4jFilterableQueryLoggingListener listener = new Slf4jFilterableQueryLoggingListener([/^update token_entry.*$/])

    when:
    listener.afterQuery(new ExecutionInfo(), [new QueryInfo("update token_entry set timestamp=? where processor_name=? and segment=? and owner=?")])
    ImmutableList<LoggingEvent> loggingEventList = logger.allLoggingEvents

    then:
    loggingEventList.size() == 1
    loggingEventList[0].level == Level.TRACE
    loggingEventList[0].message.contains("update token_entry")
  }

  void "should not filter out matching queries at TRACE level for multiple queries in the execution"() {
    given:
    configureEnabledLevels(logger, Level.TRACE)
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
    ImmutableList<LoggingEvent> loggingEventList = logger.allLoggingEvents

    then:
    loggingEventList.size() == 1
    loggingEventList[0].level == Level.TRACE
    loggingEventList[0].message.contains("select * from myTable")
    loggingEventList[0].message.contains("select * from myOtherTable")
    loggingEventList[0].message.contains("update token_entry")
  }
}
