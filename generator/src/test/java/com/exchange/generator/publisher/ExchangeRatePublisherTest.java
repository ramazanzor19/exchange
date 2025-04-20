package com.exchange.generator.publisher;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ExchangeRatePublisherTest {

  private ReactiveRedisTemplate<String, Double> redisTemplate;
  private ReactiveHashOperations<String, Object, Object> hashOperations;
  private ExchangeRatePublisher publisher;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    redisTemplate = Mockito.mock(ReactiveRedisTemplate.class);
    hashOperations = Mockito.mock(ReactiveHashOperations.class);

    publisher = new ExchangeRatePublisher(redisTemplate);

    when(redisTemplate.opsForHash()).thenReturn(hashOperations);
  }

  @Test
  void publishExchangeRates_shouldSuccessfullyPublish() {
    // Arrange
    String key = "exchange_rates";
    Map<String, Double> rates = Map.of("EUR", 0.92, "GBP", 0.79);

    when(hashOperations.putAll(key, rates)).thenReturn(Mono.just(true));

    // Act & Assert
    StepVerifier.create(publisher.publishExchangeRates(key, rates))
        .expectNext(true)
        .verifyComplete();

    verify(redisTemplate).opsForHash();
    verify(hashOperations).putAll(key, rates);
  }

  @Test
  void publishExchangeRates_shouldHandleError() {
    String key = "exchange_rates";
    Map<String, Double> rates = Map.of("EUR", 0.92);

    when(hashOperations.putAll(anyString(), anyMap()))
        .thenReturn(Mono.error(new RuntimeException("Redis down")));

    StepVerifier.create(publisher.publishExchangeRates(key, rates))
        .expectNext(false)
        .verifyComplete();
  }
}
