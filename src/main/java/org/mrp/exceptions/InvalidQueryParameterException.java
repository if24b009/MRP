package org.mrp.exceptions;

public class InvalidQueryParameterException extends RuntimeException {

    public InvalidQueryParameterException(String message) {
        super(message);
    }

    public InvalidQueryParameterException(String message, Throwable cause) {
        super(message, cause);
    } //Throwable = root superclass of all errors/exceptions | cause = original exception that caused this exception
}

//Usage: Invalid filter parameters in url