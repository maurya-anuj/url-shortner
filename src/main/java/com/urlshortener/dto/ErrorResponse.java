// Standard error payload returned by the global exception handler
package com.urlshortener.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErrorResponse {

  private int status;
  private String error;
  private String message;

  @Builder.Default private Instant timestamp = Instant.now();
}
