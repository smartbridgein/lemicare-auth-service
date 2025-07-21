package com.cosmicdoc.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown during the user authentication process.
 * <p>
 * This exception is used to signal specific failures like invalid credentials,
 * an inactive account, or a locked account, which should result in a
 * 401 Unauthorized HTTP status.
 *
 * By extending RuntimeException, it is an "unchecked" exception, meaning
 * it does not need to be declared in method signatures.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED) // This provides a default HTTP status if the exception is not caught.
public class AuthenticationException extends RuntimeException {

    /**
     * Constructs a new AuthenticationException with the specified detail message.
     *
     * @param message the detail message.
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs a new AuthenticationException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause (which is saved for later retrieval by the getCause() method).
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}