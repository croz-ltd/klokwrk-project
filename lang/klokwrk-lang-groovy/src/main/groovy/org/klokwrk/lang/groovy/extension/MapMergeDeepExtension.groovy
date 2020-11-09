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
package org.klokwrk.lang.groovy.extension

import groovy.transform.CompileStatic

/**
 * Groovy extension for <code>Map</code> that provides mergeDeep method for deep merging of current map with provided overrides.
 * <p/>
 * Similar to Groovy plus operator for maps, but instead of overriding only on root map keys, it overrides on leaf level which gives much more precision and flexibility.
 * For example, with Groovy plus operator we have
 * <pre>
 * Map myMap = [b: [b1: 1, b2: 1]]
 * Map myOverride = [b: [b1: 3]]
 *
 * assert myMap + myOverride == [b: [b1: 3]]
 * </pre>
 *
 * With this extension we have the following
 * <pre>
 * Map myMap = [b: [b1: 1, b2: 1]]
 * Map myOverride = [b: [b1: 3]]
 *
 * assert myMap.mergeDeep(myOverride) == [b: [b1: 3, b2: 1]]
 * </pre>
 * Another important distinction is that Groovy plus operator for maps produces a new map, while this extension modifies original map. To produce new map with this extension we can do the following:
 * <pre>
 * [:].mergeDeep(originalMap, override1, override2)
 * </pre>
 * References:
 * <ul>
 *   <li>https://blog.mathieu.photography/post/103163278870/deep-merge-map-in-groovy</li>
 *   <li>https://stackoverflow.com/questions/34326587/merge-two-maps-to-resultant-map-in-groovy</li>
 * </ul>
 */
@SuppressWarnings(["unused", "Instanceof"])
@CompileStatic
class MapMergeDeepExtension {
  static Map mergeDeep(Map self, Map... overrides) {
    if (!overrides) {
      return self
    }

    if (overrides.length == 1) {
      overrides[0].each { k, v ->
        if (v instanceof Map && self[k] instanceof Map) {
          mergeDeep((Map) self[k], (Map) v)
        }
        else {
          self[k] = v
        }
      }

      return self
    }

    return overrides.inject(self, { Map acc, Map override -> mergeDeep(acc, override ?: [:]) })
  }
}
