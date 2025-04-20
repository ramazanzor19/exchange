package com.exchange.generator.scheduler;

import com.exchange.generator.publisher.ExchangeRatePublisher;
import com.exchange.generator.service.CurrencyLayerService;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class ExchangeRateScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeRateScheduler.class);
  private static final String EXCHANGE_RATE_KEY = "exchangeRate";

  private final CurrencyLayerService currencyLayerService;
  private final ExchangeRatePublisher exchangeRatePublisher;

  public ExchangeRateScheduler(
      CurrencyLayerService currencyLayerService, ExchangeRatePublisher exchangeRatePublisher) {
    this.currencyLayerService = currencyLayerService;
    this.exchangeRatePublisher = exchangeRatePublisher;
  }

  @Scheduled(fixedRate = 60000)
  public void scheduledRateUpdate() {
    fetchAndPublishRates().subscribeOn(Schedulers.boundedElastic()).subscribe();
  }

  public Mono<Boolean> fetchAndPublishRates() {
    return currencyLayerService
        .getExchangeRate()
        .flatMap(
            response -> {
              if (response.success()) {
                return exchangeRatePublisher.publishExchangeRates(
                    EXCHANGE_RATE_KEY, transformQuotes(response.quotes()));
              }
              LOGGER.error("Failed to fetch exchange rates");
              return Mono.empty();
            });
  }

  private Map<String, Double> transformQuotes(Map<String, Double> quotes) {
    return quotes.entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getKey().substring(3), Map.Entry::getValue));
  }
}
