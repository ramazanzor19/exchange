package com.exchange.foreign_exchange_api.model;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "conversion_history")
public record Transaction(
    UUID id,
    double sourceAmount,
    CurrencyCode sourceCurrency,
    double targetAmount,
    CurrencyCode targetCurrency,
    double exchangeRate,
    Instant timestamp) {}
