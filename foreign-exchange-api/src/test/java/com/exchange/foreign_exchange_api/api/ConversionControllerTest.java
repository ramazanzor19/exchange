package com.exchange.foreign_exchange_api.api;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;

import com.exchange.foreign_exchange_api.api.dto.ConversionResponse;
import com.exchange.foreign_exchange_api.exception.ExchangeRateNotFoundException;
import com.exchange.foreign_exchange_api.model.CurrencyCode;
import com.exchange.foreign_exchange_api.service.ConversionService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(ConversionController.class)
@Import(ConversionControllerTest.TestConfig.class)
class ConversionControllerTest {

  private static final CurrencyCode USD = CurrencyCode.USD;
  private static final CurrencyCode EUR = CurrencyCode.EUR;

  @Autowired private WebTestClient webTestClient;
  @Autowired private ConversionService conversionService;

  @Test
  void convert_shouldReturn200_whenValidRequest() {
    UUID transactionId = UUID.randomUUID();
    Mockito.when(conversionService.convert(anyDouble(), any(), any()))
        .thenReturn(
            Mono.just(new ConversionResponse(transactionId, USD, 100.0, EUR, 92.0, Instant.now())));

    webTestClient
        .post()
        .uri("/api/convert")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
                {
                  "amount": 100,
                  "source": "USD",
                  "target": "EUR"
                }
                """)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.transactionId")
        .isEqualTo(transactionId.toString())
        .jsonPath("$.source")
        .isEqualTo("USD")
        .jsonPath("$.target")
        .isEqualTo("EUR")
        .jsonPath("$.sourceAmount")
        .isEqualTo(100.0)
        .jsonPath("$.targetAmount")
        .isEqualTo(92.0);
  }

  @Test
  void convert_shouldReturn400_whenInvalidCurrency() {
    webTestClient
        .post()
        .uri("/api/convert")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
                {
                  "amount": 100,
                  "source": "INVALID",
                  "target": "EUR"
                }
                """)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.message")
        .value(containsString("Validation failed"));
  }

  @Test
  void convert_shouldReturn404_whenRateNotFound() {
    Mockito.when(conversionService.convert(anyDouble(), any(), any()))
        .thenReturn(Mono.error(new ExchangeRateNotFoundException("Rate not found")));

    webTestClient
        .post()
        .uri("/api/convert")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
                {
                  "amount": 100,
                  "source": "USD",
                  "target": "EUR"
                }
                """)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public ConversionService conversionService() {
      return Mockito.mock(ConversionService.class);
    }
  }
}
