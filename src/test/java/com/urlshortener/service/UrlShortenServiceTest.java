// Unit tests for UrlShortenService
package com.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.exception.CodeAlreadyExistsException;
import com.urlshortener.exception.CodeNotFoundException;
import com.urlshortener.model.ShortUrl;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.util.Base62Codec;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class UrlShortenServiceTest {

  @Mock private ShortUrlRepository shortUrlRepository;

  @Mock private JdbcTemplate jdbcTemplate;

  @InjectMocks private UrlShortenService urlShortenService;

  @Test
  @DisplayName("shorten new URL generates code from sequence")
  void shortenNewUrlGeneratesCodeFromSequence() {
    when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(10000L);
    when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

    ShortenResponse response =
        urlShortenService.shorten(new ShortenRequest("https://example.com", null));

    assertThat(response.getShortCode()).isEqualTo(Base62Codec.encode(10000L));
  }

  @Test
  @DisplayName("shorten duplicate URL returns existing code")
  void shortenDuplicateUrlReturnsExistingCode() {
    when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(10001L);
    when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(0);
    when(shortUrlRepository.findByUrlHash(anyString()))
        .thenReturn(Optional.of(ShortUrl.builder().code("existingCode").build()));

    ShortenResponse response =
        urlShortenService.shorten(new ShortenRequest("https://example.com", null));

    assertThat(response.getShortCode()).isEqualTo("existingCode");
  }

  @Test
  @DisplayName("shorten with custom alias uses alias as code")
  void shortenWithCustomAliasUsesAliasAsCode() {
    when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

    ShortenResponse response =
        urlShortenService.shorten(new ShortenRequest("https://example.com", "my-link"));

    assertThat(response.getShortCode()).isEqualTo("my-link");
  }

  @Test
  @DisplayName("shorten with duplicate custom alias throws CodeAlreadyExistsException")
  void shortenWithDuplicateCustomAliasThrowsCodeAlreadyExistsException() {
    when(jdbcTemplate.update(anyString(), any(Object[].class)))
        .thenThrow(new DataIntegrityViolationException("duplicate key"));

    assertThatThrownBy(
            () -> urlShortenService.shorten(new ShortenRequest("https://example.com", "my-link")))
        .isInstanceOf(CodeAlreadyExistsException.class);
  }

  @Test
  @DisplayName("resolve existing code returns URL")
  void resolveExistingCodeReturnsUrl() {
    when(shortUrlRepository.findByCode("abc"))
        .thenReturn(Optional.of(ShortUrl.builder().longUrl("https://example.com").build()));

    assertThat(urlShortenService.resolve("abc")).isEqualTo("https://example.com");
  }

  @Test
  @DisplayName("resolve unknown code throws CodeNotFoundException")
  void resolveUnknownCodeThrowsCodeNotFoundException() {
    when(shortUrlRepository.findByCode("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> urlShortenService.resolve("missing"))
        .isInstanceOf(CodeNotFoundException.class);
  }

  @Test
  @DisplayName("shorten with invalid URL throws IllegalArgumentException")
  void shortenWithInvalidUrlThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> urlShortenService.shorten(new ShortenRequest("not-a-url", null)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
