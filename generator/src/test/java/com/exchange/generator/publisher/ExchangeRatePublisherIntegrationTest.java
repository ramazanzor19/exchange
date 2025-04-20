package com.exchange.generator.publisher;

import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

@Tag("integration")
@SpringBootTest
@Testcontainers
class ExchangeRatePublisherIntegrationTest {

  @Container
  static GenericContainer<?> redisContainer =
      new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  @Autowired private ExchangeRatePublisher publisher;

  @Autowired private ReactiveRedisTemplate<String, Double> redisTemplate;

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redisContainer::getHost);
    registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
  }

  @Test
  void publishExchangeRates_shouldPersistInRedis() {
    String key = "exchange_rates";
    Map<String, Double> rates = Map.of("EUR", 0.92, "GBP", 0.47);

    StepVerifier.create(
            publisher
                .publishExchangeRates(key, rates)
                .thenMany(redisTemplate.opsForHash().entries(key)))
        .expectNextCount(2)
        .verifyComplete();
  }
}
