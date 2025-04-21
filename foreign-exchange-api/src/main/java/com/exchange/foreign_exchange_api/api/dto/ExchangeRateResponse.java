package com.exchange.foreign_exchange_api.api.dto;

import com.exchange.foreign_exchange_api.model.CurrencyCode;
import java.time.Instant;

public record ExchangeRateResponse(
    CurrencyCode source, CurrencyCode target, double rate, Instant timestamp) {}
