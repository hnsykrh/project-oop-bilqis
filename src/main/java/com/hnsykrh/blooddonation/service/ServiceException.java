package com.hnsykrh.blooddonation.service;

/**
 * Thrown when business rules block an operation (shown to the user in the View).
 */
public final class ServiceException extends Exception {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
