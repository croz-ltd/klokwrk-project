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
 *     // metadata's keys and values. Not relevant in the context of this class. They are asserted by ResponseContentMetaDataAssertion.
 *   ],
 *   payload:[
 *     pageInfo:[
 *       // pageInfo's keys and values assertable by ResponseContentPayloadPageInfoAssertion
 *     ],
 *     pageContent: [ // pageContent is a list of maps
 *       [
 *         // first pageContent element's key and values
 *         // second pageContent element's key and values
 *         // ...
 *       ]
 *     ]
 *   ]
 * ]
 * </pre>
 *
 * This class is defines general structures and rules (implemented as methods) for asserting {@code payload.pageInfo} and {@code payload.pageContent}. While {@code payload.pageInfo} asserting is
 * delegated to {@link ResponseContentPayloadPageInfoAssertion} instances, individual instances of {@code payload.pageContent} are asserted by {@code PAGE_CONTENT_ASSERTION} type.
 * <p/>
 * Therefore, any concrete class extending this one, has to provide {@code PAGE_CONTENT_ASSERTION} type via generic declaration, where {@code PAGE_CONTENT_ASSERTION} instances are then responsible
 * for asserting the details of each element in {@code payload.pageContent}.
 * <p/>
 * In addition, {@code PAGE_CONTENT_ASSERTION} types have to implement {@link PayloadPageContentAssertionable} to make it possible for instances of this class to invoke their methods when appropriate.
 *
 * @param <SELF> The type of "{@code this}" instance representing the instance of a subclass in the context of this abstract superclass.
 * @param <PAGE_CONTENT_ASSERTION> The type of class capable to assert individual elements of {@code pageContent} list.
 */
@CompileStatic
abstract class ResponseContentPageablePayloadAssertion<SELF extends ResponseContentPageablePayloadAssertion<SELF, PAGE_CONTENT_ASSERTION>, PAGE_CONTENT_ASSERTION extends PayloadPageContentAssertionable> {
  static void assertResponseContent(Map responseContentMap) {
    assert responseContentMap instanceof Map
    responseContentMap.with {
      assert size() == 2
      assert metaData instanceof Map
      assert payload instanceof Map
    }
  }

  abstract PAGE_CONTENT_ASSERTION getPageContentAssertionInstance(Map pageContentElementPayloadMap)

  @SuppressWarnings("GrFinalVariableAccess")
  protected final Map payloadMap

  protected ResponseContentPageablePayloadAssertion(Map responseContentMap) {
    assert responseContentMap instanceof Map
    assert responseContentMap.payload instanceof Map
    this.payloadMap = responseContentMap.payload as Map
  }

