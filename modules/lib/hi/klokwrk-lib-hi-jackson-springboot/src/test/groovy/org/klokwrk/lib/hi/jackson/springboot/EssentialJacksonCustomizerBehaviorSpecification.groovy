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
package org.klokwrk.lib.hi.jackson.springboot

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import spock.lang.Specification

import javax.measure.Quantity
import javax.measure.quantity.Mass
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@JsonTest
class EssentialJacksonCustomizerBehaviorSpecification extends Specification {
  @SuppressWarnings("CodeNarc.UnnecessaryTransientModifier")
  static class MyBeanWithTransientProperties {
    transient String first
    String last

    String getFullName() {
      return "$first $last"
    }
  }

  static class MyBean {
    String first
    String last
  }

  static class MyBeanWithDefaultPropertyValues {
    String first = "someFirst"
    String last = "someLast"
  }

  static class MyBeanWithArray {
    String first
    String last
    String[] nameList
  }

  static class MyBeanWithTimeStamps {
    Date legacyDate
    Instant instant
    LocalDateTime localDateTime
    OffsetDateTime offsetDateTime
    ZonedDateTime zonedDateTime
  }

  static enum MyEnum {
    ONE, TWO
  }

  static class MyBeanWithEnum {
    String myName
    MyEnum myEnum
  }

  static class MyBeanWithQuantity {
    String name
    Quantity<Mass> weight
    Quantity length
  }

  @Autowired
  ObjectMapper objectMapper

  void "deserialization - should deserialize empty string into null"() {
    given:
    String stringToDeserialize = """
      {
        "cargoIdentifier": ${ cargoIdentifierStringValue },
        "originLocation": "myOrigin",
        "destinationLocation": "myDestination"
      }
      """

    when:
    Map<String, ?> deserializedMap = objectMapper.readValue(stringToDeserialize, Map)

    then:
    deserializedMap.cargoIdentifier == null

    where:
    cargoIdentifierStringValue | _
    '""'                       | _
    '"    "'                   | _
  }

  void "deserialization - should not deserialize transient properties"() {
    given:
    String stringToDeserialize = """
      {
        "first": "someFirst",
        "last": "someLast"
      }
      """

    when:
    MyBeanWithTransientProperties myBeanWithTransientProperties = objectMapper.readValue(stringToDeserialize, MyBeanWithTransientProperties)

    then:
    myBeanWithTransientProperties.first == null
    myBeanWithTransientProperties.last == "someLast"
    myBeanWithTransientProperties.fullName == "null someLast"
  }

  void "deserialization - should allow comments in JSON"() {
    given:
    String stringToDeserialize = """
      {
        /* some comment */
        "cargoIdentifier": "someIdentifier", /* some comment */
        "originLocation": "myOrigin", /* some comment */
        "destinationLocation": "myDestination" /* some comment */
      }
      """

    when:
    Map<String, ?> deserializedMap = objectMapper.readValue(stringToDeserialize, Map)

    then:
    deserializedMap.cargoIdentifier == "someIdentifier"
  }

  void "deserialization - should not fail for unknown properties"() {
    given:
    String stringToDeserialize = """
      {
        "first": "someFirst",
        "bla": "bla"
      }
      """

    when:
    MyBean myBean = objectMapper.readValue(stringToDeserialize, MyBean)

    then:
    myBean.first == "someFirst"
    myBean.last == null
  }

  void "deserialization - should not deserialize null values"() {
    given:
    String stringToDeserialize = """
      {
        "first": "someFirst",
        "last": null
      }
      """

    when:
    MyBeanWithDefaultPropertyValues myBeanWithDefaultPropertyValues = objectMapper.readValue(stringToDeserialize, MyBeanWithDefaultPropertyValues)

    then:
    myBeanWithDefaultPropertyValues.first == "someFirst"
    myBeanWithDefaultPropertyValues.last == "someLast"
  }

