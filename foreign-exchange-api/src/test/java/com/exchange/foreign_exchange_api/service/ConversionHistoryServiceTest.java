package com.exchange.foreign_exchange_api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import com.exchange.foreign_exchange_api.api.dto.TransactionResponse;
import com.exchange.foreign_exchange_api.model.CurrencyCode;
import com.exchange.foreign_exchange_api.model.Transaction;
import com.exchange.foreign_exchange_api.repository.TransactionRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ConversionHistoryServiceTest {

  private final UUID testTransactionId = UUID.randomUUID();
  private final Instant testTimestamp = Instant.now();
  private final Transaction testTransaction =
      new Transaction(
          testTransactionId, 100.0, CurrencyCode.USD, 85.0, CurrencyCode.EUR, 0.85, testTimestamp);
  private TransactionRepository transactionRepo;
  private ConversionHistoryService service;

  @BeforeEach
  void setUp() {
    transactionRepo = Mockito.mock(TransactionRepository.class);
    service = new ConversionHistoryService(transactionRepo);
  }

  @Test
  void getConversionHistory_shouldReturnPaginatedResults() {
    PageRequest pageable = PageRequest.of(0, 25);
    Mockito.when(transactionRepo.findByCriteria(null, null, null, pageable))
        .thenReturn(Flux.just(testTransaction));
    Mockito.when(transactionRepo.countByCriteria(null, null, null)).thenReturn(Mono.just(1L));

    Mono<ConversionHistoryService.ConversionHistoryResult> result =
        service.getConversionHistory(null, null, null, 0, 25);

    StepVerifier.create(result)
        .assertNext(
            res -> {
              Assertions.assertEquals(1, res.data().size());
              Assertions.assertEquals(1L, res.totalCount());
              TransactionResponse response = res.data().get(0);
              Assertions.assertEquals(testTransactionId, response.id());
              Assertions.assertEquals(CurrencyCode.USD, response.sourceCurrency());
              Assertions.assertEquals(CurrencyCode.EUR, response.targetCurrency());
              Assertions.assertEquals(100.0, response.sourceAmount(), 0.001);
              Assertions.assertEquals(85.0, response.targetAmount(), 0.001);
              Assertions.assertEquals(testTimestamp, response.timestamp());
            })
        .verifyComplete();
  }

  @Test
  void getConversionHistory_shouldFilterByTransactionId() {
    Mockito.when(transactionRepo.findByCriteria(eq(testTransactionId), any(), any(), any()))
        .thenReturn(Flux.just(testTransaction));
    Mockito.when(transactionRepo.countByCriteria(eq(testTransactionId), any(), any()))
        .thenReturn(Mono.just(1L));

    Mono<ConversionHistoryService.ConversionHistoryResult> result =
        service.getConversionHistory(testTransactionId, null, null, 0, 10);

    StepVerifier.create(result)
        .assertNext(
            res -> {
              Assertions.assertEquals(testTransactionId, res.data().get(0).id());
            })
        .verifyComplete();
  }

  @Test
  void getConversionHistory_shouldReturnEmptyForNoResults() {
    Mockito.when(transactionRepo.findByCriteria(any(), any(), any(), any()))
        .thenReturn(Flux.empty());
    Mockito.when(transactionRepo.countByCriteria(any(), any(), any())).thenReturn(Mono.just(0L));

    Mono<ConversionHistoryService.ConversionHistoryResult> result =
        service.getConversionHistory(null, null, null, 0, 10);

    StepVerifier.create(result)
        .assertNext(
            res -> {
              Assertions.assertTrue(res.data().isEmpty());
              Assertions.assertEquals(0L, res.totalCount());
            })
        .verifyComplete();
  }

  @Test
  void getConversionHistory_shouldHandleRepositoryErrors() {
    Mockito.when(transactionRepo.findByCriteria(any(), any(), any(), any()))
        .thenReturn(Flux.error(new RuntimeException("DB error")));
    Mockito.when(transactionRepo.countByCriteria(any(), any(), any()))
        .thenReturn(Mono.error(new RuntimeException("DB error")));

    Mono<ConversionHistoryService.ConversionHistoryResult> result =
        service.getConversionHistory(null, null, null, 0, 10);

    StepVerifier.create(result).expectError(RuntimeException.class).verify();
  }
}
