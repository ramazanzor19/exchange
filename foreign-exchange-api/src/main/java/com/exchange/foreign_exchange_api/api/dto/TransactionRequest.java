package com.exchange.foreign_exchange_api.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record TransactionRequest(
    UUID transactionId,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
    @Min(0) Integer page,
    @Min(10) @Max(200) int limit) {
  private static final List<Integer> ALLOWED_LIMITS = List.of(10, 25, 50, 100, 200);

  public TransactionRequest {
    if (page == null) {
      page = 0;
    }
    if (!ALLOWED_LIMITS.contains(limit)) {
      throw new IllegalArgumentException("Limit must be one of: " + ALLOWED_LIMITS);
    }
    if (start != null && end != null && start.isAfter(end)) {
      throw new IllegalArgumentException("Start date must be before end date");
    }
  }
}
