// Utility for encoding/decoding numeric ids to/from Base62 short codes
package com.urlshortener.util;

public final class Base62Codec {

  private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static final int BASE = ALPHABET.length();

  private Base62Codec() {}

  public static String encode(long value) {
    if (value < 0) {
      throw new IllegalArgumentException("value must be non-negative: " + value);
    }
    if (value == 0) {
      return "0";
    }
    StringBuilder sb = new StringBuilder();
    while (value > 0) {
      int remainder = (int) (value % BASE);
      sb.append(ALPHABET.charAt(remainder));
      value /= BASE;
    }
    return sb.reverse().toString();
  }

  public static long decode(String code) {
    if (code == null || code.isEmpty()) {
      throw new IllegalArgumentException("code must not be null or empty");
    }
    long result = 0L;
    for (int i = 0; i < code.length(); i++) {
      int digit = ALPHABET.indexOf(code.charAt(i));
      if (digit < 0) {
        throw new IllegalArgumentException("invalid Base62 character: " + code.charAt(i));
      }
      result = result * BASE + digit;
    }
    return result;
  }
}
