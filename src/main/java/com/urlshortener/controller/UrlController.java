// REST controller exposing the shorten, redirect, and health endpoints
package com.urlshortener.controller;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.service.UrlShortenService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class UrlController {

  private final UrlShortenService urlShortenService;

  public UrlController(UrlShortenService urlShortenService) {
    this.urlShortenService = urlShortenService;
  }

  @PostMapping("/shorten")
  public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
    ShortenResponse response = urlShortenService.shorten(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{code}")
  public ResponseEntity<Void> redirect(@PathVariable String code) {
    String longUrl = urlShortenService.resolve(code);
    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
        .header(HttpHeaders.LOCATION, longUrl)
        .header(HttpHeaders.CACHE_CONTROL, "no-cache")
        .build();
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(Map.of("status", "up"));
  }
}
