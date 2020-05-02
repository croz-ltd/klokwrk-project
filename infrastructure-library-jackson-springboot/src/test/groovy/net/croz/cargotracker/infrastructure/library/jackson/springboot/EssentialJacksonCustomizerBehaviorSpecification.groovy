package net.croz.cargotracker.infrastructure.library.jackson.springboot

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@JsonTest
class EssentialJacksonCustomizerBehaviorSpecification extends Specification {
  @Autowired
  ObjectMapper objectMapper

  @Unroll
  void "deserialization - should deserialize empty string into null - string value is #aggregateIdentifierStringValue"() {
    given:
    String stringToDeserialize = """
      {
        "aggregateIdentifier": ${ aggregateIdentifierStringValue },
        "originLocation": "myOrigin",
        "destinationLocation": "myDestination"
      }
      """

    when:
    Map<String, ?> deserializedMap = objectMapper.readValue(stringToDeserialize, Map)

    then:
    deserializedMap.aggregateIdentifier == null

    where:
    aggregateIdentifierStringValue | _
    '""'                           | _
    '"    "'                       | _
  }

  static class MyBeanWithTransientProperties {
    transient String first
    String last
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
  }

  void "deserialization - should allow comments in JSON"() {
    given:
    String stringToDeserialize = """
      {
        /* some comment */
        "aggregateIdentifier": "someIdentifier", /* some comment */
        "originLocation": "myOrigin", /* some comment */
        "destinationLocation": "myDestination" /* some comment */
      }
      """

    when:
    Map<String, ?> deserializedMap = objectMapper.readValue(stringToDeserialize, Map)

    then:
    deserializedMap.aggregateIdentifier == "someIdentifier"
  }

  static class MyBean {
    String first
    String last
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

  static class MyBeanWithDefaultPropertyValues {
    String first = "someFirst"
    String last = "someLast"
  }

  void "deserialization - null should not deserialize null values"() {
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

  static class MyBeanWithArray {
    String first
    String last
    String[] nameList
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

  static class MyBeanWithTimeStamps {
    Date legacyDate
    Instant instant
    LocalDateTime localDateTime
    OffsetDateTime offsetDateTime
    ZonedDateTime zonedDateTime
  }

  void "deserialization - timestamp types should work as expected"() {
    given:
    String legacyDateString = "2020-04-04T22:35:35.654+0200"
    SimpleDateFormat legacySimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", new Locale("hr"))
    legacySimpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Zagreb"))
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
      return "${123} 456"
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

  void "serialization - timestamp types should work as expected"() {
    given:
    String legacyDateString = "2020-04-04T22:35:35.654+0200"
    SimpleDateFormat legacySimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", new Locale("hr"))
    legacySimpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Zagreb"))
    Date legacyDate = legacySimpleDateFormat.parse(legacyDateString)

    Clock clock = Clock.fixed(Instant.parse("2020-04-04T20:35:35.654321Z"), ZoneId.of("Europe/Zagreb"))
    MyBeanWithTimeStamps myBeanWithTimeStamps = new MyBeanWithTimeStamps(
        legacyDate: legacyDate, instant: Instant.now(clock), localDateTime: LocalDateTime.now(clock), offsetDateTime: OffsetDateTime.now(clock), zonedDateTime: ZonedDateTime.now(clock)
    )

    when:
    String serializedString = objectMapper.writeValueAsString(myBeanWithTimeStamps)
    println serializedString

    then:
    serializedString.contains(/"legacyDate":"2020-04-04T20:35:35.654+0000"/)
    serializedString.contains(/"instant":"2020-04-04T20:35:35.654321Z"/)
    serializedString.contains(/"localDateTime":"2020-04-04T22:35:35.654321"/)
    serializedString.contains(/"offsetDateTime":"2020-04-04T22:35:35.654321+02:00"/)
    serializedString.contains(/"zonedDateTime":"2020-04-04T22:35:35.654321+02:00"/)
  }
}
