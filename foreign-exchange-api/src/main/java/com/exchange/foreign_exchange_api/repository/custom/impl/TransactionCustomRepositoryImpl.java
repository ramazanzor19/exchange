package com.exchange.foreign_exchange_api.repository.custom.impl;

import com.exchange.foreign_exchange_api.model.Transaction;
import com.exchange.foreign_exchange_api.repository.custom.TransactionCustomRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TransactionCustomRepositoryImpl implements TransactionCustomRepository {

  private static final String ID = "_id";
  private static final String TIMESTAMP = "timestamp";

  private ReactiveMongoTemplate mongoTemplate;

  public TransactionCustomRepositoryImpl(ReactiveMongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Flux<Transaction> findByCriteria(
      UUID transactionId, Instant start, Instant end, Pageable pageable) {
    Query query =
        new Query().with(pageable).skip(pageable.getOffset()).limit(pageable.getPageSize());

    applyCriteria(query, transactionId, start, end);

    return mongoTemplate.find(query, Transaction.class);
  }

  @Override
  public Mono<Long> countByCriteria(UUID transactionId, Instant start, Instant end) {
    Query query = new Query();
    applyCriteria(query, transactionId, start, end);
    return mongoTemplate.count(query, Transaction.class);
  }

  private void applyCriteria(Query query, UUID transactionId, Instant start, Instant end) {
    Stream.of(
            Optional.ofNullable(transactionId).map(id -> Criteria.where(ID).is(id)),
            Optional.ofNullable(start).map(s -> Criteria.where(TIMESTAMP).gte(s)),
            Optional.ofNullable(end).map(e -> Criteria.where(TIMESTAMP).lt(e)))
        .flatMap(Optional::stream)
        .reduce((criteria1, criteria2) -> new Criteria().andOperator(criteria1, criteria2))
        .ifPresent(query::addCriteria);
  }
}
