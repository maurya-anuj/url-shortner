// Response payload for POST /shorten
package com.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenResponse {

  private String shortCode;
  private String shortUrl;
  private String originalUrl;
}
