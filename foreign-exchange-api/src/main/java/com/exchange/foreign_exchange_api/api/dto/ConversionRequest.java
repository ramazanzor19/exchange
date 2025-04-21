package com.exchange.foreign_exchange_api.api.dto;

import com.exchange.foreign_exchange_api.model.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ConversionRequest(
    @NotNull @Positive double amount, @NotNull CurrencyCode source, @NotNull CurrencyCode target) {}
