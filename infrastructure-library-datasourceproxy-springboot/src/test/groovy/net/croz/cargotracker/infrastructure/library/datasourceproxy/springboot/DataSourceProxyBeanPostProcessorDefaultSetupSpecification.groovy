package net.croz.cargotracker.infrastructure.library.datasourceproxy.springboot

import com.google.common.collect.ImmutableList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.ApplicationContext
import org.springframework.jdbc.core.ColumnMapRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.LoggingEvent
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

@JdbcTest
class DataSourceProxyBeanPostProcessorDefaultSetupSpecification extends Specification {

  TestLogger queryLogger
  TestLogger slowQueryLogger

  @Autowired
  ApplicationContext applicationContext

  @Autowired
  JdbcTemplate jdbcTemplate

  void configureEnabledLevels(TestLogger testLogger, Level enabledLevel) {
    List<Level> allLevels = [Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE]
    List<Level> selectedLevels = allLevels[0..allLevels.findIndexOf({ Level level -> level == enabledLevel })]
    testLogger.setEnabledLevelsForAllThreads(selectedLevels as Level[])
  }

  void setup() {
    TestLoggerFactory.clearAll()
    TestLoggerFactory.instance.setPrintLevel(Level.DEBUG) // uncomment if you want to see logging output during the test
    queryLogger = TestLoggerFactory.getTestLogger("cargotracker.data-source-proxy.queryLogger")
    slowQueryLogger = TestLoggerFactory.getTestLogger("cargotracker.data-source-proxy.slowQueryLogger")
  }

  void "should configure proxying of default data source"() {
    expect:
    //noinspection GroovyAssignabilityCheck,GrUnresolvedAccess
    applicationContext.getBean("dataSource").properties.advisors[0].advice.getClass() == DataSourceProxyInterceptor
  }

  @SuppressWarnings("SqlResolve")
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

  @SuppressWarnings("SqlResolve")
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

  @SuppressWarnings("SqlResolve")
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
