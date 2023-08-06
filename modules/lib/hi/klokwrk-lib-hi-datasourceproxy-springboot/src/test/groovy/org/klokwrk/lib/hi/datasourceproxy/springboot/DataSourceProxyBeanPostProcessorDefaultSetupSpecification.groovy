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
package org.klokwrk.lib.hi.datasourceproxy.springboot

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import net.ttddyy.dsproxy.support.ProxyDataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.ApplicationContext
import org.springframework.jdbc.core.ColumnMapRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

import javax.sql.DataSource

@JdbcTest
class DataSourceProxyBeanPostProcessorDefaultSetupSpecification extends Specification {

  @Autowired
  ApplicationContext applicationContext

  @Autowired
  JdbcTemplate jdbcTemplate

  private List configureLoggerAndListAppender(Level loggerLevel, Logger logger = LoggerFactory.getLogger("klokwrk.datasourceproxy.queryLogger") as Logger) {
    logger.level = loggerLevel
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)

    return [logger, listAppender]
  }

  private void cleanupLogger(Logger logger, ListAppender listAppender) {
    logger.detachAppender(listAppender)
  }

  private DataSource makePlainDataSource() {
    DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create()

    dataSourceBuilder
        .driverClassName("org.h2.Driver")
        .url("jdbc:h2:mem:test")
        .username("SA")
        .password("")

    return dataSourceBuilder.build()
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
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender(Level.DEBUG)

    when:
    jdbcTemplate.query("select * from person", new ColumnMapRowMapper())
    jdbcTemplate.query("select * from not_so_interesting_person", new ColumnMapRowMapper())

    then:
    listAppender.list.size() == 1
    verifyAll(listAppender.list[0]) {
      level == Level.DEBUG
      message.contains("select * from person")
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "queryLogger - should not filter out matching queries at TRACE level"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender(Level.TRACE)

    when:
    jdbcTemplate.query("select * from person", new ColumnMapRowMapper())
    jdbcTemplate.query("select * from not_so_interesting_person", new ColumnMapRowMapper())

    then:
    listAppender.list.size() == 2
    listAppender.list[0].message.contains("select * from person")
    listAppender.list[1].message.contains("select * from not_so_interesting_person")

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "slowQueryLogger - should log slow queries"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender(Level.INFO, LoggerFactory.getLogger("klokwrk.datasourceproxy.slowQueryLogger") as Logger)

    when:
    jdbcTemplate.query("select * from person", new ColumnMapRowMapper())
    jdbcTemplate.query("select sleep(2100), name from person", new ColumnMapRowMapper())

    then:
    listAppender.list.size() == 1
    verifyAll(listAppender.list[0]) {
      level == Level.WARN
      message.contains("select sleep(2100), name from person")
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }
}
