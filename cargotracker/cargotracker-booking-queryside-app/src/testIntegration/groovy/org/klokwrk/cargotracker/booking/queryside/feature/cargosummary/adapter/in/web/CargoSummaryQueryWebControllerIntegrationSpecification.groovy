package org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.adapter.in.web

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracker.booking.queryside.test.base.AbstractQuerySideIntegrationSpecification
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.web.context.WebApplicationContext

import javax.sql.DataSource
import java.nio.charset.Charset

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup

@SpringBootTest
@ActiveProfiles("testIntegration")
class CargoSummaryQueryWebControllerIntegrationSpecification extends AbstractQuerySideIntegrationSpecification {
  @TestConfiguration
  static class TestSpringBootConfiguration {
    @Bean
    Sql groovySql(DataSource dataSource) {
      return new Sql(dataSource)
    }
  }

  @Autowired
  EventBus eventBus

  @Autowired
  Sql groovySql

  @Autowired
  WebApplicationContext webApplicationContext

  @Autowired
  ObjectMapper objectMapper

  MockMvc mockMvc

  void setup() {
    mockMvc ?= webAppContextSetup(webApplicationContext).build()
  }

  void "should work for correct request - [acceptLanguage: #acceptLanguage]"() {
    given:
    String myAggregateIdentifier = publishAndWaitForProjectedCargoBookedEvent(eventBus, groovySql)
    String webRequestBody = objectMapper.writeValueAsString([aggregateIdentifier: myAggregateIdentifier])

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/fetch-cargo-summary")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.OK.value()

    verifyAll(responseContentMap.metaData as Map) {
      locale == localeString
      severity == Severity.INFO.name()
      timestamp
      titleText == "Info"
      titleDetailedText == myTitleDetailedText
    }

    verifyAll(responseContentMap.metaData.http as Map) {
      message == HttpStatus.OK.reasonPhrase
      status == HttpStatus.OK.value().toString()
    }

    verifyAll(responseContentMap.payload as Map) {
      aggregateIdentifier == myAggregateIdentifier
      aggregateSequenceNumber == 0
      originLocation == "HRRJK"
      destinationLocation == "HRZAG"
    }

    where:
    acceptLanguage | localeString | myTitleDetailedText
    "hr-HR"        | "hr_HR"      | "Vaš je zahtjev uspješno izvršen."
    "en"           | "en"         | "Your request is successfully executed."
  }

  void "should return expected response when CargoSummary cannot be found - [acceptLanguage: #acceptLanguage]"() {
    given:
    String myAggregateIdentifier = UUID.randomUUID()
    String webRequestBody = objectMapper.writeValueAsString([aggregateIdentifier: myAggregateIdentifier])

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/fetch-cargo-summary")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.NOT_FOUND.value()

    verifyAll(responseContentMap.metaData as Map) {
      locale == localeString
      severity == Severity.WARNING.name()
      timestamp
      titleText == myTitleText
      titleDetailedText == myTitleDetailedText
    }

    verifyAll(responseContentMap.metaData.http as Map) {
      message == HttpStatus.NOT_FOUND.reasonPhrase
      status == HttpStatus.NOT_FOUND.value().toString()
    }

    verifyAll(responseContentMap.metaData.violation as Map) {
      code == HttpStatus.NOT_FOUND.value().toString()
      codeMessage == myViolationCodeMessage
    }

    verifyAll(responseContentMap.payload as Map) {
      size() == 0
    }

    where:
    acceptLanguage | localeString | myTitleText  | myTitleDetailedText                                 | myViolationCodeMessage
    "hr-HR"        | "hr_HR"      | "Upozorenje" | "Sumarni izvještaj za željeni teret nije pronađen." | "Traženi podaci nisu pronađeni."
    "en"           | "en"         | "Warning"    | "Summary report for specified cargo is not found."  | "Requested data are not found."
  }
}
