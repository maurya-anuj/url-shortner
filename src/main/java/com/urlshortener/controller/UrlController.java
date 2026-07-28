// REST controller exposing the shorten and redirect endpoints
package com.urlshortener.controller;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.service.UrlShortenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UrlController {

  private final UrlShortenService urlShortenService;

  public UrlController(UrlShortenService urlShortenService) {
    this.urlShortenService = urlShortenService;
  }

  @PostMapping("/shorten")
  public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
    return null;
  }

  @GetMapping("/{code}")
  public ResponseEntity<Void> redirect(@PathVariable String code) {
    return null;
  }
}
