package com.exchange.generator.config;

import static org.junit.jupiter.api.Assertions.*;

import com.exchange.generator.config.properties.CurrencyLayerApiProperties;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

public class HttpConfigurationTest {

  @Test
  void currencyLayerWebClient_shouldBeConfiguredCorrectly() {
    CurrencyLayerApiProperties properties = Mockito.mock(CurrencyLayerApiProperties.class);
    Mockito.when(properties.host()).thenReturn("https://api.currencylayer.com");
    Mockito.when(properties.apiKey()).thenReturn("test-api-key");

    HttpConfiguration httpConfiguration = new HttpConfiguration();

    WebClient webClient = httpConfiguration.currencyLayerWebClient(properties);

    assertNotNull(webClient);
  }
}
