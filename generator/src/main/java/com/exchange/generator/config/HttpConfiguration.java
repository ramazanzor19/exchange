package com.exchange.generator.config;

import com.exchange.generator.config.filter.AccessKeyFilter;
import com.exchange.generator.config.properties.CurrencyLayerApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
@ConfigurationPropertiesScan
public class HttpConfiguration {

  @Bean
  public WebClient currencyLayerWebClient(CurrencyLayerApiProperties properties) {
    return WebClient.builder()
        .baseUrl(properties.host())
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .filter(AccessKeyFilter.addAccessKey(properties.apiKey()))
        .build();
  }
}
