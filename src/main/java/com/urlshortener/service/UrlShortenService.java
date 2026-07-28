// Service layer for creating and resolving short URLs
package com.urlshortener.service;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.exception.CodeAlreadyExistsException;
import com.urlshortener.exception.CodeNotFoundException;
import com.urlshortener.model.ShortUrl;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.util.AliasValidator;
import com.urlshortener.util.Base62Codec;
import com.urlshortener.util.UrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UrlShortenService {

  private final ShortUrlRepository shortUrlRepository;
  private final JdbcTemplate jdbcTemplate;
  private final String baseUrl;

  public UrlShortenService(
      ShortUrlRepository shortUrlRepository,
      JdbcTemplate jdbcTemplate,
      @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
    this.shortUrlRepository = shortUrlRepository;
    this.jdbcTemplate = jdbcTemplate;
    this.baseUrl = baseUrl;
  }

  @Transactional
  public ShortenResponse shorten(ShortenRequest request) {
    String normalizedUrl = UrlValidator.normalizeAndValidate(request.getUrl());
    String urlHash = UrlValidator.hash(normalizedUrl);

    String code =
        (request.getCustomAlias() != null && !request.getCustomAlias().isBlank())
            ? shortenWithCustomAlias(request.getCustomAlias(), normalizedUrl, urlHash)
            : shortenWithGeneratedCode(normalizedUrl, urlHash);

    return ShortenResponse.builder()
        .shortCode(code)
        .shortUrl(baseUrl + "/" + code)
        .originalUrl(normalizedUrl)
        .build();
  }

  private String shortenWithCustomAlias(String rawAlias, String normalizedUrl, String urlHash) {
    String code = AliasValidator.validateAlias(rawAlias);
    try {
      jdbcTemplate.update(
          "INSERT INTO short_url (code, long_url, url_hash, is_custom) VALUES (?, ?, ?, true)",
          code,
          normalizedUrl,
          urlHash);
    } catch (DataIntegrityViolationException e) {
      throw new CodeAlreadyExistsException("Alias already exists: " + code);
    }
    return code;
  }

  private String shortenWithGeneratedCode(String normalizedUrl, String urlHash) {
    Long seq = jdbcTemplate.queryForObject("SELECT nextval('short_url_code_seq')", Long.class);
    String code = Base62Codec.encode(seq);

    int rowsAffected =
        jdbcTemplate.update(
            "INSERT INTO short_url (code, long_url, url_hash, is_custom) VALUES (?, ?, ?, false) "
                + "ON CONFLICT (url_hash) WHERE is_custom = false DO NOTHING",
            code,
            normalizedUrl,
            urlHash);

    if (rowsAffected == 0) {
      return shortUrlRepository
          .findByUrlHash(urlHash)
          .map(ShortUrl::getCode)
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "Expected existing short_url row for hash: " + urlHash));
    }
    return code;
  }

  public String resolve(String code) {
    return shortUrlRepository
        .findByCode(code)
        .map(ShortUrl::getLongUrl)
        .orElseThrow(() -> new CodeNotFoundException("No short URL found for code: " + code));
  }
}
