package com.platform.booking.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public static ResourceNotFoundException of(String entity, Long id) {
        return new ResourceNotFoundException(entity + " not found: id=" + id);
    }
}
