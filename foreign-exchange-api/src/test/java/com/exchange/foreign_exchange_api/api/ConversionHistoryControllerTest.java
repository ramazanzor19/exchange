package com.exchange.foreign_exchange_api.api;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import com.exchange.foreign_exchange_api.api.dto.TransactionResponse;
import com.exchange.foreign_exchange_api.model.CurrencyCode;
import com.exchange.foreign_exchange_api.service.ConversionHistoryService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(ConversionHistoryController.class)
@Import({ConversionHistoryControllerTest.TestConfig.class}) // Import any required validation config
class ConversionHistoryControllerTest {

  private final TransactionResponse mockTransaction =
      new TransactionResponse(
          UUID.randomUUID(), 100.0, CurrencyCode.USD, 85.0, CurrencyCode.EUR, 0.85, Instant.now());

  @Autowired private WebTestClient webTestClient;
  @Autowired private ConversionHistoryService historyService;

  @Test
  void getConversionHistory_shouldReturn200WithValidRequest() {
    // Mock service response
    Mockito.when(historyService.getConversionHistory(any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(
            Mono.just(
                new ConversionHistoryService.ConversionHistoryResult(List.of(mockTransaction), 1)));

    webTestClient
        .get()
        .uri("/api/conversions?page=0&limit=25")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.data[0].sourceCurrency")
        .isEqualTo("USD")
        .jsonPath("$.meta.totalCount")
        .isEqualTo(1);
  }

  @Test
  void getConversionHistory_shouldFilterByTransactionId() {
    UUID transactionId = UUID.randomUUID();
    Mockito.when(
            historyService.getConversionHistory(
                eq(transactionId), any(), any(), anyInt(), anyInt()))
        .thenReturn(
            Mono.just(
                new ConversionHistoryService.ConversionHistoryResult(List.of(mockTransaction), 1)));

    webTestClient
        .get()
        .uri("/api/conversions?transactionId={id}&page=0&limit=25", transactionId)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.data.length()")
        .isEqualTo(1);
  }

  @Test
  void getConversionHistory_shouldValidateLimitValues() {
    webTestClient
        .get()
        .uri("/api/conversions?limit=15") // Invalid limit
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.message")
        .value(containsString("must be one of"));
  }

  @Test
  void getConversionHistory_shouldValidateDateRange() {
    Mockito.when(historyService.getConversionHistory(isNull(), any(), any(), anyInt(), anyInt()))
        .thenReturn(
            Mono.just(
                new ConversionHistoryService.ConversionHistoryResult(List.of(mockTransaction), 1)));

    webTestClient
        .get()
        .uri("/api/conversions?start=2023-01-02T00:00:00Z&end=2023-01-03T00:00:00Z&limit=25")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.data.length()")
        .isEqualTo(1);
  }

  @Test
  void getConversionHistory_shouldValidateOnlyStartDate() {
    Mockito.when(historyService.getConversionHistory(isNull(), any(), any(), anyInt(), anyInt()))
        .thenReturn(
            Mono.just(
                new ConversionHistoryService.ConversionHistoryResult(List.of(mockTransaction), 1)));

    webTestClient
        .get()
        .uri("/api/conversions?start=2023-01-02T00:00:00Z&limit=25")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.data.length()")
        .isEqualTo(1);
  }

  @Test
  void getConversionHistory_shouldInvalidateDateRange() {
    webTestClient
        .get()
        .uri("/api/conversions?start=2023-01-02T00:00:00Z&end=2023-01-01T00:00:00Z&limit=25")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.message")
        .value(containsString("Start date must be before end date"));
  }

  @Test
  void getConversionHistory_shouldReturn404ForPageOutOfRange() {
    Mockito.when(historyService.getConversionHistory(any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(Mono.just(new ConversionHistoryService.ConversionHistoryResult(List.of(), 10)));

    webTestClient
        .get()
        .uri("/api/conversions?page=5&limit=10")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.message")
        .value(containsString("Page 5 is out of range"));
  }

  @Test
  void getConversionHistory_shouldUseDefaultValues() {
    Mockito.when(historyService.getConversionHistory(isNull(), isNull(), isNull(), eq(0), eq(25)))
        .thenReturn(
            Mono.just(
                new ConversionHistoryService.ConversionHistoryResult(List.of(mockTransaction), 1)));

    webTestClient
        .get()
        .uri("/api/conversions?limit=25") // No params
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.meta.limit")
        .isEqualTo(25)
        .jsonPath("$.meta.currentPage")
        .isEqualTo(0);
  }

  @Test
  void getConversionHistory_shouldHandleServiceErrors() {
    Mockito.when(historyService.getConversionHistory(any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(Mono.error(new RuntimeException("Database error")));

    webTestClient
        .get()
        .uri("/api/conversions?page=0&limit=25")
        .exchange()
        .expectStatus()
        .is5xxServerError();
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public ConversionHistoryService historyService() {
      return Mockito.mock(ConversionHistoryService.class);
    }
  }
}
