// Request payload for POST /shorten
package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;

public record ShortenRequest(@NotBlank String url, String customAlias) {}
