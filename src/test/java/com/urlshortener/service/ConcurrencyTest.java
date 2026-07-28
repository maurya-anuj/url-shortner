// Concurrency tests proving duplicate URLs don't race into multiple rows
package com.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.util.UrlValidator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConcurrencyTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16")
          .withDatabaseName("urlshortener")
          .withUsername("app")
          .withPassword("app");

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  private static final int REQUEST_COUNT = 500;
  private static final int THREAD_COUNT = 50;

  @Autowired private UrlShortenService urlShortenService;

  @Autowired private ShortUrlRepository shortUrlRepository;

  @BeforeEach
  void clearDatabase() {
    shortUrlRepository.deleteAll();
  }

  @Test
  @Timeout(30)
  @DisplayName("500 concurrent requests for same URL produce exactly one row and same code")
  void concurrentRequestsForSameUrlProduceOneRowAndSameCode() throws Exception {
    String url = "https://example.com/concurrent-test";

    Set<String> codes = runConcurrently(i -> url);

    assertThat(codes).hasSize(1);

    String urlHash = UrlValidator.hash(UrlValidator.normalizeAndValidate(url));
    assertThat(shortUrlRepository.countByUrlHash(urlHash)).isEqualTo(1);
  }

  @Test
  @Timeout(30)
  @DisplayName("500 concurrent requests for different URLs all succeed with unique codes")
  void concurrentRequestsForDifferentUrlsProduceUniqueCodes() throws Exception {
    Set<String> codes = runConcurrently(i -> "https://example.com/page-" + i);

    assertThat(codes).hasSize(REQUEST_COUNT);
    assertThat(shortUrlRepository.count()).isEqualTo(REQUEST_COUNT);
  }

  private Set<String> runConcurrently(java.util.function.IntFunction<String> urlForIndex)
      throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch startGate = new CountDownLatch(1);

    try {
      List<Future<String>> futures = new ArrayList<>(REQUEST_COUNT);
      for (int i = 0; i < REQUEST_COUNT; i++) {
        String url = urlForIndex.apply(i);
        futures.add(
            executor.submit(
                () -> {
                  startGate.await();
                  return urlShortenService.shorten(new ShortenRequest(url, null)).getShortCode();
                }));
      }

      startGate.countDown();

      Set<String> codes = new HashSet<>();
      for (Future<String> future : futures) {
        codes.add(future.get());
      }
      return codes;
    } finally {
      executor.shutdown();
    }
  }
}
