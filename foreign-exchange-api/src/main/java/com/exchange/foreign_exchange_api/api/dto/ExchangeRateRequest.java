package com.exchange.foreign_exchange_api.api.dto;

import com.exchange.foreign_exchange_api.model.CurrencyCode;
import jakarta.validation.constraints.NotNull;

public record ExchangeRateRequest(@NotNull CurrencyCode source, @NotNull CurrencyCode target) {}
