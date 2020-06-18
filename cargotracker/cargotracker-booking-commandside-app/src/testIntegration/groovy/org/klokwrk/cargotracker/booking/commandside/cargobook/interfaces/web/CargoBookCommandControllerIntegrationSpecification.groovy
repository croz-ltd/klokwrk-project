package org.klokwrk.cargotracker.booking.commandside.cargobook.interfaces.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.klokwrk.cargotracker.booking.commandside.cargobook.test.base.AbstractCargoBookIntegrationSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.web.context.WebApplicationContext

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup

@SpringBootTest
class CargoBookCommandControllerIntegrationSpecification extends AbstractCargoBookIntegrationSpecification {
  @Autowired
  WebApplicationContext webApplicationContext

  @Autowired
  ObjectMapper objectMapper

  MockMvc mockMvc

  void setup() {
    mockMvc ?= webAppContextSetup(webApplicationContext).build()
  }

  void "should work for correct request"() {
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
            .header(HttpHeaders.ACCEPT_LANGUAGE, "hr-HR")
    ).andReturn()

    then:
    mvcResult.response.status == HttpStatus.OK.value()
  }
}
