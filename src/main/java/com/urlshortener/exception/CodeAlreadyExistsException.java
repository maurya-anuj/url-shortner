// Thrown when a requested short code or custom alias is already taken
package com.urlshortener.exception;

public class CodeAlreadyExistsException extends RuntimeException {

  public CodeAlreadyExistsException(String message) {
    super(message);
  }
}
