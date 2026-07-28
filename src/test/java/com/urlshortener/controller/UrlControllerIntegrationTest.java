// Integration tests for UrlController against a real Postgres via Testcontainers
package com.urlshortener.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.util.UrlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlControllerIntegrationTest {

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

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private ShortUrlRepository shortUrlRepository;

  @BeforeEach
  void clearDatabase() {
    shortUrlRepository.deleteAll();
  }

  @Test
  @DisplayName("shorten and redirect round trip")
  void shortenAndRedirectRoundTrip() {
    ShortenRequest request = new ShortenRequest("https://example.com", null);

    ResponseEntity<ShortenResponse> shortenResponse =
        restTemplate.postForEntity("/shorten", request, ShortenResponse.class);

    assertThat(shortenResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    ShortenResponse body = shortenResponse.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getShortCode()).isNotBlank();
    assertThat(body.getShortUrl()).isNotBlank();

    ResponseEntity<Void> redirectResponse =
        restTemplate.getForEntity("/" + body.getShortCode(), Void.class);

    assertThat(redirectResponse.getStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY);
    assertThat(redirectResponse.getHeaders().getLocation().toString())
        .isEqualTo("https://example.com");
  }

  @Test
  @DisplayName("unknown code returns 404")
  void unknownCodeReturns404() {
    ResponseEntity<String> response = restTemplate.getForEntity("/nonexistent", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("same URL shortened twice returns same code")
  void sameUrlShortenedTwiceReturnsSameCode() {
    ShortenRequest request = new ShortenRequest("https://example.com/duplicate", null);

    ShortenResponse first = restTemplate.postForObject("/shorten", request, ShortenResponse.class);
    ShortenResponse second = restTemplate.postForObject("/shorten", request, ShortenResponse.class);

    assertThat(second.getShortCode()).isEqualTo(first.getShortCode());

    String urlHash =
        UrlValidator.hash(UrlValidator.normalizeAndValidate("https://example.com/duplicate"));
    assertThat(shortUrlRepository.countByUrlHash(urlHash)).isEqualTo(1);
  }

  @Test
  @DisplayName("custom alias works")
  void customAliasWorks() {
    ShortenRequest request = new ShortenRequest("https://example.com", "my-link");

    ResponseEntity<ShortenResponse> shortenResponse =
        restTemplate.postForEntity("/shorten", request, ShortenResponse.class);

    assertThat(shortenResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(shortenResponse.getBody().getShortCode()).isEqualTo("my-link");

    ResponseEntity<Void> redirectResponse = restTemplate.getForEntity("/my-link", Void.class);

    assertThat(redirectResponse.getStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY);
    assertThat(redirectResponse.getHeaders().getLocation().toString())
        .isEqualTo("https://example.com");
  }

  @Test
  @DisplayName("custom alias conflict returns 409")
  void customAliasConflictReturns409() {
    restTemplate.postForEntity(
        "/shorten", new ShortenRequest("https://example.com/one", "taken"), ShortenResponse.class);

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/shorten", new ShortenRequest("https://example.com/two", "taken"), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  @DisplayName("invalid URL returns 400")
  void invalidUrlReturns400() {
    ResponseEntity<String> response =
        restTemplate.postForEntity("/shorten", new ShortenRequest("not-a-url", null), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("javascript scheme blocked")
  void javascriptSchemeBlocked() {
    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/shorten", new ShortenRequest("javascript:alert(1)", null), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("missing URL returns 400")
  void missingUrlReturns400() {
    ResponseEntity<String> response =
        restTemplate.postForEntity("/shorten", new ShortenRequest("", null), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("custom alias with reserved word returns 400")
  void customAliasWithReservedWordReturns400() {
    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/shorten", new ShortenRequest("https://example.com", "shorten"), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
