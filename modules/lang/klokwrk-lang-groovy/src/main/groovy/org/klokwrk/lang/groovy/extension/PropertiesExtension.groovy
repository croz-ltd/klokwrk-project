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
package org.klokwrk.lang.groovy.extension

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.contracts.base.ContractsBase

import static org.klokwrk.lang.groovy.contracts.base.ContractsBase.requireTrueBase

/**
 * Groovy extension for <code>Object</code> that fetches properties without commonly unwanted properties like "<code>class</code>".
 */
@SuppressWarnings("unused")
@CompileStatic
class PropertiesExtension {
  /**
   * List of property names that are filtered-out by default.
   * <p/>
   * Properties in the list are: "<code>class</code>".
   */
  static final List<String> DEFAULT_FILTER_OUT_PROPERTY_NAME_LIST = ["class"]

  /**
   * Returns a map of all properties without those whose names are on the default filtered-out list {@link #DEFAULT_FILTER_OUT_PROPERTY_NAME_LIST}.
   */
  static Map<String, ?> getPropertiesFiltered(Object self) {
    Map<String, ?> filteredProperties = self.properties.findAll { !DEFAULT_FILTER_OUT_PROPERTY_NAME_LIST.contains(it.key) }
    return filteredProperties
  }

  /**
   * Returns a map of all properties except those whose names are specified on the <code>filterOutPropertyNameList</code> param.
   */
  static Map<String, ?> getPropertiesFiltered(Object self, List<String> filterOutPropertyNameList) {
    requireTrueBase(filterOutPropertyNameList != null, "${ ContractsBase.REQUIRE_TRUE_MESSAGE_DEFAULT } - [condition: (filterOutPropertyNameList != null)]")

    Map<String, ?> filteredProperties = self.properties.findAll { !filterOutPropertyNameList.contains(it.key) }
    return filteredProperties
  }
}
