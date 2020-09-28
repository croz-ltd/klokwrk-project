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
    Map<String, ?> filteredProperties = self.properties.findAll { it.key as String !in DEFAULT_FILTER_OUT_PROPERTY_NAME_LIST }
    return filteredProperties
  }

  /**
   * Returns a map of all properties except those whose names are specified on the <code>filterOutPropertyNameList</code> param.
   */
  static Map<String, ?> getPropertiesFiltered(Object self, List<String> filterOutPropertyNameList) {
    requireTrueBase(filterOutPropertyNameList != null, "${ ContractsBase.REQUIRE_TRUE_MESSAGE_DEFAULT } - [condition: (filterOutPropertyNameList != null)]")

    Map<String, ?> filteredProperties = self.properties.findAll { it.key as String !in filterOutPropertyNameList }
    return filteredProperties
  }
}
