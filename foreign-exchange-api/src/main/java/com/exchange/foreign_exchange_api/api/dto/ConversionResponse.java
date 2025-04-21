package com.exchange.foreign_exchange_api.api.dto;

import com.exchange.foreign_exchange_api.model.CurrencyCode;
import java.time.Instant;
import java.util.UUID;

public record ConversionResponse(
    UUID transactionId,
    CurrencyCode source,
    double sourceAmount,
    CurrencyCode target,
    double targetAmount,
    Instant timestamp
) {}
