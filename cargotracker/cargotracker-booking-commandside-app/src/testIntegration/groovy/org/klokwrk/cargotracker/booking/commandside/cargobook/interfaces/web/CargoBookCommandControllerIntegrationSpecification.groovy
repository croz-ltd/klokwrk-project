package org.klokwrk.cargotracker.booking.commandside.cargobook.interfaces.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.klokwrk.cargotracker.booking.commandside.cargobook.test.base.AbstractCargoBookIntegrationSpecification
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.web.context.WebApplicationContext

import java.nio.charset.Charset

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup

@SpringBootTest
@ActiveProfiles("testIntegration")
class CargoBookCommandControllerIntegrationSpecification extends AbstractCargoBookIntegrationSpecification {
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
    String myAggregateIdentifier = UUID.randomUUID()
    String webRequestBody = objectMapper.writeValueAsString([aggregateIdentifier: myAggregateIdentifier, originLocation: "HRZAG", destinationLocation: "HRRJK"])

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/cargo-book")
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
      originLocation.name == "Zagreb"
      destinationLocation.name == "Rijeka"
    }

    verifyAll(responseContentMap.payload.originLocation as Map) {
      name == "Zagreb"
      nameInternationalized == "Zagreb"

      country.name == "Hrvatska"
      country.nameInternationalized == "Hrvatska"

      unLoCode.code == "HRZAG"
      unLoCode.countryCode == "HR"
      unLoCode.locationCode == "ZAG"
    }

    verifyAll(responseContentMap.payload.destinationLocation as Map) {
      name == "Rijeka"
      nameInternationalized == "Rijeka"

      country.name == "Hrvatska"
      country.nameInternationalized == "Hrvatska"

      unLoCode.code == "HRRJK"
      unLoCode.countryCode == "HR"
      unLoCode.locationCode == "RJK"
    }

    where:
    acceptLanguage | localeString | myTitleDetailedText
    "hr-HR"        | "hr_HR"      | "Vaš je zahtjev uspješno izvršen."
    "en"           | "en"         | "Your request is successfully executed."
  }

  void "should return expected response when request is not valid - [acceptLanguage: #acceptLanguage]"() {
    given:
    String myAggregateIdentifier = UUID.randomUUID()
    String webRequestBody = objectMapper.writeValueAsString([aggregateIdentifier: myAggregateIdentifier, originLocation: "HRZAG", destinationLocation: "HRZAG"])

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/cargo-book")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.BAD_REQUEST.value()

    verifyAll(responseContentMap.metaData as Map) {
      locale == localeString
      severity == Severity.WARNING.name()
      timestamp
      titleText == myTitleText
      titleDetailedText == myTitleDetailedText
    }

    verifyAll(responseContentMap.metaData.http as Map) {
      message == HttpStatus.BAD_REQUEST.reasonPhrase
      status == HttpStatus.BAD_REQUEST.value().toString()
    }

    verifyAll(responseContentMap.metaData.violation as Map) {
      code == HttpStatus.BAD_REQUEST.value().toString()
      codeMessage == myViolationCodeMessage
    }

    verifyAll(responseContentMap.payload as Map) {
      size() == 0
    }

    where:
    acceptLanguage | localeString | myTitleText
    "hr-HR"        | "hr_HR"      | "Upozorenje"
    "en"           | "en"         | "Warning"

    myTitleDetailedText << [
        "Teret nije prihvaćen jer ga nije moguće poslati na ciljnu lokaciju iz navedene početne lokacije.",
        "Cargo is not booked since destination location cannot accept cargo from specified origin location."
    ]

    myViolationCodeMessage << [
        "Teret nije moguće poslati na ciljnu lokaciju iz navedene početne lokacije.",
        "Destination location cannot accept cargo from specified origin location."
    ]
  }
}
