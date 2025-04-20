package com.exchange.generator.config.filter;

import java.net.URI;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

public class AccessKeyFilter {

  public static ExchangeFilterFunction addAccessKey(String accessKey) {
    return ExchangeFilterFunction.ofRequestProcessor(
        request -> {
          URI originalUri = request.url();

          URI updatedUri =
              UriComponentsBuilder.fromUri(originalUri)
                  .queryParam("access_key", accessKey)
                  .build(true)
                  .toUri();

          ClientRequest newRequest = ClientRequest.from(request).url(updatedUri).build();

          return Mono.just(newRequest);
        });
  }
}
