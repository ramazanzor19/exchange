package com.exchange.foreign_exchange_api.api;

import com.exchange.foreign_exchange_api.api.dto.PaginatedResponse;
import com.exchange.foreign_exchange_api.api.dto.TransactionRequest;
import com.exchange.foreign_exchange_api.api.dto.TransactionResponse;
import com.exchange.foreign_exchange_api.exception.PageOutOfRangeException;
import com.exchange.foreign_exchange_api.service.ConversionHistoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ConversionHistoryController {

  private final ConversionHistoryService historyService;

  public ConversionHistoryController(ConversionHistoryService historyService) {
    this.historyService = historyService;
  }

  @GetMapping("/conversions")
  public Mono<PaginatedResponse<TransactionResponse>> getConversionHistory(
      @Valid TransactionRequest request) {
    return historyService
        .getConversionHistory(
            request.transactionId(),
            request.start(),
            request.end(),
            request.page(),
            request.limit())
        .flatMap(
            result -> {
              int totalPages = (int) Math.ceil((double) result.totalCount() / request.limit());

              if (request.page() >= totalPages) {
                return Mono.error(new PageOutOfRangeException(request.page(), totalPages - 1));
              }
              return Mono.just(
                  new PaginatedResponse<>(
                      result.data(),
                      new PaginatedResponse.Meta(
                          result.totalCount(), totalPages, request.page(), request.limit())));
            });
  }
}