  SELF isSuccessful() {
    payloadMap.with {
      assert size() == 2
      assert pageInfo instanceof Map
      assert pageContent instanceof List
      assert !(pageContent as List).isEmpty()

      new ResponseContentPayloadPageInfoAssertion(pageInfo as Map).with {
        isSuccessful()
      }

      (pageContent as List<Map>).with {
        it.each {
          getPageContentAssertionInstance(it).isSuccessful()
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

    new ResponseContentPayloadPageInfoAssertion(payloadMap.pageInfo as Map).with {
      isSuccessfulForEmptyPageContent()
    }

    return this as SELF
  }

  SELF isEmpty() {
    assert payloadMap.size() == 0
    return this as SELF
  }

  SELF hasPageInfoThat(
      @DelegatesTo(value = ResponseContentPayloadPageInfoAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracker.lib.test.support.assertion.ResponseContentPayloadPageInfoAssertion"
      ) Closure aClosure)
  {
    Object pageInfoMap = payloadMap.pageInfo
    assert pageInfoMap instanceof Map

    ResponseContentPayloadPageInfoAssertion pageInfoAssertion = new ResponseContentPayloadPageInfoAssertion(pageInfoMap as Map)
    aClosure.resolveStrategy = Closure.DELEGATE_FIRST
    aClosure.delegate = pageInfoAssertion
    aClosure.call(pageInfoAssertion)

    return this as SELF
  }

  SELF hasPageInfoOfFirstPageWithDefaults() {
    ResponseContentPayloadPageInfoAssertion pageInfoAssertion = new ResponseContentPayloadPageInfoAssertion(payloadMap.pageInfo as Map)
    pageInfoAssertion.isFirstPageWithDefaults()
    return this as SELF
  }

  SELF hasPageContentSizeGreaterThanOrEqual(Long comparablePageContentSize) {
    assert (payloadMap.pageContent as List<Map>).size() >= comparablePageContentSize
    return this as SELF
  }

  SELF hasPageContentWithAnyElementThat(
      @DelegatesTo(type = "PAGE_CONTENT_ASSERTION", strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = FromString,
          options = "PAGE_CONTENT_ASSERTION"
      ) Closure aClosure)
  {
    Object pageContentListAsObject = payloadMap.pageContent
    assert pageContentListAsObject instanceof List

    List<Map> pageContentList = pageContentListAsObject as List<Map>
    pageContentList.each { pageContentElementMap ->
      assert pageContentElementMap instanceof Map
    }

    aClosure.resolveStrategy = Closure.DELEGATE_FIRST

    boolean isAnyElementFound = pageContentList.any({ Map pageContentElementMap ->
      PAGE_CONTENT_ASSERTION pageContentElementAssertion = getPageContentAssertionInstance(pageContentElementMap)

      aClosure.delegate = pageContentElementAssertion
      try {
        aClosure.call(pageContentElementAssertion)
        return true
      }
      catch (PowerAssertionError ignore) {
      }

      return false
    })

    if (!isAnyElementFound) {
      throw new AssertionError("Assertion failed - none of the list elements satisfies provided conditions." as Object)
    }

    return this as SELF
  }

  SELF hasPageContentWithAllElementsThat(
      @DelegatesTo(type = "PAGE_CONTENT_ASSERTION", strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = FromString,
          options = "PAGE_CONTENT_ASSERTION"
      ) Closure aClosure)
  {
    Object pageContentListAsObject = payloadMap.pageContent
    assert pageContentListAsObject instanceof List

    List<Map> pageContentList = pageContentListAsObject as List<Map>
    assert !pageContentList.isEmpty()
    pageContentList.each { pageContentElementMap ->
      assert pageContentElementMap instanceof Map
    }

    pageContentList.eachWithIndex({ Map pageContentElementMap, int anIndex ->
      PAGE_CONTENT_ASSERTION aListElementPayloadAssertion = getPageContentAssertionInstance(pageContentElementMap)

      aClosure.delegate = aListElementPayloadAssertion
      //noinspection UnnecessaryQualifiedReference
      aClosure.resolveStrategy = Closure.DELEGATE_FIRST

      try {
        aClosure.call(aListElementPayloadAssertion)
      }
      catch (PowerAssertionError aPowerAssertionError) {
        throw new AssertionError("Assertion failed at the element with the index of [${ anIndex }].", aPowerAssertionError)
      }
    })

    return this as SELF
  }

  SELF hasPageContentWithFirstElementThat(
      @DelegatesTo(type = "PAGE_CONTENT_ASSERTION", strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = FromString,
          options = "PAGE_CONTENT_ASSERTION"
      ) Closure aClosure)
  {
    hasPageContentWithElementAtIndexThat(0, aClosure)
    return this as SELF
  }

  SELF hasPageContentWithElementAtIndexThat(
      Integer anIndex,
      @DelegatesTo(type = "PAGE_CONTENT_ASSERTION", strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = FromString,
          options = "PAGE_CONTENT_ASSERTION"
      ) Closure aClosure)
  {
    Object pageContentListAsObject = payloadMap.pageContent
    assert pageContentListAsObject instanceof List

    List<Map> pageContentList = pageContentListAsObject as List<Map>
    assert !pageContentList.isEmpty()
    assert pageContentList[anIndex] instanceof Map

    PAGE_CONTENT_ASSERTION pageContentElementAssertion = getPageContentAssertionInstance(pageContentList[anIndex])
    aClosure.resolveStrategy = Closure.DELEGATE_FIRST
    aClosure.delegate = pageContentElementAssertion
    aClosure.call(pageContentElementAssertion)

    return this as SELF
  }
}
