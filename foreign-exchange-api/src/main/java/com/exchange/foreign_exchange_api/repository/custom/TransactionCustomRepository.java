package com.exchange.foreign_exchange_api.repository.custom;

import com.exchange.foreign_exchange_api.model.Transaction;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionCustomRepository {

  Flux<Transaction> findByCriteria(
      UUID transactionId, Instant start, Instant end, Pageable pageable);

  Mono<Long> countByCriteria(UUID transactionId, Instant start, Instant end);
}
