package com.exchange.foreign_exchange_api.service;

import com.exchange.foreign_exchange_api.api.dto.ConversionResponse;
import com.exchange.foreign_exchange_api.model.CurrencyCode;
import com.exchange.foreign_exchange_api.model.Transaction;
import com.exchange.foreign_exchange_api.repository.TransactionRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConversionService {

  private final ExchangeRateService rateService;
  private final TransactionRepository transactionRepo;

  public ConversionService(ExchangeRateService rateService, TransactionRepository transactionRepo) {
    this.rateService = rateService;
    this.transactionRepo = transactionRepo;
  }

  public Mono<ConversionResponse> convert(double amount, CurrencyCode source, CurrencyCode target) {
    return rateService
        .getExchangeRate(source, target)
        .flatMap(
            rate -> {
              double convertedAmount = amount * rate;
              Transaction transaction =
                  new Transaction(
                      UUID.randomUUID(),
                      amount,
                      source,
                      convertedAmount,
                      target,
                      rate,
                      Instant.now());
              return transactionRepo
                  .save(transaction)
                  .map(
                      saved ->
                          new ConversionResponse(
                              saved.id(),
                              saved.sourceCurrency(),
                              saved.sourceAmount(),
                              saved.targetCurrency(),
                              saved.targetAmount(),
                              saved.timestamp()));
            });
  }
}
