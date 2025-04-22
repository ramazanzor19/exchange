package com.exchange.foreign_exchange_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(FxTestContainersConfiguration.class)
@SpringBootTest
class ForeignExchangeApiApplicationTests {

  @Test
  void contextLoads() {}
}
