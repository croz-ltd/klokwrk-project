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
package org.klokwrk.lib.datasourceproxy.springboot

import com.google.common.collect.ImmutableList
import net.ttddyy.dsproxy.support.ProxyDataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.ApplicationContext
import org.springframework.jdbc.core.ColumnMapRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.LoggingEvent
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

import javax.sql.DataSource

@JdbcTest
class DataSourceProxyBeanPostProcessorDefaultSetupSpecification extends Specification {

  TestLogger queryLogger
  TestLogger slowQueryLogger

  @Autowired
  ApplicationContext applicationContext

  @Autowired
  JdbcTemplate jdbcTemplate

  void configureEnabledLevels(TestLogger testLogger, Level enabledLevel) {
    Level[] allLevels = [Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE]
    Level[] selectedLevels = allLevels[0..allLevels.findIndexOf({ Level level -> level == enabledLevel })]
    testLogger.enabledLevelsForAllThreads = selectedLevels
  }

  DataSource makePlainDataSource() {
    DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create()

    dataSourceBuilder
        .driverClassName("org.h2.Driver")
        .url("jdbc:h2:mem:test")
        .username("SA")
        .password("")

    return dataSourceBuilder.build()
  }

  void setup() {
    TestLoggerFactory.clearAll()
//    TestLoggerFactory.instance.printLevel = Level.DEBUG // uncomment if you want to see logging output during the test
    queryLogger = TestLoggerFactory.getTestLogger("klokwrk.datasourceproxy.queryLogger")
    slowQueryLogger = TestLoggerFactory.getTestLogger("klokwrk.datasourceproxy.slowQueryLogger")
  }

  void "should configure proxying of default data source"() {
    expect:
    //noinspection GroovyAssignabilityCheck,GrUnresolvedAccess
    applicationContext.getBean("dataSource").properties.advisors[0].advice.getClass() == DataSourceProxyInterceptor
  }

  void "should configure proxying of plain data source for direct call"() {
    given:
    DataSourceProxyBeanPostProcessor dataSourceProxyBeanPostProcessor = new DataSourceProxyBeanPostProcessor(new DataSourceProxyConfigurationProperties())

    when:
    DataSource plainDataSource = dataSourceProxyBeanPostProcessor.postProcessAfterInitialization(makePlainDataSource(), "plainDataSource") as DataSource

    then:
    //noinspection GroovyAssignabilityCheck,GrUnresolvedAccess
    plainDataSource.properties.advisors[0].advice.getClass() == DataSourceProxyInterceptor
  }

  void "should not configure anything for ProxyDataSource"() {
    given:
    DataSourceProxyBeanPostProcessor dataSourceProxyBeanPostProcessor = new DataSourceProxyBeanPostProcessor(new DataSourceProxyConfigurationProperties())
    ProxyDataSource proxyDataSource = new ProxyDataSource(makePlainDataSource())

    when:
    DataSource plainDataSource = dataSourceProxyBeanPostProcessor.postProcessAfterInitialization(proxyDataSource, "proxyDataSource") as DataSource

    then:
    plainDataSource.properties.advisors == null
  }

  void "queryLogger - should filter out matching queries at DEBUG level"() {
    given:
    configureEnabledLevels(queryLogger, Level.DEBUG)

    when:
    jdbcTemplate.query("select * from person", new ColumnMapRowMapper())
    jdbcTemplate.query("select * from not_so_interesting_person", new ColumnMapRowMapper())

    ImmutableList<LoggingEvent> loggingEventList = queryLogger.allLoggingEvents

    then:
    loggingEventList.size() == 1
    loggingEventList[0].level == Level.DEBUG
    loggingEventList[0].message.contains("select * from person")
  }

  void "queryLogger - should not filter out matching queries at TRACE level"() {
    given:
    configureEnabledLevels(queryLogger, Level.TRACE)

    when:
    jdbcTemplate.query("select * from person", new ColumnMapRowMapper())
    jdbcTemplate.query("select * from not_so_interesting_person", new ColumnMapRowMapper())

    ImmutableList<LoggingEvent> loggingEventList = queryLogger.allLoggingEvents

    then:
    loggingEventList.size() == 2
    loggingEventList[0].message.contains("select * from person")
    loggingEventList[1].message.contains("select * from not_so_interesting_person")
  }

  void "slowQueryLogger - should log slow queries"() {
    given:
    configureEnabledLevels(slowQueryLogger, Level.INFO)

    when:
    jdbcTemplate.query("select * from person", new ColumnMapRowMapper())
    jdbcTemplate.query("select sleep(1100), name from person", new ColumnMapRowMapper())
    ImmutableList<LoggingEvent> loggingEventList = slowQueryLogger.allLoggingEvents

    then:
    loggingEventList.size() == 1
    loggingEventList[0].level == Level.WARN
    loggingEventList[0].message.contains("select sleep(1100), name from person")
  }
}
