package com.exchange.foreign_exchange_api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.exchange.foreign_exchange_api.exception.ExchangeRateNotFoundException;
import com.exchange.foreign_exchange_api.model.CurrencyCode;
import com.exchange.foreign_exchange_api.model.Transaction;
import com.exchange.foreign_exchange_api.repository.TransactionRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ConversionServiceTest {

  private static final CurrencyCode USD = CurrencyCode.USD;
  private static final CurrencyCode EUR = CurrencyCode.EUR;
  private ExchangeRateService rateService;
  private TransactionRepository transactionRepo;
  private ConversionService conversionService;

  @BeforeEach
  void setUp() {
    rateService = Mockito.mock(ExchangeRateService.class);
    transactionRepo = Mockito.mock(TransactionRepository.class);
    conversionService = new ConversionService(rateService, transactionRepo);
  }

  @Test
  void convert_shouldReturnConversionResponse() {
    UUID transactionId = UUID.randomUUID();
    Instant timestamp = Instant.now();

    when(rateService.getExchangeRate(USD, EUR)).thenReturn(Mono.just(0.92));
    when(transactionRepo.save(any(Transaction.class)))
        .thenReturn(
            Mono.just(new Transaction(transactionId, 100.0, USD, 92.0, EUR, 0.92, timestamp)));

    StepVerifier.create(conversionService.convert(100.0, USD, EUR))
        .expectNextMatches(
            response ->
                response.transactionId().equals(transactionId)
                    && response.source() == USD
                    && response.target() == EUR
                    && response.sourceAmount() == 100.0
                    && response.targetAmount() == 92.0
                    && response.timestamp().equals(timestamp))
        .verifyComplete();
  }

  @Test
  void convert_shouldPropagateRateNotFoundError() {
    when(rateService.getExchangeRate(USD, EUR))
        .thenReturn(Mono.error(new ExchangeRateNotFoundException("Rate not found")));

    StepVerifier.create(conversionService.convert(100.0, USD, EUR))
        .verifyErrorMatches(
            ex ->
                ex instanceof ExchangeRateNotFoundException
                    && ex.getMessage().equals("Rate not found"));
  }

  @Test
  void convert_shouldCalculateCorrectAmount() {
    when(rateService.getExchangeRate(USD, EUR)).thenReturn(Mono.just(0.92));
    when(transactionRepo.save(any(Transaction.class)))
        .thenAnswer(
            invocation -> {
              Transaction t = invocation.getArgument(0);
              return Mono.just(t);
            });

    StepVerifier.create(conversionService.convert(150.0, USD, EUR))
        .expectNextMatches(response -> response.targetAmount() == 138.0)
        .verifyComplete();
  }
}
