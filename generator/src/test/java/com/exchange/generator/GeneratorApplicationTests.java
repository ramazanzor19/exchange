package com.exchange.generator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@Tag("integration")
@SpringBootTest
class GeneratorApplicationIntegrationTest {

  @Test
  void contextLoads(ApplicationContext context) {
    Assertions.assertNotNull(context);
  }

  @Test
  void mainMethodStartsApplication() {
    GeneratorApplication.main(new String[] {});
  }
}
