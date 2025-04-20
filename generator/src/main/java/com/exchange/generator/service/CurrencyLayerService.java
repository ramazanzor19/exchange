package com.exchange.generator.service;

import com.exchange.generator.model.CurrencyCode;
import com.exchange.generator.model.ExchangeRateResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class CurrencyLayerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyLayerService.class);

  private final WebClient currencyLayerWebClient;

  public CurrencyLayerService(WebClient currencyLayerWebClient) {
    this.currencyLayerWebClient = currencyLayerWebClient;
  }

  public Mono<ExchangeRateResponse> getExchangeRate() {
    return currencyLayerWebClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/live").queryParam("source", CurrencyCode.USD).build())
        .retrieve()
        .bodyToMono(ExchangeRateResponse.class)
        .retryWhen(
            Retry.backoff(3, Duration.ofSeconds(2))
                .doBeforeRetry(
                    signal ->
                        LOGGER.warn(
                            "Retrying CurrencyLayer API call: attempt {}",
                            signal.totalRetries() + 1)))
        .doOnError(error -> LOGGER.error("CurrencyLayer API is unavailable", error));
  }
}
