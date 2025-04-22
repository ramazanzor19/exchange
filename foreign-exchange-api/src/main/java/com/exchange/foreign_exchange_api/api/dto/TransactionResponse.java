package com.exchange.foreign_exchange_api.api.dto;

import com.exchange.foreign_exchange_api.model.CurrencyCode;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
    UUID id,
    double sourceAmount,
    CurrencyCode sourceCurrency,
    double targetAmount,
    CurrencyCode targetCurrency,
    double exchangeRate,
    Instant timestamp) {}
