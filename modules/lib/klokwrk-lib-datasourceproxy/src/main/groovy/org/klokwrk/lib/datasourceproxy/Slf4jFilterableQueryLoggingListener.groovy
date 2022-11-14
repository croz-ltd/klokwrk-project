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
package org.klokwrk.lib.datasourceproxy

import groovy.transform.CompileStatic
import net.ttddyy.dsproxy.ExecutionInfo
import net.ttddyy.dsproxy.QueryInfo
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener
import net.ttddyy.dsproxy.support.SLF4JLogUtils
import org.klokwrk.lang.groovy.contracts.base.ContractsBase

import java.util.regex.Pattern

import static org.klokwrk.lang.groovy.contracts.base.ContractsBase.requireTrueBase

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
    requireTrueBase(filteringOutPatternStringList != null, "$ContractsBase.REQUIRE_TRUE_MESSAGE_DEFAULT - [condition: filteringOutPatternStringList != null]")

    this.filteringOutPatternList = filteringOutPatternStringList.collect({ String patternString -> Pattern.compile(patternString) })
  }

  @Override
  void afterQuery(ExecutionInfo execInfo, List<QueryInfo> originalQueryInfoList) {
    if (loggingCondition.asBoolean) {
      if (logger.isTraceEnabled()) {
        String entry = getEntry(execInfo, originalQueryInfoList)
        SLF4JLogUtils.writeLog(logger, SLF4JLogLevel.TRACE, entry)
      }
      else {
        List<QueryInfo> filteredOutQueryInfoList = filterOutQueryInfoList(originalQueryInfoList)
        if (!filteredOutQueryInfoList.isEmpty()) {
          String entry = getEntry(execInfo, filteredOutQueryInfoList)
          writeLog(entry)
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
