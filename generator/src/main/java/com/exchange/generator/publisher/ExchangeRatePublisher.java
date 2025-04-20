package com.exchange.generator.publisher;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ExchangeRatePublisher {

  private final Logger LOGGER = LoggerFactory.getLogger(ExchangeRatePublisher.class);

  private final ReactiveRedisTemplate<String, Double> redisTemplate;

  public ExchangeRatePublisher(ReactiveRedisTemplate<String, Double> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public Mono<Boolean> publishExchangeRates(String key, Map<String, Double> map) {
    return redisTemplate
        .opsForHash()
        .putAll(key, map)
        .doOnSuccess((success) -> LOGGER.info("Map published to Redis"))
        .doOnError((error) -> LOGGER.error("Error publishing to Redis: {}", error.getMessage()))
        .onErrorReturn(false);
  }
}
