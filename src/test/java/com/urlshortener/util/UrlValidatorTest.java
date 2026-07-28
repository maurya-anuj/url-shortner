// Unit tests for UrlValidator
package com.urlshortener.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UrlValidatorTest {

  @Test
  @DisplayName("basic HTTPS URL is accepted unchanged")
  void basicHttpsUrlIsAccepted() {
    assertThat(UrlValidator.normalizeAndValidate("https://example.com"))
        .isEqualTo("https://example.com");
  }

  @Test
  @DisplayName("URL with path and query is accepted unchanged")
  void urlWithPathAndQueryIsAccepted() {
    assertThat(UrlValidator.normalizeAndValidate("http://example.com/path?q=1"))
        .isEqualTo("http://example.com/path?q=1");
  }

  @Test
  @DisplayName("host is lowercased while path case is preserved")
  void hostIsLowercasedPathCasePreserved() {
    assertThat(UrlValidator.normalizeAndValidate("https://EXAMPLE.COM/Path"))
        .isEqualTo("https://example.com/Path");
  }

  @Test
  @DisplayName("default HTTPS port 443 is stripped")
  void defaultHttpsPortIsStripped() {
    assertThat(UrlValidator.normalizeAndValidate("https://example.com:443/page"))
        .isEqualTo("https://example.com/page");
  }

  @Test
  @DisplayName("default HTTP port 80 is stripped")
  void defaultHttpPortIsStripped() {
    assertThat(UrlValidator.normalizeAndValidate("http://example.com:80"))
        .isEqualTo("http://example.com");
  }

  @Test
  @DisplayName("trailing slash on root path is removed")
  void trailingSlashOnRootIsRemoved() {
    assertThat(UrlValidator.normalizeAndValidate("https://example.com/"))
        .isEqualTo("https://example.com");
  }

  @Test
  @DisplayName("null URL throws IllegalArgumentException")
  void nullUrlThrows() {
    assertThatThrownBy(() -> UrlValidator.normalizeAndValidate(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("empty URL throws IllegalArgumentException")
  void emptyUrlThrows() {
    assertThatThrownBy(() -> UrlValidator.normalizeAndValidate(""))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("blank URL throws IllegalArgumentException")
  void blankUrlThrows() {
    assertThatThrownBy(() -> UrlValidator.normalizeAndValidate("   "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("ftp scheme is blocked")
  void ftpSchemeIsBlocked() {
    assertThatThrownBy(() -> UrlValidator.normalizeAndValidate("ftp://example.com"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("javascript scheme is blocked")
  void javascriptSchemeIsBlocked() {
    assertThatThrownBy(() -> UrlValidator.normalizeAndValidate("javascript:alert(1)"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("file scheme is blocked")
  void fileSchemeIsBlocked() {
    assertThatThrownBy(() -> UrlValidator.normalizeAndValidate("file:///etc/passwd"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("data scheme is blocked")
  void dataSchemeIsBlocked() {
    assertThatThrownBy(() -> UrlValidator.normalizeAndValidate("data:text/html,<h1>hi</h1>"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("string with no scheme or host throws IllegalArgumentException")
  void noSchemeOrHostThrows() {
    assertThatThrownBy(() -> UrlValidator.normalizeAndValidate("not-a-url"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("URL exceeding max length throws IllegalArgumentException")
  void urlExceedingMaxLengthThrows() {
    String tooLong = "https://example.com/" + "a".repeat(2049 - "https://example.com/".length());
    assertThat(tooLong).hasSize(2049);
    assertThatThrownBy(() -> UrlValidator.normalizeAndValidate(tooLong))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("hash produces a 64-character lowercase hex string")
  void hashProducesSixtyFourCharLowercaseHex() {
    assertThat(UrlValidator.hash("https://example.com")).matches("[0-9a-f]{64}");
  }

  @Test
  @DisplayName("hash is deterministic for the same input")
  void hashIsDeterministic() {
    String input = "https://example.com/page";
    assertThat(UrlValidator.hash(input)).isEqualTo(UrlValidator.hash(input));
  }

  @Test
  @DisplayName("hash differs for different inputs")
  void hashDiffersForDifferentInputs() {
    assertThat(UrlValidator.hash("https://example.com/a"))
        .isNotEqualTo(UrlValidator.hash("https://example.com/b"));
  }
}
