package com.exchange.foreign_exchange_api.repository.custom.impl;

import com.exchange.foreign_exchange_api.FxTestContainersConfiguration;
import com.exchange.foreign_exchange_api.model.CurrencyCode;
import com.exchange.foreign_exchange_api.model.Transaction;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@Import(FxTestContainersConfiguration.class)
@Testcontainers
class TransactionCustomRepositoryImplTest {

  private final UUID transactionId1 = UUID.randomUUID();
  private final UUID transactionId2 = UUID.randomUUID();
  private final Instant now = Instant.now();
  private final Instant hourAgo = now.minus(1, ChronoUnit.HOURS);
  private final Instant twoHoursAgo = now.minus(2, ChronoUnit.HOURS);
  @Autowired private ReactiveMongoTemplate mongoTemplate;
  private TransactionCustomRepositoryImpl repository;

  @BeforeEach
  void setUp() {
    repository = new TransactionCustomRepositoryImpl(mongoTemplate);
    mongoTemplate.dropCollection(Transaction.class).block();
    mongoTemplate
        .insertAll(
            List.of(
                new Transaction(
                    transactionId1, 100.0, CurrencyCode.USD, 85.0, CurrencyCode.EUR, 0.85, hourAgo),
                new Transaction(
                    transactionId2, 200.0, CurrencyCode.EUR, 220.0, CurrencyCode.USD, 1.1, now)))
        .blockLast();
  }

  @Test
  void findByCriteria_shouldReturnAllWithoutFilters() {
    Flux<Transaction> result = repository.findByCriteria(null, null, null, PageRequest.of(0, 10));

    StepVerifier.create(result).expectNextCount(2).verifyComplete();
  }

  @Test
  void findByCriteria_shouldFilterByTransactionId() {
    Flux<Transaction> result =
        repository.findByCriteria(transactionId1, null, null, PageRequest.of(0, 10));

    StepVerifier.create(result)
        .assertNext(tx -> Assertions.assertEquals(transactionId1, tx.id()))
        .verifyComplete();
  }

  @Test
  void findByCriteria_shouldFilterByDateRange() {
    Flux<Transaction> result =
        repository.findByCriteria(null, twoHoursAgo, now, PageRequest.of(0, 10));

    StepVerifier.create(result)
        .assertNext(tx -> Assertions.assertEquals(transactionId1, tx.id()))
        .verifyComplete();
  }

  @Test
  void findByCriteria_shouldApplyPagination() {
    Flux<Transaction> result = repository.findByCriteria(null, null, null, PageRequest.of(1, 1));

    StepVerifier.create(result).expectNextCount(1).verifyComplete();
  }

  @Test
  void countByCriteria_shouldCountAllWithoutFilters() {
    Mono<Long> result = repository.countByCriteria(null, null, null);

    StepVerifier.create(result).expectNext(2L).verifyComplete();
  }

  @Test
  void countByCriteria_shouldFilterByTransactionId() {
    Mono<Long> result = repository.countByCriteria(transactionId1, null, null);

    StepVerifier.create(result).expectNext(1L).verifyComplete();
  }

  @Test
  void countByCriteria_shouldFilterByDateRange() {
    Mono<Long> result =
        repository.countByCriteria(
            null, hourAgo.plus(1, ChronoUnit.SECONDS), now.plus(1, ChronoUnit.HOURS));

    StepVerifier.create(result).expectNext(1L).verifyComplete();
  }
}