  void "deserialization - should configure skipNullValues only for default Spring Boot object mapper"() {
    given:
    EssentialJacksonCustomizer essentialJacksonCustomizer = new EssentialJacksonCustomizer(new EssentialJacksonCustomizerConfigurationProperties())

    when:
    ObjectMapper objectMapper = essentialJacksonCustomizer.postProcessAfterInitialization(new ObjectMapper(), "nonDefaultSpringBootNameForObjectMapperBean") as ObjectMapper

    then:
    objectMapper.deserializationConfig.defaultSetterInfo != JsonSetter.Value.forValueNulls(Nulls.SKIP)
  }

  void "deserialization - should deserialize single value into an array"() {
    given:
    String stringToDeserialize = """
      {
        "first": "someFirst",
        "last": "someLast",
        "nameList": "singleNameInList"
      }
      """

    when:
    MyBeanWithArray myBeanWithArray = objectMapper.readValue(stringToDeserialize, MyBeanWithArray)

    then:
    myBeanWithArray.first == "someFirst"
    myBeanWithArray.last == "someLast"
    myBeanWithArray.nameList.size() == 1
    myBeanWithArray.nameList[0] == "singleNameInList"
  }

  void "deserialization - timestamp types should work as expected"() {
    given:
    String legacyDateString = "2020-04-04T22:35:35.654+0200"
    SimpleDateFormat legacySimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", new Locale("hr"))
    legacySimpleDateFormat.timeZone = TimeZone.getTimeZone("Europe/Zagreb")
    Date legacyDate = legacySimpleDateFormat.parse(legacyDateString)

    String stringToDeserialize = /{
        "legacyDate":"2020-04-04T20:35:35.654+0000",
        "instant":"2020-04-04T20:35:35.654321Z",
        "localDateTime":"2020-04-04T22:35:35.654321",
        "offsetDateTime":"2020-04-04T22:35:35.654321+02:00",
        "zonedDateTime":"2020-04-04T22:35:35.654321+02:00"
    }/
    Clock clock = Clock.fixed(Instant.parse("2020-04-04T20:35:35.654321Z"), ZoneId.of("Europe/Zagreb"))

    when:
    MyBeanWithTimeStamps myBeanWithTimeStamps = objectMapper.readValue(stringToDeserialize, MyBeanWithTimeStamps)

    then:
    myBeanWithTimeStamps.legacyDate == legacyDate
    myBeanWithTimeStamps.instant == Instant.now(clock)
    myBeanWithTimeStamps.localDateTime.isEqual(LocalDateTime.now(clock))
    myBeanWithTimeStamps.offsetDateTime.isEqual(OffsetDateTime.now(clock))
    myBeanWithTimeStamps.zonedDateTime.isEqual(ZonedDateTime.now(clock))
  }

  void "deserialization - should be case-insensitive about enum names"() {
    given:
    String stringToDeserialize = """
      {
        "myName": "someName",
        "myEnum": "${ myEnumStringValueParam }"
      }
      """

    when:
    MyBeanWithEnum myBeanWithEnum = objectMapper.readValue(stringToDeserialize, MyBeanWithEnum)

    then:
    myBeanWithEnum.myName == "someName"
    myBeanWithEnum.myEnum == MyEnum.ONE

    where:
    myEnumStringValueParam | _
    "one"                  | _
    "ONE"                  | _
  }

