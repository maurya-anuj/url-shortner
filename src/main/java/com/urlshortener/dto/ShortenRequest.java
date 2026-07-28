// Request payload for POST /shorten
package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShortenRequest {

  @NotBlank(message = "URL is required")
  private String url;

  private String customAlias;
}
