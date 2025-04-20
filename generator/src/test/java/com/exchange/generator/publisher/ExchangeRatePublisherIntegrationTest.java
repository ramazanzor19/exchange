package com.exchange.generator.publisher;

import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

@Tag("integration")
@SpringBootTest
class ExchangeRatePublisherIntegrationTest {

  @Autowired private ExchangeRatePublisher publisher;

  @Autowired private ReactiveRedisTemplate<String, Double> redisTemplate;

  @Test
  void publishExchangeRates_shouldPersistInRedis() {
    String key = "exchange_rates";
    Map<String, Double> rates = Map.of("EUR", 0.92, "GBP", 0.79);

    StepVerifier.create(
            publisher
                .publishExchangeRates(key, rates)
                .thenMany(redisTemplate.opsForHash().entries(key)))
        .expectNextCount(2)
        .verifyComplete();
  }
}
