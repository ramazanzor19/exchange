package com.exchange.generator.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("currencylayer")
public record CurrencyLayerApiProperties(String host, String apiKey) {}
