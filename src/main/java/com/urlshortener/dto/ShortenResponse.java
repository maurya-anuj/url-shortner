// Response payload for POST /shorten
package com.urlshortener.dto;

public record ShortenResponse(String code, String shortUrl) {}
