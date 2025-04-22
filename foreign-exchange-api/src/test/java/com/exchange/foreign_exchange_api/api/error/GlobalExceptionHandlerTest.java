package com.exchange.foreign_exchange_api.api.error;

import com.exchange.foreign_exchange_api.api.dto.TransactionRequest;
import com.exchange.foreign_exchange_api.exception.ExchangeRateNotFoundException;
import com.exchange.foreign_exchange_api.exception.PageOutOfRangeException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = GlobalExceptionHandlerTest.TestExceptionControllers.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

  @Autowired private WebTestClient webTestClient;

  @Test
  void whenExchangeRateNotFound_thenReturns404() {
    webTestClient
        .get()
        .uri("/test/exceptions/rate-not-found")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody(ErrorResponse.class)
        .value(
            response ->
                Assertions.assertEquals("Rate not found for USD to JPY", response.message()));
  }

  @Test
  void whenPageOutOfRange_thenReturns404() {
    webTestClient
        .get()
        .uri("/test/exceptions/page-out-of-range")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody(ErrorResponse.class)
        .value(
            response ->
                Assertions.assertEquals("Page 5 is out of range (max 3)", response.message()));
  }

  @Test
  void whenBeanInstantiationWithIllegalArg_thenReturns400() {
    webTestClient
        .get()
        .uri("/test/exceptions/bean-instantiation-illegalarg")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ErrorResponse.class)
        .value(response -> Assertions.assertEquals("Invalid limit value", response.message()));
  }

  @Test
  void whenBeanInstantiationWithOtherError_thenReturns500() {
    webTestClient
        .get()
        .uri("/test/exceptions/bean-instantiation-other")
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ErrorResponse.class)
        .value(
            response -> Assertions.assertEquals("Invalid request parameters", response.message()));
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public TestExceptionControllers testController() {
      return new TestExceptionControllers();
    }
  }

  @RestController
  @RequestMapping("/test/exceptions")
  static class TestExceptionControllers {
    @GetMapping("/rate-not-found")
    Mono<Void> throwRateNotFound() {
      throw new ExchangeRateNotFoundException("Rate not found for USD to JPY");
    }

    @GetMapping("/page-out-of-range")
    Mono<Void> throwPageOutOfRange() {
      throw new PageOutOfRangeException(5, 3);
    }

    @PostMapping("/validation-fail")
    Mono<Void> triggerValidation(@Valid @RequestBody InvalidRequest request) {
      return Mono.empty();
    }

    @GetMapping("/bean-instantiation-illegalarg")
    Mono<Void> throwBeanInstantiationWithIllegalArg() {
      throw new BeanInstantiationException(
          TransactionRequest.class, "Test", new IllegalArgumentException("Invalid limit value"));
    }

    @GetMapping("/bean-instantiation-other")
    Mono<Void> throwBeanInstantiationWithOther() {
      throw new BeanInstantiationException(
          TransactionRequest.class, "Test", new NullPointerException("Unexpected error"));
    }

    @GetMapping("/generic-error")
    Mono<Void> throwGenericError() {
      throw new RuntimeException("Database connection failed");
    }
  }

  // Invalid request DTO for validation testing
  record InvalidRequest(@NotBlank String name, @Min(1) int quantity) {}
}
