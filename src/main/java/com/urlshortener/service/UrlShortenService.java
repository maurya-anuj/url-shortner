// Service layer for creating and resolving short URLs
package com.urlshortener.service;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.repository.ShortUrlRepository;
import org.springframework.stereotype.Service;

@Service
public class UrlShortenService {

  private final ShortUrlRepository shortUrlRepository;

  public UrlShortenService(ShortUrlRepository shortUrlRepository) {
    this.shortUrlRepository = shortUrlRepository;
  }

  public ShortenResponse shorten(ShortenRequest request) {
    return null;
  }

  public String resolve(String code) {
    return null;
  }
}
