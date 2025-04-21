package com.exchange.foreign_exchange_api.api;

import com.exchange.foreign_exchange_api.api.dto.ConversionRequest;
import com.exchange.foreign_exchange_api.api.dto.ConversionResponse;
import com.exchange.foreign_exchange_api.service.ConversionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ConversionController {

  private final ConversionService conversionService;

  public ConversionController(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  @PostMapping("/convert")
  public Mono<ConversionResponse> convert(@Valid @RequestBody ConversionRequest request) {
    return conversionService.convert(request.amount(), request.source(), request.target());
  }
}
