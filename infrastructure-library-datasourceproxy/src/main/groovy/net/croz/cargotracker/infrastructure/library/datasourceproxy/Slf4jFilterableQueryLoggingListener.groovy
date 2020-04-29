package net.croz.cargotracker.infrastructure.library.datasourceproxy

import groovy.transform.CompileStatic
import net.ttddyy.dsproxy.ExecutionInfo
import net.ttddyy.dsproxy.QueryInfo
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener
import net.ttddyy.dsproxy.support.SLF4JLogUtils

import java.util.regex.Pattern

/**
 * Extends DataSourceProxy's {@link SLF4JQueryLoggingListener} by adding capability of filtering out queries based on regular expression match.
 * <p/>
 * If needed, filtered out queries can still be sent to log if the logging level is set to <code>TRACE</code> for the appropriate logger.
 * <p/>
 * Filtering queries might be very useful when there is some kind of recurring queries happening, for example when we have some kind of database polling. One good example is Axon queryside projection
 * application, where projection is projecting into RDBMS database. In that scenario, Axon continually issues <code>update token_entry</code> statements for maintaining <code>token_entry</code> table.
 * Commonly we do not want to see these statements all the time and it is convenient to be able to filter them out.
 */
@CompileStatic
class Slf4jFilterableQueryLoggingListener extends SLF4JQueryLoggingListener {
  List<Pattern> filteringOutPatternList

  Slf4jFilterableQueryLoggingListener(List<String> filteringOutPatternStringList = []) {
    super()

    this.filteringOutPatternList = filteringOutPatternStringList.collect({ String patternString -> Pattern.compile(patternString) })
  }

  @Override
  void afterQuery(ExecutionInfo execInfo, List<QueryInfo> originalQueryInfoList) {
    if (loggingCondition.getAsBoolean()) {
      List<QueryInfo> filteredOutQueryInfoList = filterOutQueryInfoList(originalQueryInfoList)
      if (filteredOutQueryInfoList) {
        String entry = getEntry(execInfo, filteredOutQueryInfoList)
        writeLog(entry)
      }
      else {
        if (getLogger().isTraceEnabled()) {
          String entry = getEntry(execInfo, originalQueryInfoList)
          SLF4JLogUtils.writeLog(getLogger(), SLF4JLogLevel.TRACE, entry)
        }
      }
    }
  }

  /**
   * Filters out original {@link QueryInfo} list based on supplied list of regular expression patterns.
   */
  protected List<QueryInfo> filterOutQueryInfoList(List<QueryInfo> originalQueryInfoList) {
    List<QueryInfo> filteredQueryInfoList = originalQueryInfoList.findAll({ QueryInfo queryInfo ->
      String databaseQueryString = queryInfo.query

      Boolean matches = filteringOutPatternList.find({ Pattern pattern ->
        return pattern.matcher(databaseQueryString)
      }) as Boolean

      if (matches) {
        return false
      }

      return true
    })

    return filteredQueryInfoList
  }
}
