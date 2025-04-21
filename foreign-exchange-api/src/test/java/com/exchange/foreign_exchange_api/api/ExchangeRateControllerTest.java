package com.exchange.foreign_exchange_api.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.exchange.foreign_exchange_api.exception.ExchangeRateNotFoundException;
import com.exchange.foreign_exchange_api.model.CurrencyCode;
import com.exchange.foreign_exchange_api.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = ExchangeRateController.class)
@Import(ExchangeRateControllerTest.TestConfig.class)
class ExchangeRateControllerTest {

  private static final CurrencyCode USD = CurrencyCode.USD;
  private static final CurrencyCode EUR = CurrencyCode.EUR;
  private static final double TEST_RATE = 0.92;

  @Autowired private WebTestClient webTestClient;
  @Autowired private ExchangeRateService exchangeRateService;

  @Test
  void getExchangeRate_shouldReturnRate_whenValidRequest() {
    when(exchangeRateService.getExchangeRate(any(), any())).thenReturn(Mono.just(TEST_RATE));

    webTestClient
        .get()
        .uri("/api/exchange-rates?source=USD&target=EUR")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.source")
        .isEqualTo(USD.name())
        .jsonPath("$.target")
        .isEqualTo(EUR.name())
        .jsonPath("$.rate")
        .isEqualTo(TEST_RATE)
        .jsonPath("$.timestamp")
        .exists();
  }

  @Test
  void getExchangeRate_shouldReturn400_whenMissingParameters() {
    webTestClient.get().uri("/api/exchange-rates").exchange().expectStatus().isBadRequest();
  }

  @Test
  void getExchangeRate_shouldReturn404_whenRateNotFound() {
    when(exchangeRateService.getExchangeRate(any(), any()))
        .thenReturn(Mono.error(new ExchangeRateNotFoundException("Not found")));

    webTestClient
        .get()
        .uri("/api/exchange-rates?source=USD&target=JPY")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.message")
        .isNotEmpty();
  }

  @Test
  void getExchangeRate_shouldReturn400_whenInvalidCurrency() {
    webTestClient
        .get()
        .uri("/api/exchange-rates?source=INVALID&target=EUR")
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void getExchangeRate_whenServiceThrowsUnexpectedException_returnsInternalServerError() {
    when(exchangeRateService.getExchangeRate(any(), any())).thenThrow(new RuntimeException("Boom"));

    webTestClient
        .get()
        .uri("/exchange-rate?source=USD&target=EUR")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        .expectBody()
        .jsonPath("$.message")
        .isEqualTo("Unexpected error occurred");
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public ExchangeRateService exchangeRateService() {
      return Mockito.mock(ExchangeRateService.class);
    }
  }
}
