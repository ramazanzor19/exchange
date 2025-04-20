package com.exchange.generator.model;

import java.util.Map;

public record ExchangeRateResponse(boolean success, Map<String, Double> quotes) {}
