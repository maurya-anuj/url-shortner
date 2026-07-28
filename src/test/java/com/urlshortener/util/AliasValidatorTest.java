// Unit tests for AliasValidator
package com.urlshortener.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AliasValidatorTest {

  @Test
  @DisplayName("alias with hyphen is valid")
  void aliasWithHyphenIsValid() {
    assertThat(AliasValidator.validateAlias("my-link")).isEqualTo("my-link");
  }

  @Test
  @DisplayName("minimum length alias is valid")
  void minimumLengthAliasIsValid() {
    assertThat(AliasValidator.validateAlias("abc")).isEqualTo("abc");
  }

  @Test
  @DisplayName("mixed-case alias with underscore is valid and case is preserved")
  void mixedCaseAliasWithUnderscoreIsValid() {
    assertThat(AliasValidator.validateAlias("Test_123")).isEqualTo("Test_123");
  }

  @Test
  @DisplayName("maximum length (16-char) alias is valid")
  void maximumLengthAliasIsValid() {
    String alias = "a".repeat(16);
    assertThat(AliasValidator.validateAlias(alias)).isEqualTo(alias);
  }

  @Test
  @DisplayName("null alias throws IllegalArgumentException")
  void nullAliasThrows() {
    assertThatThrownBy(() -> AliasValidator.validateAlias(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("empty alias throws IllegalArgumentException")
  void emptyAliasThrows() {
    assertThatThrownBy(() -> AliasValidator.validateAlias(""))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("alias shorter than 3 characters throws IllegalArgumentException")
  void tooShortAliasThrows() {
    assertThatThrownBy(() -> AliasValidator.validateAlias("ab"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("alias longer than 16 characters throws IllegalArgumentException")
  void tooLongAliasThrows() {
    String alias = "a".repeat(17);
    assertThatThrownBy(() -> AliasValidator.validateAlias(alias))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("alias with a space throws IllegalArgumentException")
  void aliasWithSpaceThrows() {
    assertThatThrownBy(() -> AliasValidator.validateAlias("my link"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("alias with special characters throws IllegalArgumentException")
  void aliasWithSpecialCharsThrows() {
    assertThatThrownBy(() -> AliasValidator.validateAlias("abc!@#"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("reserved word 'shorten' throws IllegalArgumentException")
  void reservedWordShortenThrows() {
    assertThatThrownBy(() -> AliasValidator.validateAlias("shorten"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("reserved word check is case-insensitive")
  void reservedWordCheckIsCaseInsensitive() {
    assertThatThrownBy(() -> AliasValidator.validateAlias("SHORTEN"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("reserved word 'admin' throws IllegalArgumentException")
  void reservedWordAdminThrows() {
    assertThatThrownBy(() -> AliasValidator.validateAlias("admin"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
