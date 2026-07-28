// Validates and normalises incoming long URLs before they are shortened
package com.urlshortener.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class UrlValidator {

  private static final int MAX_LENGTH = 2048;

  private UrlValidator() {}

  public static String normalizeAndValidate(String rawUrl) {
    if (rawUrl == null || rawUrl.isBlank()) {
      throw new IllegalArgumentException("url must not be null or blank");
    }
    if (rawUrl.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "url exceeds maximum length of " + MAX_LENGTH + " characters");
    }

    URI uri;
    try {
      uri = new URI(rawUrl.trim());
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("url is not a valid URI: " + e.getMessage(), e);
    }

    String scheme = uri.getScheme();
    if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
      throw new IllegalArgumentException("url scheme must be http or https");
    }
    scheme = scheme.toLowerCase(Locale.ROOT);

    String host = uri.getHost();
    if (host == null || host.isEmpty()) {
      throw new IllegalArgumentException("url must have a non-empty host");
    }
    host = host.toLowerCase(Locale.ROOT);

    int port = uri.getPort();
    boolean isDefaultPort =
        (scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443);
    if (isDefaultPort) {
      port = -1;
    }

    String path = uri.getRawPath();
    if ("/".equals(path)) {
      path = "";
    }

    StringBuilder sb = new StringBuilder();
    sb.append(scheme).append("://").append(host);
    if (port != -1) {
      sb.append(':').append(port);
    }
    if (path != null) {
      sb.append(path);
    }
    if (uri.getRawQuery() != null) {
      sb.append('?').append(uri.getRawQuery());
    }
    return sb.toString();
  }

  public static String hash(String normalizedUrl) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(normalizedUrl.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(hashBytes.length * 2);
      for (byte b : hashBytes) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    }
  }
}
