package com.exchange.foreign_exchange_api.repository;

import com.exchange.foreign_exchange_api.model.Transaction;
import com.exchange.foreign_exchange_api.repository.custom.TransactionCustomRepository;
import java.util.UUID;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TransactionRepository
    extends ReactiveMongoRepository<Transaction, UUID>, TransactionCustomRepository {}
