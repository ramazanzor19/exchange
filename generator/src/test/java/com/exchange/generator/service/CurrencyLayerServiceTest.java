package com.exchange.generator.service;

import static org.mockito.ArgumentMatchers.any;

import com.exchange.generator.model.CurrencyCode;
import com.exchange.generator.model.ExchangeRateResponse;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CurrencyLayerServiceTest {

  private ResponseSpec responseSpec;
  private CurrencyLayerService currencyLayerService;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    var currencyLayerWebClient = Mockito.mock(WebClient.class);
    var requestHeadersUriSpec = Mockito.mock(RequestHeadersUriSpec.class);
    var requestHeadersSpec = Mockito.mock(RequestHeadersSpec.class);

    responseSpec = Mockito.mock(ResponseSpec.class);

    Mockito.when(currencyLayerWebClient.get()).thenReturn(requestHeadersUriSpec);
    Mockito.when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
    Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

    currencyLayerService = new CurrencyLayerService(currencyLayerWebClient);
  }

  @Test
  void constructor_shouldInitializeWithWebClient() {
    WebClient mockWebClient = Mockito.mock(WebClient.class);

    CurrencyLayerService service = new CurrencyLayerService(mockWebClient);

    Assertions.assertTrue(service != null);
  }

  @Test
  void getExchangeRate_shouldReturnExchangeRateResponse() {
    // Arrange
    ExchangeRateResponse mockResponse =
        new ExchangeRateResponse(
            true, Map.of(CurrencyCode.EUR.name(), 0.9, CurrencyCode.NOK.name(), 11.0));
    Mockito.when(responseSpec.bodyToMono(ExchangeRateResponse.class))
        .thenReturn(Mono.just(mockResponse));

    Mono<ExchangeRateResponse> result = currencyLayerService.getExchangeRate();

    StepVerifier.create(result).expectNext(mockResponse).verifyComplete();

    Mockito.verify(responseSpec).bodyToMono(ExchangeRateResponse.class);
  }

  @Test
  void getExchangeRate_shouldPropagateErrorWhenWebClientFails() {
    RuntimeException expectedError = new RuntimeException("API error");
    Mockito.when(responseSpec.bodyToMono(ExchangeRateResponse.class))
        .thenReturn(Mono.error(expectedError));

    StepVerifier.create(currencyLayerService.getExchangeRate())
        .expectErrorMatches(
            throwable -> {
              Assertions.assertEquals("API error", throwable.getCause().getMessage());
              return true;
            })
        .verify();

    Mockito.verify(responseSpec, Mockito.times(1)).bodyToMono(ExchangeRateResponse.class);
  }
}
