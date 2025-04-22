package com.exchange.foreign_exchange_api.service;

import com.exchange.foreign_exchange_api.api.dto.TransactionResponse;
import com.exchange.foreign_exchange_api.model.Transaction;
import com.exchange.foreign_exchange_api.repository.TransactionRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConversionHistoryService {

  private final TransactionRepository transactionRepo;

  public ConversionHistoryService(TransactionRepository transactionRepo) {
    this.transactionRepo = transactionRepo;
  }

  public Mono<ConversionHistoryResult> getConversionHistory(
      UUID transactionId, Instant start, Instant end, int page, int limit) {
    Pageable pageable = PageRequest.of(page, limit);

    return Mono.zip(
            transactionRepo
                .findByCriteria(transactionId, start, end, pageable)
                .map(this::toResponse)
                .collectList(),
            transactionRepo.countByCriteria(transactionId, start, end))
        .map(tuple -> new ConversionHistoryResult(tuple.getT1(), tuple.getT2()));
  }

  private TransactionResponse toResponse(Transaction transaction) {
    return new TransactionResponse(
        transaction.id(),
        transaction.sourceAmount(),
        transaction.sourceCurrency(),
        transaction.targetAmount(),
        transaction.targetCurrency(),
        transaction.exchangeRate(),
        transaction.timestamp());
  }

  public record ConversionHistoryResult(List<TransactionResponse> data, long totalCount) {}

}
