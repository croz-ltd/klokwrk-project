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
package org.klokwrk.cargotracker.lib.test.support.assertion

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.transform.stc.SimpleType
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError

/**
 * Parent class for each assertion class that wants to check a map representing pageable payload returned from some operation.
 * <p/>
 * The general structure of the response map corresponds to
 * <pre>
 * [
 *   metadata:[
 *     // metadata's keys and values. Not relevant in the context of this class. They are asserted by MetaDataAssertion.
 *   ],
 *   payload:[
 *     pageInfo:[
 *       // pageInfo's keys and values assertable by PageInfoAssertion
 *     ],
 *     pageContent: [ // pageContent is a list of maps
 *       [
 *         // first pageContent item's key and values
 *         // second pageContent item's key and values
 *         // ...
 *       ]
 *     ]
 *   ]
 * ]
 * </pre>
 *
 * This class is defines general structures and rules (implemented as methods) for asserting {@code payload.pageInfo} and {@code payload.pageContent}. While {@code payload.pageInfo} asserting is
 * delegated to {@link PageInfoAssertion} instances, individual instances of {@code payload.pageContent} are asserted by {@code PAGE_ITEM_ASSERTION} type.
 * <p/>
 * Therefore, any concrete class extending this one, has to provide {@code PAGE_ITEM_ASSERTION} type via generic declaration, where {@code PAGE_ITEM_ASSERTION} instances are then responsible
 * for asserting the details of each item in {@code payload.pageContent}.
 * <p/>
 * In addition, {@code PAGE_ITEM_ASSERTION} types have to implement {@link PageItemAssertionable} to make it possible for instances of this class to invoke their methods when appropriate.
 *
 * @param <SELF> The type of "{@code this}" instance representing the instance of a subclass in the context of this abstract superclass.
 * @param <PAGE_ITEM_ASSERTION> The type of class capable to assert individual item of {@code pageContent} list.
 */
@CompileStatic
abstract class PageablePayloadAssertion<SELF extends PageablePayloadAssertion<SELF, PAGE_ITEM_ASSERTION>, PAGE_ITEM_ASSERTION extends PageItemAssertionable> {
  static void assertResponse(Map responseMap) {
    assert responseMap instanceof Map
    responseMap.with {
      assert size() == 2
      assert metaData instanceof Map
      assert payload instanceof Map
    }
  }

  abstract PAGE_ITEM_ASSERTION getPageItemAssertionInstance(Map pageItemMap)

  @SuppressWarnings("GrFinalVariableAccess")
  protected final Map payloadMap

  protected PageablePayloadAssertion(Map responseMap) {
    assert responseMap instanceof Map
    assert responseMap.payload instanceof Map
    this.payloadMap = responseMap.payload as Map
  }

  SELF isSuccessful() {
    payloadMap.with {
      assert size() == 2
      assert pageInfo instanceof Map
      assert pageContent instanceof List
      assert !(pageContent as List).isEmpty()

      new PageInfoAssertion(pageInfo as Map).with {
        isSuccessful()
      }

      (pageContent as List<Map>).with {
        it.each {
          getPageItemAssertionInstance(it).isSuccessful()
        }
      }
    }

    return this as SELF
  }

  SELF isSuccessfulAndEmpty() {
    payloadMap.with {
      assert size() == 2
      assert pageInfo instanceof Map
      assert pageContent instanceof List
      assert (pageContent as List).isEmpty()
    }

    new PageInfoAssertion(payloadMap.pageInfo as Map).with {
      isSuccessfulForEmptyPageContent()
    }

    return this as SELF
  }

  SELF isEmpty() {
    assert payloadMap.size() == 0
    return this as SELF
  }

  SELF hasPageInfoThat(
      @DelegatesTo(value = PageInfoAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracker.lib.test.support.assertion.PageInfoAssertion"
      ) Closure aClosure)
  {
    Object pageInfoMap = payloadMap.pageInfo
    assert pageInfoMap instanceof Map

    PageInfoAssertion pageInfoAssertion = new PageInfoAssertion(pageInfoMap as Map)
    aClosure.resolveStrategy = Closure.DELEGATE_FIRST
    aClosure.delegate = pageInfoAssertion
    aClosure.call(pageInfoAssertion)

    return this as SELF
  }

