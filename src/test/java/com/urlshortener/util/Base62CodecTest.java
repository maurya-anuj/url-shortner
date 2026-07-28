// Unit tests for Base62Codec
package com.urlshortener.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Base62CodecTest {

  @Test
  @DisplayName("encode(0) returns \"0\"")
  void encodeZeroReturnsZeroString() {
    assertThat(Base62Codec.encode(0L)).isEqualTo("0");
  }

  @Test
  @DisplayName("encode(10000) produces a valid Base62 string")
  void encodeStartingSequenceValueIsValidBase62() {
    String encoded = Base62Codec.encode(10000L);
    assertThat(encoded).matches("[0-9A-Za-z]+");
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 1L, 61L, 62L, 10000L, 999999L, Long.MAX_VALUE})
  @DisplayName("decode(encode(n)) round-trips to n")
  void encodeDecodeRoundTrips(long value) {
    assertThat(Base62Codec.decode(Base62Codec.encode(value))).isEqualTo(value);
  }

  @Test
  @DisplayName("encode output only contains characters from [0-9A-Za-z]")
  void encodeOutputMatchesCharsetRegex() {
    assertThat(Base62Codec.encode(123456789L)).matches("[0-9A-Za-z]+");
  }

  @Test
  @DisplayName("encode throws IllegalArgumentException for negative input")
  void encodeNegativeThrows() {
    assertThatThrownBy(() -> Base62Codec.encode(-1L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("decode throws IllegalArgumentException for null input")
  void decodeNullThrows() {
    assertThatThrownBy(() -> Base62Codec.decode(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("decode throws IllegalArgumentException for empty string")
  void decodeEmptyThrows() {
    assertThatThrownBy(() -> Base62Codec.decode(""))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("decode throws IllegalArgumentException for string with invalid characters")
  void decodeInvalidCharsThrows() {
    assertThatThrownBy(() -> Base62Codec.decode("abc!"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("encode(n) and encode(n+1) produce different strings")
  void encodeIsMonotonicallyDistinct() {
    for (long n = 0; n < 1000; n++) {
      assertThat(Base62Codec.encode(n)).isNotEqualTo(Base62Codec.encode(n + 1));
    }
  }
}
