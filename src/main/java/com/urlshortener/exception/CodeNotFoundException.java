// Thrown when a short code cannot be resolved to a long URL
package com.urlshortener.exception;

public class CodeNotFoundException extends RuntimeException {

  public CodeNotFoundException(String message) {
    super(message);
  }
}
