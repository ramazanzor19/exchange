package com.exchange.generator.service;

import com.exchange.generator.model.CurrencyCode;
import com.exchange.generator.model.ExchangeRateResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CurrencyLayerService {

  private final WebClient currencyLayerWebClient;

  public CurrencyLayerService(WebClient currencyLayerWebClient) {
    this.currencyLayerWebClient = currencyLayerWebClient;
  }

  public Mono<ExchangeRateResponse> getExchangeRate() {
    return currencyLayerWebClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/live").queryParam("source", CurrencyCode.USD).build())
        .retrieve()
        .bodyToMono(ExchangeRateResponse.class);
  }
}
