package com.exchange.foreign_exchange_api.repository;

import com.exchange.foreign_exchange_api.model.Transaction;
import java.util.UUID;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, UUID> {
}