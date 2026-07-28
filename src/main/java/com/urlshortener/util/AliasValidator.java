// Validates user-supplied custom aliases
package com.urlshortener.util;

import java.util.Set;

public final class AliasValidator {

  public static final Set<String> RESERVED_WORDS =
      Set.of("shorten", "api", "health", "admin", "static", "favicon.ico");

  private static final int MIN_LENGTH = 3;
  private static final int MAX_LENGTH = 16;
  private static final String ALLOWED_CHARS_REGEX = "[0-9A-Za-z_-]+";

  private AliasValidator() {}

  public static String validateAlias(String alias) {
    if (alias == null || alias.isBlank()) {
      throw new IllegalArgumentException("alias must not be null or blank");
    }
    if (alias.length() < MIN_LENGTH || alias.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "alias length must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters");
    }
    if (!alias.matches(ALLOWED_CHARS_REGEX)) {
      throw new IllegalArgumentException("alias must only contain [0-9A-Za-z_-] characters");
    }
    if (RESERVED_WORDS.contains(alias.toLowerCase())) {
      throw new IllegalArgumentException("alias is a reserved word: " + alias);
    }
    return alias;
  }
}