  SELF hasPageInfoOfFirstPageWithDefaults() {
    PageInfoAssertion pageInfoAssertion = new PageInfoAssertion(payloadMap.pageInfo as Map)
    pageInfoAssertion.isFirstPageWithDefaults()
    return this as SELF
  }

  SELF hasPageContentSizeGreaterThanOrEqual(Long comparablePageContentSize) {
    assert (payloadMap.pageContent as List<Map>).size() >= comparablePageContentSize
    return this as SELF
  }

  SELF hasPageContentWithAnyItemThat(
      @DelegatesTo(type = "PAGE_ITEM_ASSERTION", strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = FromString,
          options = "PAGE_ITEM_ASSERTION"
      ) Closure aClosure)
  {
    Object pageContentListAsObject = payloadMap.pageContent
    assert pageContentListAsObject instanceof List

    List<Map> pageContentList = pageContentListAsObject as List<Map>
    pageContentList.each { pageItemMap ->
      assert pageItemMap instanceof Map
    }

    aClosure.resolveStrategy = Closure.DELEGATE_FIRST

    boolean isAnyItemFound = pageContentList.any({ Map pageItemMap ->
      PAGE_ITEM_ASSERTION pageItemAssertion = getPageItemAssertionInstance(pageItemMap)

      aClosure.delegate = pageItemAssertion
      try {
        aClosure.call(pageItemAssertion)
        return true
      }
      catch (PowerAssertionError ignore) {
      }

      return false
    })

    if (!isAnyItemFound) {
      throw new AssertionError("Assertion failed - none of the page items satisfies provided conditions." as Object)
    }

    return this as SELF
  }

  SELF hasPageContentWithAllItemsThat(
      @DelegatesTo(type = "PAGE_ITEM_ASSERTION", strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = FromString,
          options = "PAGE_ITEM_ASSERTION"
      ) Closure aClosure)
  {
    Object pageContentListAsObject = payloadMap.pageContent
    assert pageContentListAsObject instanceof List

    List<Map> pageContentList = pageContentListAsObject as List<Map>
    assert !pageContentList.isEmpty()
    pageContentList.each { pageItemMap ->
      assert pageItemMap instanceof Map
    }

    pageContentList.eachWithIndex({ Map pageItemMap, int anIndex ->
      PAGE_ITEM_ASSERTION pageItemAssertion = getPageItemAssertionInstance(pageItemMap)

      aClosure.delegate = pageItemAssertion
      //noinspection UnnecessaryQualifiedReference
      aClosure.resolveStrategy = Closure.DELEGATE_FIRST

      try {
        aClosure.call(pageItemAssertion)
      }
      catch (PowerAssertionError powerAssertionError) {
        throw new AssertionError("Assertion failed at the page item with the index of [${ anIndex }].", powerAssertionError)
      }
    })

    return this as SELF
  }

  SELF hasPageContentWithFirstItemThat(
      @DelegatesTo(type = "PAGE_ITEM_ASSERTION", strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = FromString,
          options = "PAGE_ITEM_ASSERTION"
      ) Closure aClosure)
  {
    hasPageContentWithItemAtIndexThat(0, aClosure)
    return this as SELF
  }

  SELF hasPageContentWithItemAtIndexThat(
      Integer anIndex,
      @DelegatesTo(type = "PAGE_ITEM_ASSERTION", strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = FromString,
          options = "PAGE_ITEM_ASSERTION"
      ) Closure aClosure)
  {
    Object pageContentListAsObject = payloadMap.pageContent
    assert pageContentListAsObject instanceof List

    List<Map> pageContentList = pageContentListAsObject as List<Map>
    assert !pageContentList.isEmpty()
    assert pageContentList[anIndex] instanceof Map

    PAGE_ITEM_ASSERTION pageItemAssertion = getPageItemAssertionInstance(pageContentList[anIndex])
    aClosure.resolveStrategy = Closure.DELEGATE_FIRST
    aClosure.delegate = pageItemAssertion
    aClosure.call(pageItemAssertion)

    return this as SELF
  }
}
