package net.croz.cargotracker.infrastructure.project.web.spring.mvc

import groovy.json.JsonSlurper
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@WebAppConfiguration
@ContextConfiguration(classes = [WebConfig])
class ResponseFormattingResponseBodyAdviceSpecification extends Specification {

  @SuppressWarnings("Indentation")
  @RestController
  @RequestMapping("/test")
  static class TestController {
    @PostMapping("/testControllerMethodWithOperationResponse")
    OperationResponse<Map> testControllerMethodWithOperationResponse() {
      return new OperationResponse<Map>(payload: [data: "Some testing data"])
    }

    @PostMapping("/testControllerMethodWithMapResponse")
    Map testControllerMethodWithMapResponse() {
      return [data: "Some testing data"]
    }

    @PostMapping("/testControllerMethodWithoutResponse")
    void testControllerMethodWithoutResponse() {
    }
  }

  @SuppressWarnings("Indentation")
  @ControllerAdvice
  static class ResponseFormattingResponseBodyAdviceControllerAdvice extends ResponseFormattingResponseBodyAdvice {
  }

  @SuppressWarnings("Indentation")
  @EnableWebMvc
  @Configuration
  static class WebConfig implements WebMvcConfigurer {
    @Bean
    TestController testController() {
      return new TestController()
    }

    @Bean
    ResponseFormattingResponseBodyAdviceControllerAdvice responseFormattingResponseBodyAdviceControllerAdvice() {
      return new ResponseFormattingResponseBodyAdviceControllerAdvice()
    }

    @Bean
    MessageSource messageSource() {
      ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource()
      messageSource.setDefaultEncoding("UTF-8")
      messageSource.setBasenames("responseFormattingDefaultMessages", "responseFormattingTestMessages")
      return messageSource
    }
  }

  @Autowired
  WebApplicationContext webApplicationContext

  JsonSlurper jsonSlurper
  MockMvc mockMvc

  void setup() {
    jsonSlurper = new JsonSlurper()
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                             .build()
  }

  void "should format successful response as expected when result is returned from controller"() {
    when:
    MvcResult mvcResult = mockMvc.perform(post("/test/testControllerMethodWithOperationResponse").accept(MediaType.APPLICATION_JSON)).andReturn()
    String bodyText = mvcResult.response.contentAsString
    Map bodyMap = jsonSlurper.parseText(bodyText) as Map

    then:
    verifyAll {
      mvcResult.response.status == HttpStatus.OK.value()

      bodyMap
      bodyMap.payload
      bodyMap.metaData

      bodyMap.payload.data == "Some testing data"

      bodyMap.metaData.http
      bodyMap.metaData.http.status == "200"
      bodyMap.metaData.http.message == "OK"

      bodyMap.metaData.violation == null
      bodyMap.metaData.timestamp
      bodyMap.metaData.severity == "INFO"
      bodyMap.metaData.locale == "en"
      bodyMap.metaData.titleText == "Info"
      bodyMap.metaData.titleDetailedText == "Your request is successfully executed."
    }
  }

  void "should ignore response that is not an instance of OperationResponse"() {
    when:
    MvcResult mvcResult = mockMvc.perform(post("/test/testControllerMethodWithMapResponse").accept(MediaType.APPLICATION_JSON)).andReturn()
    String bodyText = mvcResult.response.contentAsString
    Map bodyMap = jsonSlurper.parseText(bodyText) as Map

    then:
    verifyAll {
      mvcResult.response.status == HttpStatus.OK.value()

      bodyMap
      bodyMap.data == "Some testing data"
    }
  }

  void "should ignore response without body"() {
    when:
    MvcResult mvcResult = mockMvc.perform(post("/test/testControllerMethodWithoutResponse").accept(MediaType.APPLICATION_JSON)).andReturn()
    String bodyText = mvcResult.response.contentAsString

    then:
    verifyAll {
      mvcResult.response.status == HttpStatus.OK.value()
      bodyText == ""
    }
  }
}
