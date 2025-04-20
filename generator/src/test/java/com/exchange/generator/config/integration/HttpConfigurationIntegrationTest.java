package com.exchange.generator.config.integration;

import com.exchange.generator.config.HttpConfiguration;
import com.exchange.generator.model.ExchangeRateResponse;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

@Tag("integration")
@SpringBootTest(classes = HttpConfiguration.class)
class HttpConfigurationIntegrationTest {

  private static MockWebServer mockWebServer;

  @Autowired private WebClient webClient;

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("currencylayer.host", () -> "http://localhost:" + mockWebServer.getPort());
    registry.add("currencylayer.api-key", () -> "test-access-key");
  }

  @BeforeAll
  static void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @AfterAll
  static void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  void webClientRequests_shouldIncludeAccessKey() throws Exception {
    String responseBody =
        """
        {
            "success": true,
            "quotes": {
                "USDEUR": 0.92,
                "USDGBP": 0.79,
                "USDJPY": 151.50
            }
        }
        """;

    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseBody));

    webClient
        .get()
        .uri("/live?source=USD")
        .retrieve()
        .bodyToMono(ExchangeRateResponse.class)
        .block();

    var recordedRequest = mockWebServer.takeRequest();
    String requestUrl = recordedRequest.getRequestUrl().toString();
    Assertions.assertTrue(
        requestUrl.contains("access_key=test-access-key"),
        "Request should include access_key parameter");
  }
}
