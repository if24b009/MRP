package org.mrp.exceptions;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}

//Usage: Ownership mismatch -> only creator can edit/delete