package com.exchange.foreign_exchange_api.api.dto;

import java.util.List;

public record PaginatedResponse<T>(List<T> data, Meta meta) {
  public record Meta(long totalCount, int totalPages, int currentPage, int limit) {}
}