  void "deserialization - should deserialize uom Quantity"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": {
          "value": 1234,
          "unitSymbol": "kg"
        },
        "length": {
          "value": 456,
          "unitSymbol": "m"
        }
      }
      """

    when:
    MyBeanWithQuantity myBeanWithQuantity = objectMapper.readValue(stringToDeserialize, MyBeanWithQuantity)

    then:
    myBeanWithQuantity.name == "someName"
    myBeanWithQuantity.weight == 1234.kg
    myBeanWithQuantity.length == 456.m
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "serialization - should serialize only non-null values"() {
    given:
    MyBean myBean = new MyBean(first: "someFirst")
    assert myBean.last == null

    when:
    String serializedString = objectMapper.writeValueAsString(myBean)

    then:
    serializedString.contains(/"last":/) == false
    serializedString.contains(/null/) == false

    serializedString.contains(/"first":/) == true
    serializedString.contains(/"someFirst"/) == true
  }

  void "serialization - should serialize GString as a String"() {
    given:
    Closure closure = {
      return "${ 123 } 456"
    }

    Map mapToSerialize = [
        "bla": closure()
    ]

    when:
    String serializedString = objectMapper.writeValueAsString(mapToSerialize)

    then:
    serializedString.contains("123 456")
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "serialization - should not serialize transient properties"() {
    given:
    MyBeanWithTransientProperties myBeanWithTransientProperties = new MyBeanWithTransientProperties(first: "someFirst", last: "someLast")

    when:
    String serializedString = objectMapper.writeValueAsString(myBeanWithTransientProperties)

    then:
    serializedString.contains(/"first":/) == false
    serializedString.contains(/"someFirst"/) == false
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "serialization - should not serialize read-only properties"() {
    given:
    MyBeanWithTransientProperties myBeanWithTransientProperties = new MyBeanWithTransientProperties(first: "someFirst", last: "someLast")

    when:
    String serializedString = objectMapper.writeValueAsString(myBeanWithTransientProperties)

    then:
    serializedString.contains(/"fullName":/) == false
    serializedString.contains(/"someFirst someLast"/) == false
  }

  void "serialization - timestamp types should work as expected"() {
    given:
    String legacyDateString = "2020-04-04T22:35:35.654+02:00"
    SimpleDateFormat legacySimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", new Locale("hr"))
    legacySimpleDateFormat.timeZone = TimeZone.getTimeZone("Europe/Zagreb")
    Date legacyDate = legacySimpleDateFormat.parse(legacyDateString)

    Clock clock = Clock.fixed(Instant.parse("2020-04-04T20:35:35.654321Z"), ZoneId.of("Europe/Zagreb"))
    MyBeanWithTimeStamps myBeanWithTimeStamps = new MyBeanWithTimeStamps(
        legacyDate: legacyDate, instant: Instant.now(clock), localDateTime: LocalDateTime.now(clock), offsetDateTime: OffsetDateTime.now(clock), zonedDateTime: ZonedDateTime.now(clock)
    )

    when:
    String serializedString = objectMapper.writeValueAsString(myBeanWithTimeStamps)

    then:
    serializedString.contains(/"legacyDate":"2020-04-04T20:35:35.654+00:00"/)
    serializedString.contains(/"instant":"2020-04-04T20:35:35.654321Z"/)
    serializedString.contains(/"localDateTime":"2020-04-04T22:35:35.654321"/)
    serializedString.contains(/"offsetDateTime":"2020-04-04T22:35:35.654321+02:00"/)
    serializedString.contains(/"zonedDateTime":"2020-04-04T22:35:35.654321+02:00"/)
  }

  void "serialization - should work with enums as expected"() {
    given:
    MyBeanWithEnum myBeanWithEnum = new MyBeanWithEnum(myName: "someName", myEnum: MyEnum.TWO)

    when:
    String serializedString = objectMapper.writeValueAsString(myBeanWithEnum)

    then:
    serializedString.contains(/"myEnum":"TWO"/)
  }

  void "serialization - should serialize uom Quantity"() {
    given:
    MyBeanWithQuantity myBeanWithQuantity = new MyBeanWithQuantity(name: "someName", weight: 1234.kg, length: 456.m)

    when:
    String serializedString = objectMapper.writeValueAsString(myBeanWithQuantity)

    then:
    serializedString == /{"name":"someName","weight":{"value":1234,"unitSymbol":"kg"},"length":{"value":456,"unitSymbol":"m"}}/
  }
}
